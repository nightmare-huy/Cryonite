#!/usr/bin/env python3
"""Generates every texture of the Cryonite mod (items, blocks, armor layers, GUI).

Usage:  python tools/gen_textures.py        (needs Pillow)

All art is drawn procedurally from the ASCII templates and palettes below, so the
textures can be re-generated or re-coloured at any time. Output goes to
src/main/resources/assets/cryonite/textures.
"""
import os
import random
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "cryonite", "textures")


def rgb(hexa):
    hexa = hexa.lstrip("#")
    return tuple(int(hexa[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


# --------------------------------------------------------------------------- palettes
# o = outline, d = dark, m = mid, l = light, h = highlight
TIERS = {
    "cryonite": dict(o="#0a2742", d="#1b5a8c", m="#35a3d4", l="#86e0f4", h="#e8fdff"),
    "infernite": dict(o="#2e0703", d="#8a1a08", m="#dc4a16", l="#ff9b2c", h="#ffe88f"),
    "void": dict(o="#07030f", d="#27104d", m="#57279c", l="#9858e2", h="#e2bcff"),
    "aether": dict(o="#6b5318", d="#c9a53c", m="#f4dc7c", l="#fff7cf", h="#ffffff"),
}
# S = handle dark, s = handle mid, t = handle light, O = handle outline
HANDLES = {
    "cryonite": dict(O="#2a1d09", S="#4f3a17", s="#866626", t="#ab873c"),
    "infernite": dict(O="#0c0b0e", S="#1f1d24", s="#34313c", t="#4e4a58"),
    "void": dict(O="#05030a", S="#150d22", s="#2a1d40", t="#44335f"),
    "aether": dict(O="#5d6470", S="#a9b3c0", s="#dfe6ee", t="#ffffff"),
}
STONE = dict(A="#b9b9c2", B="#8a8a96", C="#55555f", o="#26262c")


def palette(tier):
    pal = {k: rgb(v) for k, v in TIERS[tier].items()}
    pal.update({k: rgb(v) for k, v in HANDLES[tier].items()})
    pal.update({k: rgb(v) for k, v in STONE.items() if k != "o"})
    return pal


# --------------------------------------------------------------------------- templates (inner pixels, outline is added automatically)
SWORD = [
    "................",
    ".............hl.",
    "............hlm.",
    "...........hlm..",
    "..........hlm...",
    ".........hlm....",
    "........hlm.....",
    ".......hlm......",
    "...d..hlm.......",
    "....dhlm........",
    ".....dm.........",
    "....s.d.........",
    "...sS..d........",
    "..sS............",
    ".d..............",
    "................",
]
PICKAXE = [
    "................",
    "................",
    "....hhhlll......",
    "......lmmmll....",
    ".........sdml...",
    "........st.dml..",
    ".......st...ml..",
    "......st....mm..",
    ".....st.....md..",
    "....st.......d..",
    "...st...........",
    "..st............",
    ".sS.............",
    "................",
    "................",
    "................",
]
AXE = [
    "................",
    ".....hhll.......",
    "....hlllmm.t....",
    "....hllmmmts....",
    "....hlmmmts.....",
    "....lmmdts......",
    ".....mdts.......",
    "......ts........",
    ".....ts.........",
    "....ts..........",
    "...ts...........",
    "..ts............",
    ".sS.............",
    "................",
    "................",
    "................",
]
SHOVEL = [
    "................",
    "..........hll...",
    ".........hllmm..",
    ".........llmmm..",
    ".........lmmmd..",
    "........ts.dd...",
    ".......ts.......",
    "......ts........",
    ".....ts.........",
    "....ts..........",
    "...ts...........",
    "..ts............",
    ".sS.............",
    "................",
    "................",
    "................",
]
HOE = [
    "................",
    ".......hhlll....",
    "......hlmmmts...",
    "......lm..ts....",
    ".........ts.....",
    "........ts......",
    ".......ts.......",
    "......ts........",
    ".....ts.........",
    "....ts..........",
    "...ts...........",
    "..ts............",
    ".sS.............",
    "................",
    "................",
    "................",
]
HELMET = [
    "................",
    "................",
    "................",
    "....hhhlllmm....",
    "...hhlllllmmm...",
    "...hllllmmmmd...",
    "...hllmmmmmmd...",
    "...llm....mmd...",
    "...lmm....mdd...",
    "...lm......dd...",
    "...md......dd...",
    "................",
    "................",
    "................",
    "................",
    "................",
]
CHESTPLATE = [
    "................",
    "................",
    "..hhl......lmm..",
    "..hllm....lmmd..",
    "..hllllllmmmmd..",
    "..llllllmmmmmd..",
    "..lm.lllmmm.md..",
    ".....hllmmm.....",
    ".....hlmmmm.....",
    ".....llmmmd.....",
    ".....lmmmmd.....",
    ".....lmmmdd.....",
    ".....mmmddd.....",
    "................",
    "................",
    "................",
]
LEGGINGS = [
    "................",
    "................",
    "....hhllllmm....",
    "....hllllmmm....",
    "....hllmmmmd....",
    "....llm..mmd....",
    "....llm..mmd....",
    "....lm....md....",
    "....lm....md....",
    "....lm....md....",
    "....lm....md....",
    "....mm....dd....",
    "....md....dd....",
    "................",
    "................",
    "................",
]
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "...hll...llm....",
    "...hlm...lmm....",
    "...llm...lmd....",
    "...lmm...mmd....",
    "..hlmm..llmmd...",
    "..llmd..lmmdd...",
    "..mmdd..mmddd...",
    "................",
    "................",
    "................",
    "................",
]
INGOT = [
    "................",
    "................",
    "................",
    "................",
    ".......hhhhhh...",
    "......hlllllm...",
    ".....hllllmmd...",
    "....hllllmmdd...",
    "...lllllmmmdd...",
    "...mmmmmmmdd....",
    "...mmmmmddd.....",
    "...ddddddd......",
    "................",
    "................",
    "................",
    "................",
]
RAW = [
    "................",
    "................",
    "................",
    "......hll.......",
    ".....hllmm.h....",
    "....hllmmmlm....",
    "...hllmmdmmmd...",
    "...llmmdmmlmd...",
    "...lmmdmmlmmd...",
    "....mmdmmmmdd...",
    "....mddmmmdd....",
    ".....dd.ddd.....",
    "................",
    "................",
    "................",
    "................",
]
DUST = [
    "................",
    "................",
    "................",
    "...........h....",
    "................",
    "....l...........",
    ".......hl.......",
    "......hllm..h...",
    "....hhllmmmlm...",
    "...hllmlmmmmmd..",
    "..hllmmmlmmmdmd.",
    "..lmmdmmmmddmdd.",
    "...mdddmdddddd..",
    "................",
    "................",
    "................",
]
SHARD = [
    "................",
    "................",
    "..........hh....",
    ".........hllm...",
    "........hllmm...",
    ".......hllmmd...",
    "......hllmmd....",
    ".....hllmmd.....",
    "....hllmmdd.....",
    "....llmmdd.lm...",
    "...lmmmdd..md...",
    "...mmdd.........",
    "...dd...........",
    "................",
    "................",
    "................",
]
CLUSTER = [
    "................",
    "................",
    ".......h........",
    "......hlm.......",
    "......hlm..h....",
    "...h..hlm.hlm...",
    "..hlm.hlmmhlm...",
    "..hlm.hlmdhlm...",
    "..hlmhllmdlmd...",
    "..hlmllmmdlmd...",
    "..llmlmmddmmd...",
    "...mmmmmdddd....",
    "....dddddd......",
    "................",
    "................",
    "................",
]
BLANK = [
    "................",
    "................",
    "................",
    "................",
    ".....hhhhll.....",
    "....hllllllm....",
    "...hlllllmmmd...",
    "...hllllmmmmd...",
    "...lllmmmmmdd...",
    "...llmmmmmddd...",
    "....mmmmdddd....",
    ".....dddddd.....",
    "................",
    "................",
    "................",
    "................",
]
ORB = [
    "................",
    "................",
    "................",
    "......hhll......",
    "....hhllllmm....",
    "....hlllmmmm....",
    "...hlllmmmmmd...",
    "...hllmmmmmdd...",
    "...llmmmmmddd...",
    "...lmmmmmdddd...",
    "....mmmmdddd....",
    "....mmdddddd....",
    "......dddd......",
    "................",
    "................",
    "................",
]
TEMPLATE = [
    "................",
    "....AAAAAAAA....",
    "...AABBBBBBBC...",
    "...ABBBhlBBBC...",
    "...ABBhllmBBC...",
    "...ABhllmmdBC...",
    "...ABBlmmdBBC...",
    "...ABBBmdBBBC...",
    "...ABBBBBBBBC...",
    "...ABlBBBBmBC...",
    "...ABBlmmdBBC...",
    "...ABBBBBBBBC...",
    "...BCCCCCCCCC...",
    "....CCCCCCCC....",
    "................",
    "................",
]

HANDLE_CHARS = "Sst"
STONE_CHARS = "ABC"


def render(template, pal, outline=True, overrides=None):
    """Turns an ASCII template into a 16x16 RGBA image and adds a 1px outline around it."""
    assert len(template) == 16 and all(len(row) == 16 for row in template), template
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(template):
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = (overrides or {}).get(ch, pal[ch])
    if outline:
        stone_outline = rgb(STONE["o"])
        for y in range(16):
            for x in range(16):
                if template[y][x] != ".":
                    continue
                neighbours = [template[ny][nx] for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))
                              if 0 <= nx < 16 and 0 <= ny < 16 and template[ny][nx] != "."]
                if not neighbours:
                    continue
                if any(n not in HANDLE_CHARS + STONE_CHARS for n in neighbours):
                    px[x, y] = pal["o"]
                elif any(n in STONE_CHARS for n in neighbours):
                    px[x, y] = stone_outline
                else:
                    px[x, y] = pal["O"]
    return img


def save(img, *path):
    full = os.path.join(ROOT, *path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    img.save(full)


def put(img, pixels, color):
    px = img.load()
    for x, y in pixels:
        px[x, y] = color


# --------------------------------------------------------------------------- items
def items():
    for tier in TIERS:
        pal = palette(tier)
        for name, template in (("sword", SWORD), ("pickaxe", PICKAXE), ("axe", AXE), ("shovel", SHOVEL), ("hoe", HOE),
                               ("helmet", HELMET), ("chestplate", CHESTPLATE), ("leggings", LEGGINGS), ("boots", BOOTS)):
            img = render(template, pal)
            if tier == "aether":
                # a cyan spark on every Aether piece
                spark = rgb("#8ff2ff")
                spots = {"sword": [(10, 5), (12, 3)], "pickaxe": [(7, 2), (12, 6)], "axe": [(6, 3)], "shovel": [(11, 3)],
                         "hoe": [(9, 2)], "helmet": [(7, 5), (8, 5)], "chestplate": [(7, 9), (8, 9)],
                         "leggings": [(7, 3), (8, 3)], "boots": [(4, 8), (10, 8)]}[name]
                put(img, spots, spark)
            if tier == "void":
                star = rgb("#f3e3ff")
                spots = {"sword": [(11, 4)], "pickaxe": [(8, 3)], "axe": [(7, 3)], "shovel": [(11, 3)], "hoe": [(9, 2)],
                         "helmet": [(6, 5)], "chestplate": [(7, 8)], "leggings": [(6, 3)], "boots": [(4, 7)]}[name]
                put(img, spots, star)
            save(img, "item", f"{tier}_{name}.png")
        save(render(INGOT, pal), "item", f"{tier}_ingot.png")
        if tier != "aether":
            save(render(TEMPLATE, pal), "item", f"{tier}_upgrade_smithing_template.png")

    cry, inf, void, aeth = (palette(t) for t in ("cryonite", "infernite", "void", "aether"))

    save(render(RAW, cry), "item", "raw_cryonite.png")
    save(render(DUST, cry), "item", "cryonite_crystal_dust.png")

    # molten shard: glowing core
    img = render(SHARD, inf)
    put(img, [(9, 4), (8, 5), (7, 6), (6, 7), (5, 8)], rgb("#fff6c0"))
    save(img, "item", "molten_infernite_shard.png")
    # cooled blank: dark crust with dim red seams
    crust = dict(o=rgb("#120807"), d=rgb("#2a1512"), m=rgb("#4a2a22"), l=rgb("#6e4435"), h=rgb("#96644c"))
    img = render(BLANK, crust)
    put(img, [(6, 6), (7, 7), (8, 7), (9, 8), (5, 8), (6, 9)], rgb("#c2401a"))
    save(img, "item", "cooled_infernite_blank.png")

    # unstable shard: flickering white fractures; stabilized cluster: clean crystal
    img = render(SHARD, void)
    put(img, [(9, 3), (7, 5), (8, 6), (5, 8), (6, 9), (11, 9)], rgb("#ff7bf0"))
    put(img, [(13, 6), (2, 7), (12, 12)], rgb("#c06bff"))
    save(img, "item", "unstable_void_shard.png")
    save(render(CLUSTER, void), "item", "stabilized_void_cluster.png")
    # molten void blank: purple billet with hot magenta veins
    img = render(BLANK, void)
    put(img, [(6, 6), (7, 6), (8, 7), (9, 7), (5, 8), (6, 9), (10, 8)], rgb("#ff8cf5"))
    put(img, [(7, 7), (9, 8)], rgb("#ffffff"))
    save(img, "item", "molten_void_blank.png")

    # inactive aether: dull orb showing the four ingots it was made of
    dull = dict(o=rgb("#1d1d22"), d=rgb("#44444e"), m=rgb("#6b6b78"), l=rgb("#9a9aa8"), h=rgb("#c9c9d4"))
    img = render(ORB, dull)
    put(img, [(5, 6), (6, 5)], rgb("#5a5560"))                # netherite
    put(img, [(9, 5), (10, 6)], cry["m"])
    put(img, [(5, 9), (6, 10)], inf["m"])
    put(img, [(10, 9), (9, 10)], void["l"])
    save(img, "item", "inactive_aether.png")

    # aether ingot gets extra sparkles
    img = render(INGOT, aeth)
    put(img, [(8, 6), (6, 8)], rgb("#8ff2ff"))
    put(img, [(13, 3), (2, 12)], rgb("#fff7cf"))
    save(img, "item", "aether_ingot.png")


# --------------------------------------------------------------------------- blocks
def noise_tile(base, spread, seed, streaks=False):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        row_shift = rnd.randint(-spread // 2, spread // 2) if streaks else 0
        for x in range(16):
            delta = rnd.randint(-spread, spread) + row_shift
            px[x, y] = tuple(max(0, min(255, c + delta)) for c in base[:3]) + (255,)
    return img


ORE_BLOBS = [
    [(2, 2), (3, 2), (3, 3), (4, 3), (2, 3)],
    [(10, 1), (11, 1), (11, 2), (12, 2), (12, 3)],
    [(6, 6), (7, 6), (8, 6), (7, 7), (8, 7), (9, 7), (7, 5)],
    [(1, 9), (2, 9), (2, 10), (3, 10)],
    [(12, 9), (13, 9), (13, 10), (12, 10), (11, 10)],
    [(5, 12), (6, 12), (6, 13), (7, 13), (5, 13)],
    [(10, 13), (11, 13), (11, 14)],
]


def ore(base_img, pal, seed, glow=False):
    rnd = random.Random(seed)
    px = base_img.load()
    for blob in ORE_BLOBS:
        for i, (x, y) in enumerate(blob):
            shade = "hlmmd"[min(i, 4)] if not glow else "hllmm"[min(i, 4)]
            px[x, y] = pal[shade]
        # shadow under the crystal
        bx, by = blob[-1]
        if by + 1 < 16:
            px[bx, by + 1] = mix(px[bx, by + 1], (0, 0, 0, 255), 0.45)
    return base_img


def bevel_block(pal, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge == 0:
                color = pal["l"] if (x == 0 or y == 0) else pal["d"]
                if (x == 0 and y == 15) or (x == 15 and y == 0):
                    color = pal["m"]
            elif edge == 1:
                color = pal["h"] if (x == 1 or y == 1) and x < 14 and y < 14 else pal["d"]
            else:
                t = (x + y) / 30.0
                color = mix(pal["l"], pal["m"], min(1.0, t * 1.4))
                if rnd.random() < 0.12:
                    color = mix(color, pal["d"], 0.35)
                if (x - y) % 7 == 0 and 3 <= x <= 12:
                    color = mix(color, pal["h"], 0.5)
            px[x, y] = color
    return img


def smelter_side(seed, top=False):
    rnd = random.Random(seed)
    dark, mid, light, trim = rgb("#17131f"), rgb("#2a2436"), rgb("#3d3550"), rgb("#57506a")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            color = mix(mid, light, rnd.random() * 0.5)
            # brick seams
            if y % 5 == 4 or (x + (4 if (y // 5) % 2 else 0)) % 8 == 7:
                color = dark
            px[x, y] = color
    # netherite frame
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = trim if (i % 4) else rgb("#7a7290")
    for (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
        px[x, y] = rgb("#0e0b14")
    if top:
        # grate with a cold-blue core
        for y in range(4, 12):
            for x in range(4, 12):
                px[x, y] = rgb("#0b0910") if (x + y) % 2 == 0 else rgb("#221d2d")
        for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
            px[x, y] = rgb("#2f6f9c")
        for i in range(3, 13):
            for (x, y) in ((i, 3), (i, 12), (3, i), (12, i)):
                px[x, y] = rgb("#6b6380")
    return img


def smelter_front(lit):
    img = smelter_side(11)
    px = img.load()
    frame = rgb("#6b6380")
    # mouth
    for y in range(8, 14):
        for x in range(3, 13):
            px[x, y] = frame if (x in (3, 12) or y in (8, 13)) else rgb("#07060b")
    # upper vents
    for x in (4, 6, 8, 10):
        for y in (3, 4, 5):
            px[x, y] = rgb("#07060b")
            px[x + 1, y] = frame
    if lit:
        fire = [rgb("#fff3b0"), rgb("#ffb23c"), rgb("#f0601c"), rgb("#3fd0ff"), rgb("#1b78c8")]
        pattern = [
            "33.44.33",
            "3003.300",
            "01101101",
            "12211221",
        ]
        for dy, row in enumerate(pattern):
            for dx, ch in enumerate(row):
                if ch != ".":
                    px[4 + dx, 9 + dy] = fire[int(ch)]
        for x in (4, 6, 8, 10):
            px[x, 4] = rgb("#ff8a2a")
            px[x, 5] = rgb("#3fd0ff")
            px[x, 3] = rgb("#7a2a10")
    else:
        for (x, y) in ((5, 12), (6, 12), (9, 12), (10, 12), (7, 11)):
            px[x, y] = rgb("#1c1924")
    return img


def blocks():
    cry, inf, void = (palette(t) for t in ("cryonite", "infernite", "void"))
    save(ore(noise_tile(rgb("#3d3d43"), 9, 1, streaks=True), cry, 1), "block", "cryonite_ore.png")
    save(ore(noise_tile(rgb("#2a2428"), 8, 2, streaks=True), inf, 2, glow=True), "block", "infernite_ore.png")
    save(ore(noise_tile(rgb("#dbdea0"), 10, 3), void, 3), "block", "void_ore.png")
    save(bevel_block(inf, 4), "block", "infernite_block.png")
    save(smelter_side(10), "block", "primordial_smelter_side.png")
    save(smelter_side(12, top=True), "block", "primordial_smelter_top.png")
    save(smelter_front(False), "block", "primordial_smelter_front.png")
    save(smelter_front(True), "block", "primordial_smelter_front_on.png")


# --------------------------------------------------------------------------- armor layers (64x32)
def shade_rect(px, x0, y0, x1, y1, pal, rnd, trim=True):
    """Fills a box face with a top-lit metal gradient and a darker rim."""
    h = max(1, y1 - y0 - 1)
    for y in range(y0, y1):
        for x in range(x0, x1):
            t = (y - y0) / h
            color = mix(pal["l"], pal["m"], t) if t < 0.5 else mix(pal["m"], pal["d"], (t - 0.5) * 1.4)
            if rnd.random() < 0.10:
                color = mix(color, pal["h"], 0.35)
            if trim and (x in (x0, x1 - 1) or y in (y0, y1 - 1)):
                color = mix(color, pal["o"], 0.55)
            px[x, y] = color


def armor():
    for tier in TIERS:
        pal = palette(tier)
        rnd = random.Random(sum(map(ord, tier)))
        accent = {"cryonite": rgb("#e8fdff"), "infernite": rgb("#ffe88f"), "void": rgb("#ff7bf0"), "aether": rgb("#8ff2ff")}[tier]

        # ---- layer 1: helmet, chestplate, boots
        img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        px = img.load()
        # head: top, bottom-less, four sides
        shade_rect(px, 8, 0, 16, 8, pal, rnd)          # top
        for x0 in (0, 8, 16, 24):                       # right, front, left, back
            shade_rect(px, x0, 8, x0 + 8, 16, pal, rnd)
        # face opening (front)
        for y in range(11, 16):
            for x in range(10, 14):
                px[x, y] = (0, 0, 0, 0)
        for x in range(9, 15):
            px[x, 10] = accent if x in (9, 14) else pal["h"]
        px[11, 2] = px[12, 2] = px[11, 3] = px[12, 3] = accent   # crest
        # body
        shade_rect(px, 20, 16, 28, 20, pal, rnd)        # top
        shade_rect(px, 28, 16, 36, 20, pal, rnd)        # bottom
        shade_rect(px, 16, 20, 20, 32, pal, rnd)
        shade_rect(px, 20, 20, 28, 32, pal, rnd)        # front
        shade_rect(px, 28, 20, 32, 32, pal, rnd)
        shade_rect(px, 32, 20, 40, 32, pal, rnd)        # back
        for (x, y) in ((23, 23), (24, 23), (23, 24), (24, 24)):
            px[x, y] = accent                            # core gem
        for y in range(21, 31):
            px[22, y] = mix(px[22, y], pal["h"], 0.4)
            px[25, y] = mix(px[25, y], pal["o"], 0.3)
        # arms: shoulder pad + upper arm
        shade_rect(px, 44, 16, 48, 20, pal, rnd)        # top
        for x0 in (40, 44, 48, 52):
            shade_rect(px, x0, 20, x0 + 4, 27, pal, rnd)
        # boots: lower part of the legs + sole
        shade_rect(px, 8, 16, 12, 20, pal, rnd)         # sole
        for x0 in (0, 4, 8, 12):
            shade_rect(px, x0, 26, x0 + 4, 32, pal, rnd)
            px[x0 + 1, 27] = accent
        save(img, "models", "armor", f"{tier}_layer_1.png")

        # ---- layer 2: leggings
        img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        px = img.load()
        # belt on the body
        for x0, x1 in ((16, 20), (20, 28), (28, 32), (32, 40)):
            shade_rect(px, x0, 27, x1, 32, pal, rnd)
        px[23, 28] = px[24, 28] = accent                # buckle
        # legs
        shade_rect(px, 4, 16, 8, 20, pal, rnd)
        for x0 in (0, 4, 8, 12):
            shade_rect(px, x0, 20, x0 + 4, 30, pal, rnd)
        px[5, 24] = px[6, 24] = accent                  # knee
        save(img, "models", "armor", f"{tier}_layer_2.png")


# --------------------------------------------------------------------------- GUI (256x256, panel 176x166)
def gui():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    px = img.load()
    W, H = 176, 166
    face, hi, lo, black = rgb("#c6c6c6"), rgb("#ffffff"), rgb("#555555"), rgb("#000000")

    for y in range(H):
        for x in range(W):
            px[x, y] = face
    for i in range(W):
        px[i, 0] = black; px[i, H - 1] = black
        px[i, 1] = hi; px[i, 2] = hi
        px[i, H - 2] = lo; px[i, H - 3] = lo
    for i in range(H):
        px[0, i] = black; px[W - 1, i] = black
        px[1, i] = hi; px[2, i] = hi
        px[W - 2, i] = lo; px[W - 3, i] = lo
    # rounded corners
    clear = (0, 0, 0, 0)
    for (cx, cy, sx, sy) in ((0, 0, 1, 1), (W - 1, 0, -1, 1), (0, H - 1, 1, -1), (W - 1, H - 1, -1, -1)):
        px[cx, cy] = clear; px[cx + sx, cy] = clear; px[cx, cy + sy] = clear
        px[cx + sx, cy + sy] = black
        px[cx + 2 * sx, cy] = black; px[cx, cy + 2 * sy] = black
    px[W - 3, 2] = face; px[2, H - 3] = face
    for i in range(3, W - 3):
        px[i, 1] = hi; px[i, 2] = hi; px[i, H - 2] = lo; px[i, H - 3] = lo

    def slot(x, y, size=18, tint=None):
        """Slot frame whose inner item area starts at (x + 1, y + 1)."""
        inner = rgb("#8b8b8b") if tint is None else tint
        for dy in range(size):
            for dx in range(size):
                if dx == size - 1 and dy == 0 or dx == 0 and dy == size - 1:
                    color = rgb("#8b8b8b")
                elif dx == 0 or dy == 0:
                    color = rgb("#373737")
                elif dx == size - 1 or dy == size - 1:
                    color = hi
                else:
                    color = inner
                px[x + dx, y + dy] = color

    slot(51, 16, tint=rgb("#7f8f9c"))            # main input        (52, 17)
    slot(25, 16, tint=rgb("#8f8799"))            # catalyst 1        (26, 17)
    slot(77, 16, tint=rgb("#8f8799"))            # catalyst 2        (78, 17)
    slot(51, 52, tint=rgb("#9c8a7f"))            # special fuel      (52, 53)
    slot(133, 30, size=26)                       # output            (138, 35)
    for row in range(3):
        for col in range(9):
            slot(7 + col * 18, 83 + row * 18)
    for col in range(9):
        slot(7 + col * 18, 141)

    # "+" between the catalysts and the main ingredient
    plus = rgb("#6f6f6f")
    for cx in (47, 73):
        for d in range(-2, 3):
            px[cx + d, 25] = plus
            px[cx, 25 + d] = plus

    flame = [
        "......x.......",
        ".....xx.......",
        ".....xxx......",
        "....xxxx...x..",
        "...xxxxxx.xx..",
        "..xxxxxxxxxx..",
        "..xxxxxxxxxxx.",
        ".xxxxxxxxxxxx.",
        ".xxxxxxxxxxxx.",
        ".xxxxxxxxxxxx.",
        ".xxxxxxxxxxxx.",
        "..xxxxxxxxxx..",
        "..xxxxxxxxxx..",
        "...xxxxxxxx...",
    ]
    empty = rgb("#7a7a7a")
    hot = [rgb("#fffbd0"), rgb("#ffd24a"), rgb("#ff8a1e"), rgb("#e0481a"), rgb("#37c8ff")]
    for y, row in enumerate(flame):
        for x, ch in enumerate(row):
            if ch != "x":
                continue
            px[53 + x, 36 + y] = empty
            # lit sprite: white-hot core, orange body, cold blue tips (Primordial fire)
            dist = abs(x - 6.5) / 6.5 + (13 - y) / 13.0 * 0.9
            if y <= 3:
                color = hot[4]
            elif dist < 0.45:
                color = hot[0]
            elif dist < 0.8:
                color = hot[1]
            elif dist < 1.15:
                color = hot[2]
            else:
                color = hot[3]
            px[176 + x, y] = color

    # progress arrow 24x17: background at (104, 35), filled sprite at (176, 14)
    for y in range(17):
        for x in range(24):
            if x < 15:
                inside = 6 <= y <= 10
            else:
                inside = abs(y - 8) <= (23 - x)
            if inside:
                px[104 + x, 35 + y] = rgb("#8b8b8b")
                edge = y in (6, 10) if x < 15 else abs(y - 8) == (23 - x)
                px[176 + x, 14 + y] = rgb("#ffffff") if not edge else rgb("#9fe6ff")
    save(img, "gui", "container", "primordial_smelter.png")


if __name__ == "__main__":
    items()
    blocks()
    armor()
    gui()
    print("textures written to", os.path.normpath(ROOT))
