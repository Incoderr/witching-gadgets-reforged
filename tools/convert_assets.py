import argparse
import codecs
import json
import re
import shutil
from collections import Counter, defaultdict
from pathlib import Path

codecs.register_error("cp1252_fallback", lambda error: (error.object[error.start:error.end].decode("cp1252"), error.end))
BOM = b"\xef\xbb\xbf"
ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "wg-src" / "src" / "main" / "resources" / "assets" / "witchinggadgets"
NS = "witchinggadgets"
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / NS
LANG_EXTRA = ROOT / "tools" / "lang"
ASSETS_EXTRA = ROOT / "tools" / "assets_extra"

TEXTURE_DIRS = {
    "blocks": "block",
    "items": "item",
    "models": "models",
    "gui": "gui",
}

FORMAT_PATTERN = re.compile(r"%(?:(\d+)\$)?([A-Za-z%]|$)")
INVALID_PATH = re.compile(r"[^a-z0-9_.\-/]")
RESEARCH_NAME = re.compile(r"^witchinggadgets_research_name\.(.+)$")
RESEARCH_TEXT = re.compile(r"^witchinggadgets_research_text\.(.+)$")
RESEARCH_PAGE = re.compile(r"^witchinggadgets_research_page\.(.+)$")
ENCHANTMENT = re.compile(r"^enchantment\.wg\.(\w+)$")
PASS_THROUGH = re.compile(r"^(tc\.research_category\.|itemGroup\.witchinggadgets$|wg\.)")
UNMAPPED_PREFIX = "witchinggadgets."
UNQUOTED_KEY = re.compile(r"([{,]\s*)([A-Za-z_]\w*)\s*:")

report = {
    "renamed": [],
    "warnings": [],
}


def warn(message):
    report["warnings"].append(message)
    print(f"warning: {message}")


def clean_name(name):
    cleaned = INVALID_PATH.sub("_", name.lower().replace(" - ", "_"))
    return re.sub(r"_+", "_", cleaned)


def snake_case(name):
    return re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name).lower()


def remove(path):
    if path.is_dir():
        shutil.rmtree(path)
    elif path.exists():
        path.unlink()


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def clean_targets():
    for name in ("textures", "lang", "obj"):
        remove(ASSETS / name)


def convert_mcmeta(source, destination):
    text = source.read_text(encoding="utf-8")
    try:
        data = json.loads(text)
    except json.JSONDecodeError:
        data = json.loads(UNQUOTED_KEY.sub(r'\1"\2":', text))
        report["renamed"].append(f"{source.name}: unquoted keys quoted")
    write_json(destination, data)


def convert_textures():
    counts = Counter()
    for old_dir, new_dir in TEXTURE_DIRS.items():
        source_root = SOURCE / "textures" / old_dir
        if not source_root.exists():
            continue
        for path in sorted(source_root.rglob("*")):
            if path.is_dir() or path.name == ".DS_Store":
                continue
            relative = path.relative_to(source_root).as_posix()
            name = clean_name(relative)
            if name != relative:
                report["renamed"].append(f"textures/{new_dir}/{relative} -> {name}")
            destination = ASSETS / "textures" / new_dir / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            if path.suffix == ".mcmeta":
                convert_mcmeta(path, destination)
                counts[f"{new_dir}/mcmeta"] += 1
            elif path.suffix == ".png":
                shutil.copyfile(path, destination)
                counts[f"{new_dir}/png"] += 1
            else:
                warn(f"unexpected texture file skipped: {path}")
    for path in sorted((SOURCE / "textures").glob("*.*")):
        warn(f"texture outside a known folder skipped: {path.name}")
    return counts


def convert_obj_models():
    count = 0
    for path in sorted((SOURCE / "models").glob("*.obj")):
        target = ASSETS / "obj" / f"{clean_name(snake_case(path.stem))}.obj"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, target)
        count += 1
    return count


def load_id_map(path):
    if not path.exists():
        warn(f"id map not found: {path}, tile and item keys are not renamed")
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def lang_target(kind, entry):
    target = f"{kind}.{NS}.{entry['id']}"
    if entry.get("variant"):
        target += f".{entry['variant']}"
    return target


def build_key_map(id_map):
    key_map = defaultdict(list)
    if id_map is None:
        return key_map
    block_ids = set()
    for entry in id_map["blocks"]:
        if entry.get("skip") or not entry.get("lang"):
            continue
        target = lang_target("block", entry)
        if target not in key_map[entry["lang"]]:
            key_map[entry["lang"]].append(target)
        block_ids.add(entry["id"])
    for entry in id_map["items"]:
        if entry.get("skip") or not entry.get("lang") or entry["id"] in block_ids:
            continue
        target = lang_target("item", entry)
        if target not in key_map[entry["lang"]]:
            key_map[entry["lang"]].append(target)
    return key_map


def skipped_keys(id_map):
    if id_map is None:
        return set()
    return {
        entry["lang"]
        for entry in id_map["blocks"] + id_map["items"]
        if entry.get("skip") and entry.get("lang")
    }


def unescape(value):
    return re.sub(r"\\(n|\\)", lambda match: "\n" if match.group(1) == "n" else "\\", value)


def escape_format(value, stats):
    def replace(match):
        index, kind = match.group(1), match.group(2)
        if kind in ("s", "%"):
            return match.group(0)
        stats["escaped"] += 1
        if kind == "d":
            return f"%{index}$s" if index else "%s"
        return "%%" + (f"{index}$" if index else "") + kind

    return FORMAT_PATTERN.sub(replace, value)


def decode(data, path):
    if data.startswith(BOM):
        data = data[len(BOM):]
    text = data.decode("utf-8", "cp1252_fallback")
    if text != data.decode("utf-8", "replace"):
        report["renamed"].append(f"lang/{path.name}: cp1252 bytes decoded")
    return text


def read_lang(path):
    text = decode(path.read_bytes(), path)
    entries = {}
    for number, line in enumerate(text.splitlines(), 1):
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "=" not in line:
            warn(f"{path.name}:{number} line without '=': {stripped[:40]}")
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = unescape(value)
        if key in entries and entries[key] != value:
            warn(f"{path.name}:{number} duplicate key {key}, last wins")
        entries[key] = value
    return entries


def research_key(key):
    for pattern, template in (
        (RESEARCH_NAME, "tc.research_name.{}"),
        (RESEARCH_TEXT, "tc.research_text.{}"),
        (RESEARCH_PAGE, "tc.research_page.{}"),
    ):
        match = pattern.match(key)
        if match:
            return template.format(match.group(1))
    return None


def convert_image_links(value):
    def replace(match):
        directory, separator, name = match.group(1).partition("/")
        path = f"{TEXTURE_DIRS[directory]}/{clean_name(name)}" if separator and directory in TEXTURE_DIRS else clean_name(match.group(1))
        return f"<IMG>{NS}:textures/{path}{match.group(2)}</IMG>"

    return re.sub(r"<IMG>witchinggadgets:textures/([^:]+)(:[^<]*)</IMG>", replace, value)


def languages():
    names = {path.stem.lower(): path for path in sorted((SOURCE / "lang").glob("*.lang"))}
    extra = {path.stem for path in LANG_EXTRA.glob("*.json")} | {path.stem for path in LANG_EXTRA.glob("*/*.json")}
    return [(language, names.get(language)) for language in sorted(set(names) | extra)]


def convert_lang(id_map):
    key_map = build_key_map(id_map)
    dropped = skipped_keys(id_map)
    used_keys = set()
    results = {}
    mapped = Counter()
    unmapped = defaultdict(Counter)
    unmapped_keys = defaultdict(list)
    escape_stats = Counter()
    for language, path in languages():
        result = {}
        source = read_lang(path) if path else {}
        for key, value in source.items():
            value = escape_format(value, escape_stats)
            if key in key_map:
                used_keys.add(key)
                for target in key_map[key]:
                    result[target] = value
                mapped[language, "id_map"] += 1
                continue
            if key in dropped:
                mapped[language, "skipped"] += 1
                continue
            target = research_key(key)
            if target is None:
                enchantment = ENCHANTMENT.match(key)
                if enchantment:
                    target = f"enchantment.{NS}.{snake_case(enchantment.group(1))}"
            if target is None and PASS_THROUGH.match(key):
                target = key
            if target is not None:
                result[target] = value
                mapped[language, "rewritten"] += 1
                continue
            result[UNMAPPED_PREFIX + key] = value
            unmapped[language][key.split(".")[0]] += 1
            unmapped_keys[language].append(key)
        extra_paths = [LANG_EXTRA / f"{language}.json", *sorted(LANG_EXTRA.glob(f"*/{language}.json"))]
        for extra_path in extra_paths:
            if extra_path.exists():
                result.update(json.loads(extra_path.read_text(encoding="utf-8")))
        result = {key: convert_image_links(value) for key, value in result.items()}
        results[language] = result
        write_json(ASSETS / "lang" / f"{language}.json", dict(sorted(result.items())))
    uncovered = []
    if id_map is not None:
        uncovered = sorted(
            entry["lang"]
            for entry in id_map["blocks"] + id_map["items"]
            if entry.get("lang") and not entry.get("skip") and entry["lang"] not in used_keys
        )
    return results, mapped, unmapped, unmapped_keys, uncovered, escape_stats


def validate(results):
    for path in ASSETS.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8"))
    for language, entries in results.items():
        for key, value in entries.items():
            if any(match.group(2) not in ("s", "%") for match in FORMAT_PATTERN.finditer(value)):
                warn(f"{language}: bad format in {key}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--id-map", default=str(ROOT / "tools" / "id_map.json"))
    arguments = parser.parse_args()

    if not SOURCE.exists():
        raise SystemExit("reference/wg-src is missing, run tools/fetch_wg.py first")
    clean_targets()
    texture_counts = convert_textures()
    obj_count = convert_obj_models()
    id_map = load_id_map(Path(arguments.id_map))
    results, mapped, unmapped, unmapped_keys, uncovered, escape_stats = convert_lang(id_map)
    if ASSETS_EXTRA.exists():
        shutil.copytree(ASSETS_EXTRA, ASSETS, dirs_exist_ok=True)
    validate(results)

    print("\n== textures ==")
    for category, count in sorted(texture_counts.items()):
        print(f"{category}: {count}")
    print(f"obj models: {obj_count}")
    print("\n== lang ==")
    for language, entries in sorted(results.items()):
        rewritten = mapped[language, "rewritten"]
        by_map = mapped[language, "id_map"]
        skipped = mapped[language, "skipped"]
        left = sum(unmapped[language].values())
        print(f"{language}: {len(entries)} keys (id_map {by_map}, rewritten {rewritten}, skipped {skipped}, unmapped {left})")
    print(f"format specifiers escaped: {escape_stats['escaped']}")
    print(f"\n== unmapped en_us keys kept under {UNMAPPED_PREFIX} ==")
    for prefix, count in unmapped["en_us"].most_common():
        print(f"{prefix}: {count}")
    print("sample:", ", ".join(unmapped_keys["en_us"][:8]))
    print("\n== id_map lang keys without source entry ==")
    for key in uncovered:
        print(key)
    print(f"total: {len(uncovered)}")
    print("\n== renamed ==")
    for line in report["renamed"][:15]:
        print(line)
    print(f"total: {len(report['renamed'])}")
    print(f"\nwarnings: {len(report['warnings'])}")


if __name__ == "__main__":
    main()
