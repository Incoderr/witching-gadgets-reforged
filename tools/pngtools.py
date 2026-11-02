import struct
import zlib


def read_rgba(path):
    data = path.read_bytes()
    width, height, depth, color_type, _, _, interlace = struct.unpack(">IIBBBBB", data[16:29])
    if depth != 8 or color_type != 6 or interlace:
        raise ValueError(f"{path}: only 8-bit RGBA non-interlaced PNG is supported")
    position = 8
    compressed = b""
    while position < len(data):
        length, kind = struct.unpack(">I4s", data[position:position + 8])
        if kind == b"IDAT":
            compressed += data[position + 8:position + 8 + length]
        position += 12 + length
    raw = zlib.decompress(compressed)
    stride = width * 4
    rows = []
    previous = bytearray(stride)
    offset = 0
    for _ in range(height):
        kind = raw[offset]
        line = bytearray(raw[offset + 1:offset + 1 + stride])
        offset += 1 + stride
        for index in range(stride):
            left = line[index - 4] if index >= 4 else 0
            up = previous[index]
            corner = previous[index - 4] if index >= 4 else 0
            if kind == 1:
                line[index] = (line[index] + left) & 0xFF
            elif kind == 2:
                line[index] = (line[index] + up) & 0xFF
            elif kind == 3:
                line[index] = (line[index] + (left + up) // 2) & 0xFF
            elif kind == 4:
                estimate = left + up - corner
                distances = (abs(estimate - left), abs(estimate - up), abs(estimate - corner))
                predictor = left if distances[0] <= distances[1] and distances[0] <= distances[2] else up if distances[1] <= distances[2] else corner
                line[index] = (line[index] + predictor) & 0xFF
        rows.append(line)
        previous = line
    return width, height, rows


def write_rgba(path, width, height, rows):
    def chunk(kind, body):
        return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body) & 0xFFFFFFFF)

    raw = b"".join(b"\x00" + bytes(row) for row in rows)
    content = b"\x89PNG\r\n\x1a\n"
    content += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    content += chunk(b"IDAT", zlib.compress(raw, 9))
    content += chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(content)


def desaturate(source, target):
    width, height, rows = read_rgba(source)
    for row in rows:
        for index in range(0, len(row), 4):
            gray = (row[index] * 299 + row[index + 1] * 587 + row[index + 2] * 114) // 1000
            row[index] = row[index + 1] = row[index + 2] = gray
    write_rgba(target, width, height, rows)
