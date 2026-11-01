import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import wgjava

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "wg-src" / "src" / "main" / "java" / "witchinggadgets" / "common"
OUTPUT = ROOT / "tools" / "wg_research.json"
BASE = next(parent / "thaumcraft-4-reforged" for parent in ROOT.parents if (parent / "thaumcraft-4-reforged").is_dir())
ASPECT_SOURCE = BASE / "src" / "main" / "java" / "thaumcraft" / "aspect" / "Aspect.java"
CATEGORY = "WITCHGADG"
PAGE_PREFIX = "witchinggadgets_research_page."
CONDITIONS = {
    "WGModCompat.tfRavensFeather!=null": False,
    "Config.allowMirrors": True,
    "WGConfig.allowClusters": False,
    "WGConfig.allowTransmutations": False,
    'Aspect.getAspect("tempus")!=null': False,
}
EXCLUDED_RECIPES = {"CLOAKKAMA_4"}
PAGE_CASTS = {
    "ShapedArcaneRecipe": "arcane",
    "ShapelessArcaneRecipe": "arcane",
    "IArcaneRecipe": "arcane",
    "InfusionRecipe": "infusion",
    "CrucibleRecipe": "crucible",
    "InfusionEnchantmentRecipe": "enchant",
    "IRecipe": "crafting",
    "ShapedOreRecipe": "crafting",
    "ShapelessOreRecipe": "crafting",
    "List": "compound",
}
FLAG_METHODS = {
    "setSecondary": "secondary",
    "setRound": "round",
    "setStub": "stub",
    "setSpecial": "special",
    "setConcealed": "concealed",
    "setAutoUnlock": "autoUnlock",
    "setVirtual": "virtual",
    "setHidden": "hidden",
    "setLost": "lost",
}
LIST_METHODS = {"setParents": "parents", "setParentsHidden": "parentsHidden", "setSiblings": "siblings"}
CAST_PAGE = re.compile(r"^\(\s*(\w+)(\[\])?\s*\)\s*(\w+)\.(get|toArray)\((.*)\)$", re.S)


class Interpreter:
    def __init__(self, source, constants):
        self.source = source
        self.tokens = wgjava.tokenize(source)
        self.parser = wgjava.Parser(source, self.tokens)
        self.constants = constants
        self.variables = {}
        self.entries = []

    def token(self, offset=0):
        return self.parser.peek(offset)

    def is_id(self, value, offset=0):
        token = self.token(offset)
        return token[0] == "id" and token[1] == value

    def matching(self, open_symbol, close_symbol):
        depth = 0
        start = self.parser.index
        while True:
            kind, value, _, _ = self.token()
            if kind == "eof":
                raise ValueError("unbalanced " + open_symbol)
            if kind == "sym" and value == open_symbol:
                depth += 1
            elif kind == "sym" and value == close_symbol:
                depth -= 1
                if depth == 0:
                    self.parser.index += 1
                    return start, self.parser.index
            self.parser.index += 1

    def raw(self, start, end):
        return re.sub(r"\s+", "", self.source[self.tokens[start][2]:self.tokens[end - 1][3]])

    def run_block(self, until_close):
        while True:
            if self.token()[0] == "eof":
                return
            if until_close and self.parser.is_sym("}"):
                self.parser.index += 1
                return
            self.statement(True)

    def skip_statement(self):
        if self.parser.is_sym("{"):
            self.matching("{", "}")
            return
        if self.is_id("if") or self.is_id("for"):
            self.parser.index += 1
            self.matching("(", ")")
            self.skip_statement()
            if self.is_id("else"):
                self.parser.index += 1
                self.skip_statement()
            return
        depth = 0
        while True:
            kind, value, _, _ = self.token()
            if kind == "sym" and value in "({[":
                depth += 1
            elif kind == "sym" and value in ")}]":
                depth -= 1
            elif kind == "sym" and value == ";" and depth == 0:
                self.parser.index += 1
                return
            self.parser.index += 1

    def statement(self, execute):
        if not execute:
            self.skip_statement()
            return
        if self.parser.is_sym("{"):
            self.parser.index += 1
            self.run_block(True)
            return
        if self.parser.is_sym(";"):
            self.parser.index += 1
            return
        if self.is_id("if"):
            self.parser.index += 1
            start, end = self.matching("(", ")")
            condition = self.raw(start + 1, end - 1)
            if condition not in CONDITIONS:
                raise ValueError("unknown condition " + condition)
            value = CONDITIONS[condition]
            self.statement(value)
            if self.is_id("else"):
                self.parser.index += 1
                self.statement(not value)
            return
        if self.is_id("for"):
            self.parser.index += 1
            start, end = self.matching("(", ")")
            header = self.raw(start + 1, end - 1)
            body = self.parser.index
            for name, value in self.loop_values(header):
                self.parser.index = body
                self.variables[name] = value
                self.statement(True)
            if not self.loop_values(header):
                self.skip_statement()
            return
        declaration = self.declaration()
        if declaration:
            return
        node = self.parser.parse_expr()
        self.parser.expect_sym(";")
        self.expression(node)

    def loop_values(self, header):
        match = re.match(r"^int(\w+)=0;\w+<(\w+)\.subNames\.length;\w+\+\+$", header)
        if match:
            return [(match.group(1), index) for index in range(len(self.constants[match.group(2)]))]
        match = re.match(r"^String(\w+):(\w+)\.subNames$", header)
        if match:
            return [(match.group(1), value) for value in self.constants[match.group(2)]]
        raise ValueError("unknown loop " + header)

    def declaration(self):
        offset = 0
        if self.token()[0] != "id":
            return False
        offset = 1
        if self.parser.is_sym("<", offset):
            depth = 0
            while True:
                if self.parser.is_sym("<", offset):
                    depth += 1
                elif self.parser.is_sym(">", offset):
                    depth -= 1
                    if depth == 0:
                        offset += 1
                        break
                offset += 1
        while self.parser.is_sym("[", offset) and self.parser.is_sym("]", offset + 1):
            offset += 2
        if self.token(offset)[0] == "id" and (self.parser.is_sym("=", offset + 1) or self.parser.is_sym(";", offset + 1)):
            name = self.token(offset)[1]
            self.parser.index += offset + 1
        elif self.parser.is_sym("=", 1) and not self.parser.is_sym("=", 2):
            name = self.token()[1]
            self.parser.index += 1
        else:
            return False
        if self.parser.is_sym(";"):
            self.parser.index += 1
            self.variables[name] = None
            return True
        self.parser.expect_sym("=")
        node = self.parser.parse_expr()
        self.parser.expect_sym(";")
        self.variables[name] = self.value(node)
        return True

    def value(self, node):
        if node["t"] == "new" and node["cls"] == "ArrayList":
            return []
        return node

    def expression(self, node):
        root, calls = chain_of(node)
        if root["t"] == "call" and root["target"] is None and root["name"] in ("getResearchItem", "getFakeResearchItem"):
            self.research(root, calls)
            return
        if root["t"] == "name" and isinstance(self.variables.get(root["v"]), list) and len(calls) == 1 and calls[0]["name"] == "add":
            self.variables[root["v"]].append(calls[0]["args"][0])
            return
        raise ValueError("unknown statement " + node["raw"][:120])

    def string(self, node):
        kind = node["t"]
        if kind == "str":
            return node["v"]
        if kind == "num":
            return str(node["v"])
        if kind == "bin" and node["op"] == "+":
            return self.string(node["l"]) + self.string(node["r"])
        if kind == "name" and node["v"] in self.variables:
            return str(self.variables[node["v"]])
        if kind == "cond":
            return self.string(node["yes"] if self.condition(node["cond"]) else node["no"])
        raise ValueError("cannot evaluate string " + node["raw"])

    def condition(self, node):
        if node["t"] == "not":
            return not self.condition(node["v"])
        raw = re.sub(r"\s+", "", node["raw"])
        if raw not in CONDITIONS:
            raise ValueError("unknown condition " + raw)
        return CONDITIONS[raw]

    def integer(self, node):
        if node["t"] == "num":
            return int(node["v"])
        if node["t"] == "neg":
            return -self.integer(node["v"])
        raise ValueError("cannot evaluate int " + node["raw"])

    def aspects(self, node):
        if node["t"] == "name":
            node = self.variables[node["v"]]
        root, calls = chain_of(node)
        if root["t"] != "new" or root["cls"] != "AspectList":
            raise ValueError("cannot evaluate aspects " + node["raw"])
        entries = []
        for call in calls:
            if call["name"] != "add":
                raise ValueError("unexpected aspect call " + call["raw"])
            aspect = call["args"][0]
            if aspect["t"] != "field" or aspect["target"]["v"] != "Aspect":
                raise ValueError("unexpected aspect " + aspect["raw"])
            entries.append([aspect_tag(aspect["name"]), self.integer(call["args"][1])])
        return entries

    def strings(self, args):
        if len(args) == 1 and args[0]["t"] == "array":
            args = args[0]["items"]
        if len(args) == 1 and args[0]["t"] == "name" and isinstance(self.variables.get(args[0]["v"]), list):
            args = self.variables[args[0]["v"]]
        return [self.string(item) for item in args]

    def pages(self, node):
        if node["t"] == "name":
            node = self.variables[node["v"]]
        if node["t"] != "array":
            raise ValueError("cannot evaluate pages " + node["raw"])
        result = []
        for item in node["items"]:
            page = self.page(item)
            if page is not None:
                result.append(page)
        return result

    def page(self, node):
        if node["t"] != "new" or node["cls"] != "ResearchPage" or len(node["args"]) != 1:
            raise ValueError("unknown page " + node["raw"])
        argument = node["args"][0]
        if argument["t"] in ("str", "bin"):
            text = self.string(argument)
            if not text.startswith(PAGE_PREFIX):
                raise ValueError("unexpected page key " + text)
            return {"type": "text", "text": text[len(PAGE_PREFIX):]}
        match = CAST_PAGE.match(cast_text(self.source, node))
        if not match:
            raise ValueError("unknown page argument " + node["raw"])
        cast, _, variable, method, inner = match.groups()
        kind = PAGE_CASTS[cast]
        if method == "toArray":
            recipes = [self.recipe_key(item) for item in self.variables[variable]]
        else:
            expression = parse_fragment(inner)
            recipes = [self.string_fragment(expression)]
        recipes = [recipe for recipe in recipes if recipe not in EXCLUDED_RECIPES]
        if not recipes:
            return None
        return {"type": kind, "recipes": recipes}

    def recipe_key(self, node):
        root, calls = chain_of(node)
        if root["t"] == "call" and root["name"] == "get" and root["target"]["t"] == "name" and root["target"]["v"] == "recipeList":
            return self.string(root["args"][0])
        if node["t"] == "call" and node["name"] == "get":
            return self.string(node["args"][0])
        raise ValueError("cannot evaluate recipe " + node["raw"])

    def string_fragment(self, node):
        return self.string(node)

    def icon(self, node):
        if node["t"] == "name" and node["v"] in self.variables:
            return self.variables[node["v"]]["raw"]
        return node["raw"]

    def research(self, root, calls):
        args = root["args"]
        fake = root["name"] == "getFakeResearchItem"
        if fake:
            original = self.string(args[0])
            entry = new_entry("WGFAKE" + original, [], self.integer(args[2]), self.integer(args[3]), 1, self.icon(args[4]))
            entry["fake"] = {"original": original, "category": self.string(args[1])}
            entry["stub"] = True
            entry["hidden"] = True
        else:
            category = self.string(args[1])
            if category != CATEGORY:
                raise ValueError("unexpected category " + category)
            entry = new_entry(self.string(args[0]), self.aspects(args[2]), self.integer(args[3]), self.integer(args[4]), self.integer(args[5]), self.icon(args[6]))
        registered = False
        for call in calls:
            name = call["name"]
            if name in FLAG_METHODS:
                entry[FLAG_METHODS[name]] = True
            elif name in LIST_METHODS:
                entry[LIST_METHODS[name]] = self.strings(call["args"])
            elif name == "setPages":
                entry["pages"] = self.pages(call["args"][0])
            elif name == "addWarp":
                entry["warp"] = self.integer(call["args"][0])
            elif name == "setItemTriggers":
                entry["itemTriggers"] = [item["raw"] for item in array_items(call["args"])]
            elif name == "setAspectTriggers":
                entry["aspectTriggers"] = [aspect_tag(item["name"]) for item in array_items(call["args"])]
            elif name == "registerResearchItem":
                registered = True
            else:
                raise ValueError("unknown method " + name + " in " + entry["key"])
        if registered:
            entry["complexityClamped"] = max(1, min(3, entry["complexity"]))
            self.entries.append(entry)


def new_entry(key, aspects, column, row, complexity, icon):
    entry = {
        "key": key,
        "category": CATEGORY,
        "aspects": aspects,
        "column": column,
        "row": row,
        "complexity": complexity,
        "icon": icon,
        "parents": [],
        "parentsHidden": [],
        "siblings": [],
        "warp": 0,
        "pages": [],
        "itemTriggers": [],
        "aspectTriggers": [],
        "fake": None,
    }
    for flag in FLAG_METHODS.values():
        entry[flag] = False
    return entry


def chain_of(node):
    calls = []
    current = node
    while current["t"] == "call" and current["target"] is not None:
        calls.append(current)
        current = current["target"]
    calls.reverse()
    return current, calls


ASPECT_TAGS = dict(re.findall(r"public static final Aspect (\w+) = \w+\(\"(\w+)\"", ASPECT_SOURCE.read_text(encoding="utf-8")))


def aspect_tag(name):
    if name not in ASPECT_TAGS:
        raise ValueError("unknown aspect " + name)
    return ASPECT_TAGS[name]


def array_items(args):
    if len(args) == 1 and args[0]["t"] == "array":
        return args[0]["items"]
    return args


def cast_text(source, node):
    raw = node["raw"]
    inner = raw[raw.index("(") + 1:raw.rindex(")")]
    return re.sub(r"\s+", "", inner)


def parse_fragment(text):
    parser = wgjava.Parser(text, wgjava.tokenize(text))
    return parser.parse_expr()


def sub_names(path):
    text = wgjava.strip_comments(path.read_text(encoding="utf-8", errors="replace"))
    match = re.search(r"static\s+String\[\]\s+subNames\s*=\s*\{([^}]*)\}", text)
    return re.findall(r'"([^"]*)"', match.group(1))


def method_body(text, name):
    start = text.index("public static void " + name + "()")
    brace = text.index("{", start)
    depth = 0
    for index in range(brace, len(text)):
        if text[index] == "{":
            depth += 1
        elif text[index] == "}":
            depth -= 1
            if depth == 0:
                return text[brace + 1:index]
    raise ValueError("unbalanced method " + name)


def collect():
    text = wgjava.strip_comments((SOURCE / "WGResearch.java").read_text(encoding="utf-8", errors="replace"))
    constants = {
        "ItemCloak": sub_names(SOURCE / "items" / "baubles" / "ItemCloak.java"),
        "ItemClusters": sub_names(SOURCE / "items" / "ItemClusters.java"),
    }
    interpreter = Interpreter(method_body(text, "registerResearch"), constants)
    interpreter.run_block(False)
    return interpreter.entries


def main():
    entries = collect()
    keys = [entry["key"] for entry in entries]
    duplicates = sorted({key for key in keys if keys.count(key) > 1})
    if duplicates:
        raise SystemExit("duplicate research keys: " + ", ".join(duplicates))
    OUTPUT.write_text(json.dumps(entries, indent=2, sort_keys=True, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("entries:", len(entries), "fake:", sum(1 for entry in entries if entry["fake"]))


if __name__ == "__main__":
    main()
