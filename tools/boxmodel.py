def uv(texture_size, u1, v1, u2, v2):
    scale_u = 16 / texture_size[0]
    scale_v = 16 / texture_size[1]
    return [round(u1 * scale_u, 4), round(v1 * scale_v, 4), round(u2 * scale_u, 4), round(v2 * scale_v, 4)]


def faces(u, v, size, texture_size, texture):
    width, height, depth = size

    def face(u1, v1, u2, v2):
        return {"uv": uv(texture_size, u1, v1, u2, v2), "texture": texture}

    return {
        "east": face(u + depth + width, v + depth + height, u + depth + width + depth, v + depth),
        "west": face(u, v + depth + height, u + depth, v + depth),
        "down": face(u + depth, v, u + depth + width, v + depth),
        "up": face(u + depth + width, v + depth, u + depth + width + width, v),
        "north": face(u + depth, v + depth + height, u + depth + width, v + depth),
        "south": face(u + depth + width + depth, v + depth + height, u + depth + width + depth + width, v + depth),
    }


def unflipped(u, v, offset, size, origin, texture_size, texture, rotation=None):
    start = [round(origin[axis] + offset[axis], 4) for axis in range(3)]
    end = [round(start[axis] + size[axis], 4) for axis in range(3)]
    content = {"from": start, "to": end, "faces": faces(u, v, size, texture_size, texture)}
    if rotation:
        content["rotation"] = {"origin": [round(value, 4) for value in origin], "axis": rotation[0], "angle": rotation[1]}
    return content
