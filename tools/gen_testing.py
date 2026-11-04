import gzip
import struct

TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 3, 8, 9, 10


class Int(int):
    pass


class TagList(list):
    def __init__(self, items, kind):
        super().__init__(items)
        self.kind = kind


def tag_type(value):
    if isinstance(value, Int):
        return TAG_INT
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, TagList):
        return TAG_LIST
    if isinstance(value, dict):
        return TAG_COMPOUND
    raise TypeError(type(value))


def write_string(out, value):
    encoded = value.encode("utf-8")
    out.append(struct.pack(">H", len(encoded)))
    out.append(encoded)


def write_payload(out, value):
    kind = tag_type(value)
    if kind == TAG_INT:
        out.append(struct.pack(">i", value))
    elif kind == TAG_STRING:
        write_string(out, value)
    elif kind == TAG_LIST:
        out.append(struct.pack(">bi", value.kind if value else 0, len(value)))
        for item in value:
            write_payload(out, item)
    else:
        for key, item in value.items():
            out.append(struct.pack(">b", tag_type(item)))
            write_string(out, key)
            write_payload(out, item)
        out.append(b"\x00")


def to_nbt(root):
    out = [struct.pack(">b", TAG_COMPOUND)]
    write_string(out, "")
    write_payload(out, root)
    return gzip.compress(b"".join(out), mtime=0)


def platform(ctx, size=(7, 7, 7)):
    blocks = [
        {"pos": TagList([Int(x), Int(0), Int(z)], TAG_INT), "state": Int(0)}
        for x in range(size[0])
        for z in range(size[2])
    ]
    return {
        "DataVersion": Int(ctx.profile.data_version),
        "size": TagList([Int(value) for value in size], TAG_INT),
        "palette": TagList([{"Name": "minecraft:stone"}], TAG_COMPOUND),
        "blocks": TagList(blocks, TAG_COMPOUND),
        "entities": TagList([], TAG_COMPOUND),
    }


def generate(ctx):
    path = ctx.DATA / ctx.NS / ctx.profile.structure_folder / "test" / "platform.nbt"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(to_nbt(platform(ctx)))
