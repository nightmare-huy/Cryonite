#!/usr/bin/env python3
"""Writes the empty 9x7x9 structure used by the GameTests (data/cryonite/structure/empty.nbt)."""
import gzip
import os
import struct

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "data", "cryonite", "structure", "empty.nbt")
SIZE = (9, 7, 9)
DATA_VERSION = 3955  # Minecraft 1.21.1

END, INT, STRING, LIST, COMPOUND = 0, 3, 8, 9, 10


def name(text):
    raw = text.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def tag(kind, key, payload):
    return bytes([kind]) + name(key) + payload


def int_list(values):
    return bytes([INT]) + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def compound_list(compounds):
    return bytes([COMPOUND if compounds else END]) + struct.pack(">i", len(compounds)) + b"".join(c + bytes([END]) for c in compounds)


air = tag(STRING, "Name", name("minecraft:air"))
block = tag(LIST, "pos", int_list([0, 0, 0])) + tag(INT, "state", struct.pack(">i", 0))

root = (
    tag(INT, "DataVersion", struct.pack(">i", DATA_VERSION))
    + tag(LIST, "size", int_list(list(SIZE)))
    + tag(LIST, "palette", compound_list([air]))
    + tag(LIST, "blocks", compound_list([block]))
    + tag(LIST, "entities", compound_list([]))
)
data = bytes([COMPOUND]) + name("") + root + bytes([END])

os.makedirs(os.path.dirname(OUT), exist_ok=True)
with gzip.GzipFile(OUT, "wb", mtime=0) as handle:
    handle.write(data)
print("written", os.path.normpath(OUT), len(data), "bytes")
