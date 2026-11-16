#!/usr/bin/env python3
"""
HD pixel-art generator for the Cryonite mod (Minecraft 1.14+ "Jappa" style).

Writes every texture of the mod - items, blocks, worn armor layers and the Primordial Smelter GUI -
straight into src/main/resources/assets/cryonite/textures, including animated sprite sheets with their
.png.mcmeta files.

    python tools/generate_hd_pixelart.py                  # generate everything
    python tools/generate_hd_pixelart.py --preview p.png  # ... and a contact sheet of the result
    python tools/generate_hd_pixelart.py --vanilla <dir|jar>

Vanilla textures are used as raw material: stone backgrounds for the ores, the shading of the netherite
armor, ingot and smithing template, the furnace for the smelter. They are looked up in
  1. --vanilla / $MC_ASSETS (a directory containing assets/minecraft/textures, or any jar/zip with it),
  2. the Gradle caches (~/.gradle/caches/**/ *client*.jar, written by NeoForge's ModDevGradle),
  3. the launcher (~/.minecraft/versions/1.21.1/1.21.1.jar).
Nothing from vanilla is copied into the repository unmodified.

Requires: Pillow, numpy.
"""
from __future__ import annotations

import argparse
import glob
import io
import json
import math
import os
import sys
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image

MODID = "cryonite"
ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src" / "main" / "resources" / "assets" / MODID / "textures"
TAU = math.tau

# ============================================================================================ vanilla


class Vanilla:
    """Reads vanilla textures from a directory or from a jar."""

    PROBE = "assets/minecraft/textures/item/netherite_sword.png"

    def __init__(self, explicit: str | None):
        self.dir: Path | None = None
        self.zip: zipfile.ZipFile | None = None
        self.cache: dict[str, np.ndarray] = {}
        for candidate in self._candidates(explicit):
            if self._open(Path(candidate)):
                self.source = str(candidate)
                return
        sys.exit("Vanilla textures not found. Pass --vanilla <dir or jar> (a folder with assets/minecraft/textures, "
                 "the Minecraft client jar, or client-extra.jar from the Gradle cache), or run a Gradle build first.")

    @staticmethod
    def _candidates(explicit):
        if explicit:
            yield explicit
            return
        if os.environ.get("MC_ASSETS"):
            yield os.environ["MC_ASSETS"]
        home = Path.home()
        gradle = Path(os.environ.get("GRADLE_USER_HOME", home / ".gradle"))
        for pattern in ("caches/neoformruntime/**/*client*.jar", "caches/**/client-extra*.jar", "caches/**/*client*1.21*.jar"):
            yield from sorted(glob.glob(str(gradle / pattern), recursive=True), key=os.path.getsize, reverse=True)
        yield from sorted(glob.glob(str(ROOT / "build" / "**" / "*client*.jar"), recursive=True))
        for base in (home / ".minecraft", home / "AppData" / "Roaming" / ".minecraft", home / "Library" / "Application Support" / "minecraft"):
            yield base / "versions" / "1.21.1" / "1.21.1.jar"

    def _open(self, path: Path) -> bool:
        if path.is_dir():
            for sub in (path, path / "assets" / "minecraft" / "textures", path / "minecraft" / "textures"):
                if (sub / "item" / "netherite_sword.png").is_file():
                    self.dir = sub
                    return True
            return False
        if path.is_file():
            try:
                archive = zipfile.ZipFile(path)
            except zipfile.BadZipFile:
                return False
            if self.PROBE in archive.namelist():
                self.zip = archive
                return True
        return False

    def get(self, name: str) -> np.ndarray:
        """RGBA array (H, W, 4) of assets/minecraft/textures/<name>.png."""
        if name not in self.cache:
            if self.dir is not None:
                image = Image.open(self.dir / (name + ".png"))
            else:
                image = Image.open(io.BytesIO(self.zip.read(f"assets/minecraft/textures/{name}.png")))
            self.cache[name] = np.array(image.convert("RGBA"))
        return self.cache[name].copy()

    def frame(self, name: str, index: int = 0) -> np.ndarray:
        """One 16x16 frame of a (possibly animated) vanilla texture."""
        sheet = self.get(name)
        size = sheet.shape[1]
        return sheet[index * size:(index + 1) * size]


VANILLA: Vanilla = None  # set in main()

# ============================================================================================ colour helpers


def rgb(value: str) -> np.ndarray:
    value = value.lstrip("#")
    return np.array([int(value[i:i + 2], 16) for i in (0, 2, 4)], dtype=float)


def ramp(*colors: str) -> np.ndarray:
    return np.stack([rgb(c) for c in colors])


def sample(colors: np.ndarray, position: float) -> np.ndarray:
    """Linear interpolation inside a ramp; position is a (fractional) index, clamped to the ramp."""
    position = min(max(position, 0.0), len(colors) - 1.0)
    low = int(math.floor(position))
    high = min(low + 1, len(colors) - 1)
    return colors[low] + (colors[high] - colors[low]) * (position - low)


def cyclic(colors: np.ndarray, position: float) -> np.ndarray:
    """Interpolation in a closed colour loop; position in turns (1.0 = full loop)."""
    scaled = (position % 1.0) * len(colors)
    low = int(math.floor(scaled)) % len(colors)
    high = (low + 1) % len(colors)
    return colors[low] + (colors[high] - colors[low]) * (scaled - math.floor(scaled))


def mix(a, b, amount: float) -> np.ndarray:
    return np.asarray(a, dtype=float) * (1.0 - amount) + np.asarray(b, dtype=float) * amount


def luminance(pixels: np.ndarray) -> np.ndarray:
    return pixels[..., 0] * 0.299 + pixels[..., 1] * 0.587 + pixels[..., 2] * 0.114


def noise(x: int, y: int, seed: int = 0) -> float:
    """Deterministic hash in [0, 1)."""
    n = (x * 374761393 + y * 668265263 + seed * 2147483647 + 1013904223) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65536.0


def seed_of(text: str) -> int:
    return sum((i + 1) * ord(c) for i, c in enumerate(text)) % 9973


def along(x: int, y: int) -> float:
    """0..1 along the bottom-left -> top-right diagonal (the axis of every vanilla tool sprite)."""
    return (x + 15 - y) / 30.0


# ============================================================================================ tiers
#
# Every sprite is a grid of one-character tokens that the tier turns into colours:
#   0-7  metal ramp        0/1 dark coloured outline, 2-5 body, 6 highlight, 7 specular
#   a-e  handle / secondary material ramp
#   A-E  primary energy    (frost glow, magma, neon rim, astral core) - animated
#   P-T  secondary energy  (frost sparkle, soul fire, stars, divine glow) - animated
#   .    transparent
# All ramps are hue shifted: shadows drift towards blue/violet, highlights towards warm/bright.


class Tier:
    name = ""
    frames = 16
    hold_first = 0          # extra ticks the first frame stays on screen (0 = plain loop)
    metal: np.ndarray
    handle: np.ndarray
    energy: np.ndarray
    spark: np.ndarray
    extra: np.ndarray = None    # tokens u-y, only some tiers

    def prepare(self, grid, seed):
        """Adapts a grid of plain vanilla shading levels to the look of the tier."""
        return list(grid)

    def metal_px(self, level, x, y, t, seed):
        return sample(self.metal, level)

    def handle_px(self, level, x, y, t, seed):
        return sample(self.handle, level)

    def energy_px(self, level, x, y, t, seed):
        return sample(self.energy, level)

    def spark_px(self, level, x, y, t, seed):
        return sample(self.spark, level)


class Cryonite(Tier):
    """Sharp faceted ice and the echo of the deep. A cold glint slides along the crystal."""
    name = "cryonite"
    frames = 12
    hold_first = 36
    metal = ramp("#091428", "#0a3641", "#086788", "#00a3cc", "#30c2e2", "#7fdcee", "#caf0f8", "#ffffff")
    handle = ramp("#070d1f", "#0f2440", "#17405c", "#2a6e86", "#56a7b4")
    energy = ramp("#06262f", "#0a4654", "#0e7f8c", "#29c7c1", "#a4f7ea")
    spark = ramp("#30c2e2", "#90e0ef", "#caf0f8", "#ffffff", "#ffffff")

    def glint(self, x, y, t):
        centre = -0.25 + 1.5 * t
        return max(0.0, 1.0 - abs(along(x, y) - centre) / 0.11)

    def metal_px(self, level, x, y, t, seed):
        if level >= 2:
            level = min(7.0, level + 2.2 * self.glint(x, y, t))
        return sample(self.metal, level)

    def energy_px(self, level, x, y, t, seed):
        return sample(self.energy, level + 0.8 * math.sin(TAU * (t + noise(x, y, seed) * 0.3)))

    def spark_px(self, level, x, y, t, seed):
        twinkle = max(0.0, math.sin(TAU * (t + noise(x, y, seed)))) ** 2
        return sample(self.spark, level + 2.5 * twinkle + 2.0 * self.glint(x, y, t))


class Infernite(Tier):
    """Charred basalt, veins of pulsing magma, soul fire on the cutting edge."""
    name = "infernite"
    metal = ramp("#0b0612", "#140c1c", "#2d1b2e", "#44293d", "#5c3a49", "#7a5258", "#a07a70", "#d3b092")
    handle = ramp("#0b0612", "#1c1020", "#352033", "#523445", "#7a5560")
    energy = ramp("#6a040f", "#d00000", "#ff6d00", "#ffba08", "#fff3b0")
    spark = ramp("#0077b6", "#00bbf9", "#00f5d4", "#b8fff2", "#ffffff")

    def energy_px(self, level, x, y, t, seed):
        return sample(self.energy, level + 1.0 * math.sin(TAU * (t - along(x, y) * 0.9)))

    def spark_px(self, level, x, y, t, seed):
        flicker = 0.9 * math.sin(TAU * (2 * t + noise(x, y, seed))) + 0.4 * math.sin(TAU * (t + noise(y, x, seed)))
        return sample(self.spark, level + flicker)


class Void(Tier):
    """A pitch black core full of distant stars inside a pulsing neon-violet rim."""
    name = "void"
    metal = ramp("#05010a", "#0a0316", "#110626", "#1a0a38", "#240046", "#3c096c", "#5a189a", "#7b2cbf")
    handle = ramp("#05010a", "#150a2b", "#2a1250", "#43217a", "#6a3fa8")
    energy = ramp("#240046", "#5a189a", "#7b2cbf", "#9d4edd", "#e0aaff")
    spark = ramp("#f6edff", "#c77dff", "#8be9fd", "#e0aaff", "#ffffff")

    def prepare(self, grid, seed):
        return voidify(grid, seed)

    def energy_px(self, level, x, y, t, seed):
        pulse = math.sin(TAU * (t - along(x, y) * 0.75))
        return sample(self.energy, level + 0.2 + 0.8 * pulse)

    def spark_px(self, level, x, y, t, seed):
        speed = 1 + int(noise(y, x, seed + 7) * 2)
        glow = 0.3 + 0.7 * max(0.0, math.sin(TAU * (speed * t + noise(x, y, seed)))) ** 2
        return mix(self.metal[0], self.spark[int(level) % len(self.spark)], glow)


class Aether(Tier):
    """Pearl and sky-gold frame around an astral core; a wave of energy runs along the blade."""
    name = "aether"
    metal = ramp("#4a2c1d", "#7f5539", "#b8743f", "#f4a261", "#ffd166", "#ffe6a3", "#fff6de", "#ffffff")
    handle = ramp("#463a66", "#857ea8", "#bdb8d8", "#e7e5f4", "#ffffff")
    energy = ramp("#b5179e", "#7a3fd0", "#4ea8de", "#72efdd", "#e2fff8")   # closed loop
    spark = ramp("#ffd166", "#ffe6a3", "#fff6de", "#ffffff", "#ffffff")

    def prepare(self, grid, seed):
        # pearl plates instead of solid gold: everything but the outline moves one step up the ramp
        return ["".join(str(min(7, int(c) + 1)) if c.isdigit() and int(c) >= 3 else c for c in row) for row in grid]

    def metal_px(self, level, x, y, t, seed):
        if level >= 3:
            shine = max(0.0, 1.0 - abs(along(x, y) - (-0.2 + 1.4 * t)) / 0.1)
            level = min(7.0, level + 1.2 * shine)
        return sample(self.metal, level)

    def energy_px(self, level, x, y, t, seed):
        wave = cyclic(self.energy, along(x, y) * 1.4 - t + level * 0.07)
        crest = max(0.0, math.sin(TAU * (along(x, y) * 1.4 - t))) ** 3
        return mix(wave, (255, 255, 255), 0.45 * crest)

    def spark_px(self, level, x, y, t, seed):
        return sample(self.spark, level + 1.2 * math.sin(TAU * (t + noise(x, y, seed) * 0.5)))


TIERS = {tier.name: tier for tier in (Cryonite(), Infernite(), Void(), Aether())}

# ============================================================================================ grid rendering


def check_grid(grid, name, width=16, height=16):
    assert len(grid) == height and all(len(row) == width for row in grid), \
        f"{name}: grid must be {width}x{height}, rows {[len(r) for r in grid]}"


def token_color(token, tier: Tier, x, y, t, seed):
    if token.isdigit():
        return tier.metal_px(int(token), x, y, t, seed)
    if "a" <= token <= "e":
        return tier.handle_px(ord(token) - ord("a"), x, y, t, seed)
    if "A" <= token <= "E":
        return tier.energy_px(ord(token) - ord("A"), x, y, t, seed)
    if "P" <= token <= "T":
        return tier.spark_px(ord(token) - ord("P"), x, y, t, seed)
    if "u" <= token <= "y":
        return sample(tier.extra, ord(token) - ord("u"))
    raise ValueError(f"unknown token {token!r}")


def render_grid(grid, tier: Tier, t: float, seed: int, background: np.ndarray | None = None, glow=None) -> np.ndarray:
    """
    One RGBA frame of a token grid. With a background, a space keeps the background pixel, '#' / '+'
    darken it (drop shadows) and `glow` = (colour, strength) tints the background next to energy tokens.
    """
    height, width = len(grid), len(grid[0])
    out = np.zeros((height, width, 4), dtype=float) if background is None else background.astype(float).copy()
    for y, row in enumerate(grid):
        for x, token in enumerate(row):
            if token == ".":
                if background is None:
                    continue
                token = " "
            if token == " ":
                if glow is not None and any(0 <= x + dx < width and 0 <= y + dy < height and "A" <= grid[y + dy][x + dx] <= "E"
                                            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    pulse = 0.75 + 0.25 * math.sin(TAU * (t - along(x, y) * 0.9))
                    out[y, x, :3] = mix(out[y, x, :3], glow[0], glow[1] * pulse)
                continue
            if token in "#+":
                out[y, x, :3] *= 0.6 if token == "#" else 0.8
                continue
            out[y, x, :3] = token_color(token, tier, x, y, t, seed)
            out[y, x, 3] = 255
    return np.clip(np.round(out), 0, 255).astype(np.uint8)


def animate(grid, tier: Tier, name: str, frames: int | None = None, background=None, glow=None):
    """All frames of a sprite; frames=1 gives a still image."""
    count = tier.frames if frames is None else frames
    seed = seed_of(name)
    return [render_grid(grid, tier, i / count, seed, background, glow) for i in range(count)]

# ============================================================================================ tools
#
# Geometry starts from the vanilla netherite tools (same handle, same diagonal, same size) and is then
# reshaped per tier: ice spikes, jagged edges with magma cracks, detached floating tips, winged guards.

TOOLS = {
    "cryonite": {
        "sword": [
            ".............111",
            "............1770",
            "...........17620",
            "..........16520.",
            ".........17420..",
            "...1....16520...",
            "..161..17420....",
            "..154116520.....",
            "...1517420......",
            "...154320.......",
            "....14431.......",
            "...ac01451......",
            "..ad0..11.......",
            "163a............",
            "1520............",
            "000.............",
        ],
        "pickaxe": [
            "................",
            "........1.......",
            "......11711.....",
            ".....176654210..",
            "......100034d0..",
            "......1...a340..",
            ".........ac0340.",
            "........ada.050.",
            ".......aca..070.",
            "......ada...060.",
            ".....aba....050.",
            "....aba.....10..",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "axe": [
            "................",
            ".........11.....",
            ".......11761....",
            "......176431....",
            ".....1765334c...",
            "......0653430...",
            ".....1.0043a30..",
            "........aca330..",
            ".......ada.00...",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "shovel": [
            "................",
            "............1...",
            "...........171..",
            "..........17660.",
            ".........165450.",
            "........1643450.",
            ".........a3450..",
            "........ad050...",
            ".......ada.0....",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "..aaba..........",
            "..ada...........",
            "...aa...........",
            "................",
        ],
        "hoe": [
            "........1.......",
            ".......171......",
            "......17661.....",
            ".......00561ac..",
            ".........045da..",
            "..........a430..",
            ".........ac00...",
            "........ada.....",
            ".......aca......",
            "......ada.......",
            ".....aca........",
            "....aba.........",
            "...aca..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
    },
    "infernite": {
        "sword": [
            ".............PRQ",
            "............1SR0",
            "...........16DP0",
            "..........15C30.",
            "........116D20..",
            "..1.....15B30...",
            "..141..16C30....",
            "..154.15D20.....",
            "...1516C30......",
            "...154B20.......",
            "....1430........",
            "...ac0430.......",
            "..ad0.0420......",
            "1C20....00......",
            "120.............",
            "000.............",
        ],
        "pickaxe": [
            "................",
            "................",
            "......11111.....",
            ".....14C45B110..",
            ".....1100034d0..",
            "..........a3C0..",
            ".........ac03D0.",
            "........ada.0C0.",
            ".......aca..040.",
            "......ada...0D0.",
            ".....aba....0R0.",
            "....aba......Q..",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "axe": [
            "................",
            ".........11.....",
            "........1541....",
            ".......Q5C31....",
            "......R54D34c...",
            "......P43C430...",
            ".......0043a30..",
            "........aca330..",
            ".......ada.00...",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "shovel": [
            "................",
            "................",
            "...........1Q1..",
            "..........15C40.",
            ".........14D340.",
            "........143C340.",
            ".........a3450..",
            "........ad050...",
            ".......ada.0....",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "..aaba..........",
            "..ada...........",
            "...aa...........",
            "................",
        ],
        "hoe": [
            "................",
            ".......111......",
            "......1PC51.....",
            ".......00B41ac..",
            ".........045da..",
            "..........a430..",
            ".........ac00...",
            "........ada.....",
            ".......aca......",
            "......ada.......",
            ".....aca........",
            "....aba.........",
            "...aca..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
    },
    "void": {
        "sword": [
            ".............EDC",
            "............E00B",
            ".............B0A",
            ".D........ED..A.",
            ".........E00B...",
            "........D0P0A...",
            "..DC...D000A....",
            "..C0A.D0Q0A.....",
            "...C0D000A......",
            "...C0000A.......",
            "....C00A...B....",
            "...acaC0A.......",
            "..ada.AB0A......",
            "DC0a....AA......",
            "C0A.............",
            "AAA.............",
        ],
        "pickaxe": [
            "................",
            "................",
            "......EEEDD.....",
            "...C.D0P000BBA..",
            "......CAAA00dA..",
            "..........a0QA..",
            ".........acC00A.",
            "........ada.DAA.",
            ".......aca......",
            "......ada...DDA.",
            ".....aba....C0A.",
            "....aba......A..",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "axe": [
            "................",
            ".........ED.....",
            "........E00B....",
            "....D..E0P0B....",
            "......D0000Cc...",
            "......A0Q00BA...",
            "....B..AA0BaBA..",
            "........aca00A..",
            ".......ada.AA...",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "...aba..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "shovel": [
            "................",
            "................",
            "...........EDC..",
            ".......C..E000A.",
            ".........E0P00A.",
            "........D000Q0A.",
            ".........a000A..",
            "........adA0A...",
            ".......ada.A.B..",
            "......aca.......",
            ".....aba........",
            "....aba.........",
            "..aaba..........",
            "..ada...........",
            "...aa...........",
            "................",
        ],
        "hoe": [
            "................",
            ".......EDC......",
            "....D.E0P0B.....",
            ".......AA00Bac..",
            ".....B...A00da..",
            "..........a00A..",
            ".........acAA...",
            "........ada.....",
            ".......aca......",
            "......ada.......",
            ".....aca........",
            "....aba.........",
            "...aca..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
    },
    "aether": {
        "sword": [
            ".............111",
            "............1760",
            "...........17C40",
            "...1......16E40.",
            "..171....17C40..",
            "..161...16E40...",
            ".1651..17C40....",
            ".1565116E40.....",
            "..11517C40......",
            "....1TE1........",
            "....1444111.....",
            "...ada154561....",
            "..ada.14511.....",
            "164a...11.......",
            "1T40............",
            "111.............",
        ],
        "pickaxe": [
            "................",
            "................",
            "......11111..1..",
            ".....17C6E54171.",
            "......1000C4T61.",
            "..........a4E0..",
            ".........ad0C40.",
            "........ada.1E0.",
            ".......ada..1C0.",
            "......ada...1E0.",
            ".....ada....1C0.",
            "....ada......0..",
            "...ada..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "axe": [
            "................",
            ".........11.....",
            "........1761....",
            ".......17C41....",
            "......16CE44d...",
            "......06C4430...",
            ".......00T4a40..",
            "........ada440..",
            ".......ada.00...",
            "......ada.......",
            ".....ada........",
            "....ada.........",
            "...ada..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
        "shovel": [
            "................",
            "................",
            "...........111..",
            "..........17E60.",
            ".........16C450.",
            "........15E4450.",
            ".........a4450..",
            "........ad050...",
            ".......ada.0....",
            "......ada.......",
            ".....ada........",
            "....ada.........",
            "..aada..........",
            "..ada...........",
            "...aa...........",
            "................",
        ],
        "hoe": [
            "................",
            ".......111......",
            "......17E61.....",
            ".......00C61ad..",
            ".........04Tda..",
            "..........a430..",
            ".........ad00...",
            "........ada.....",
            ".......ada......",
            "......ada.......",
            ".....ada........",
            "....ada.........",
            "...ada..........",
            "..ada...........",
            "..aa............",
            "................",
        ],
    },
}

# ============================================================================================ vanilla-derived sprites


def levels_of(name: str, top: int = 6, whole: bool = False) -> list[str]:
    """Token grid of a vanilla sprite: every colour becomes a metal level by its luminance rank."""
    pixels = VANILLA.get(name) if whole else VANILLA.frame(name)
    opaque = pixels[..., 3] > 0
    lum = np.round(luminance(pixels[..., :3].astype(float)), 3)
    values = sorted(set(lum[opaque].tolist()))
    grid = []
    for y in range(pixels.shape[0]):
        row = ""
        for x in range(pixels.shape[1]):
            if not opaque[y, x]:
                row += "."
            else:
                row += str(int(round(values.index(lum[y, x]) / max(1, len(values) - 1) * top)))
        grid.append(row)
    return grid


def patch(grid: list[str], runs: str, only_opaque: bool = False) -> list[str]:
    """
    Applies edits written as "x,y:tokens" (a horizontal run starting at x,y), separated by spaces.
    With only_opaque, pixels that are transparent in the grid stay transparent.
    """
    rows = [list(row) for row in grid]
    for run in runs.split():
        position, tokens = run.split(":")
        x, y = (int(v) for v in position.split(","))
        for i, token in enumerate(tokens):
            if token == "_" or (only_opaque and rows[y][x + i] == "."):
                continue
            rows[y][x + i] = token
    return ["".join(row) for row in rows]


def is_edge(grid, x, y, dx, dy) -> bool:
    nx, ny = x + dx, y + dy
    return not (0 <= nx < len(grid[0]) and 0 <= ny < len(grid)) or grid[ny][nx] == "."


def voidify(grid: list[str], seed: int, stars: float = 0.16) -> list[str]:
    """Black core, neon rim (bright where it faces the light at the top left), stars inside."""
    out = []
    for y, row in enumerate(grid):
        line = ""
        for x, token in enumerate(row):
            if not token.isdigit():
                line += token
                continue
            lit = is_edge(grid, x, y, 0, -1) or is_edge(grid, x, y, -1, 0)
            dark = is_edge(grid, x, y, 0, 1) or is_edge(grid, x, y, 1, 0)
            level = int(token)
            if lit:
                line += "E" if level >= 3 and noise(x, y, seed) > 0.35 else "D"
            elif dark:
                line += "A" if noise(x, y, seed) > 0.4 else "B"
            elif noise(x, y, seed + 31) < stars:
                line += "PQRS"[int(noise(y, x, seed + 5) * 4)]
            else:
                line += "2" if level >= 5 else "1" if level >= 4 else "0"
        out.append(line)
    return out


# ============================================================================================ multitools
#
# A paxel: a broad triangular axe blade on the top left, a pick arm that curves down on the right and
# the diagonal handle of every vanilla tool. Silhouette and shading follow the reference sprite the icon
# was redrawn from (light from the top left, a dark underside below the blade, a bright ridge inside it).
# The silhouette is shared by all four tiers; each tier then dresses it the way it dresses its other tools.

MULTITOOL_BASE = [
    ".........43.....",
    "........473.....",
    ".......3752.....",
    "......375452....",
    ".....36544342...",
    "....2652212352..",
    "....222...b051..",
    ".........bca151.",
    "........bda.042.",
    ".......bca...11.",
    "......bda....01.",
    ".....bca......1.",
    "....bda.......0.",
    "...bca..........",
    "..bda...........",
    "..ca............",
]


def multitool_grids() -> dict[str, list[str]]:
    return {
        # an icicle on the blade tip, frozen edges and three frost sparkles
        "cryonite": patch(MULTITOOL_BASE, "9,0:57 8,1:5 3,5:1 3,6:1 14,13:1 8,3:R 12,6:R 5,5:R"),
        # magma veins through the blade and the pick arm, soul fire on the cutting edge and the pick point
        "infernite": [
            ".........43.....",
            "........4D3.....",
            ".......3C52.....",
            "......3D5452....",
            ".....3C544C42...",
            "....S652212D52..",
            "....RQP...b0C1..",
            ".........bca1D1.",
            "........bda.0C2.",
            ".......bca...B1.",
            "......bda....01.",
            ".....bca......Q.",
            "....bda.......R.",
            "...bca..........",
            "..bda...........",
            "..ca............",
        ],
        # black starry core in a neon rim, shards drifting away from the blade and the pick point
        "void": patch(voidify(MULTITOOL_BASE, seed_of("void_multitool"), stars=0.2),
                      "7,0:D 3,3:C 2,7:B 12,2:C 15,6:B 14,14:D 12,11:B"),
        # pearl and sky-gold frame, the astral wave runs through the blade and down the pick arm
        "aether": [
            ".........54.....",
            "........575.....",
            ".......47E3.....",
            "......47C563....",
            ".....47E5T453...",
            "....3763323C63..",
            "....232...b2E3..",
            ".........bda2C2.",
            "........bda.2E3.",
            ".......bda...C2.",
            "......bda....23.",
            ".....bda......2.",
            "....bda.......2.",
            "...bda..........",
            "..bda...........",
            "..da............",
        ],
    }


class Machine(Tier):
    """Palette of the Primordial Smelter: netherite plating, copper pipes, gold fittings, primordial fire."""
    name = "machine"
    frames = 8
    metal = ramp("#120d16", "#1d1620", "#2b222c", "#3a3038", "#4b4148", "#5f565c", "#787076", "#978f93")
    handle = ramp("#4a1f1a", "#7c3a2a", "#b55c3f", "#de8a62", "#f7bd93")       # copper
    energy = ramp("#8f0a0a", "#e03400", "#ff7b00", "#ffc21a", "#fff4b8")       # fire
    spark = ramp("#0a5fc2", "#00bbf9", "#3df5e0", "#c4fff6", "#ffffff")        # soul fire -> white heat
    extra = ramp("#7a4a12", "#b97a14", "#e9b12c", "#fbdc58", "#fff7ae")        # gold

    def energy_px(self, level, x, y, t, seed):
        return sample(self.energy, level + 0.9 * math.sin(TAU * (t + noise(x, y, seed))))

    def spark_px(self, level, x, y, t, seed):
        return sample(self.spark, level + 0.9 * math.sin(TAU * (t + noise(y, x, seed + 3))))


MACHINE = Machine()

# ============================================================================================ items

ARMOR_PIECES = ("helmet", "chestplate", "leggings", "boots")

# Edits on top of the (re-shaded) vanilla netherite armor icons.
ARMOR_ICON_EDITS = {
    # ice crest and shoulder icicles
    ("cryonite", "helmet"): "8,1:1 7,2:171 5,2:1 10,2:1 6,5:7 9,4:R",
    ("cryonite", "chestplate"): "1,1:1 4,1:1 11,1:1 14,1:1 2,3:7 13,3:7 7,9:R 5,8:7",
    ("cryonite", "leggings"): "5,3:77 10,8:R 4,6:7",
    ("cryonite", "boots"): "4,2:1 11,2:1 4,4:7 11,4:7 3,10:R",
    # horns, magma cracks, soul-fire eyes
    ("infernite", "helmet"): "3,4:3 2,3:15 1,2:16 1,1:11 12,4:3 12,3:51 13,2:61 13,1:11 6,4:C 7,5:D 8,5:C 9,6:B 6,8:Q 9,8:Q",
    ("infernite", "chestplate"): "2,1:1 13,1:1 5,7:C 6,8:D 7,9:C 8,10:D 9,11:C 7,7:B 10,9:B 3,4:B 12,5:C",
    ("infernite", "leggings"): "5,5:C 6,6:B 10,4:C 11,5:D 10,9:B 5,10:C 5,11:B",
    ("infernite", "boots"): "4,6:C 4,7:B 11,7:C 12,8:D 3,10:B 12,10:C",
    # shards drifting away from the plates
    ("void", "helmet"): "3,1:D 12,1:C 2,11:B 13,12:B",
    ("void", "chestplate"): "3,0:D 12,0:D 0,9:B 15,10:B 7,15:B",
    ("void", "leggings"): "2,5:C 13,8:B 7,9:D 8,12:B",
    ("void", "boots"): "6,3:C 9,2:D 7,7:B 0,7:C",
    # halo, wings, astral core
    ("aether", "helmet"): "5,1:RSTTSR 7,4:CC 7,5:EC 6,8:C 9,8:C",
    ("aether", "chestplate"): "1,0:1 0,1:1761 14,0:1 12,1:1671 7,8:DE 7,9:ED 7,11:CC 7,12:CC",
    ("aether", "leggings"): "7,3:CE 5,8:C 10,8:C 5,10:C 10,10:C",
    ("aether", "boots"): "1,4:11 1,5:17 2,6:1 13,4:11 13,5:71 13,6:1 4,7:C 11,7:C",
}

INGOT_EDITS = {
    "cryonite": "9,5:77 5,6:7 6,7:7 12,6:R 3,8:R",
    "infernite": "3,6:C 4,6:D 5,7:C 6,7:D 9,5:C 10,6:D 11,6:C 12,7:B 5,9:B 8,9:C 11,9:B 2,9:B",
    "void": "",
    "aether": "2,6:CDCDCDCDCDCD 4,5:CECECECE 3,9:C 6,9:C 9,9:C 12,9:C",
}


def to_tokens(grid, mapping):
    """Replaces shading levels through a table like {"01": "a", "23": "b"}."""
    table = {digit: token for digits, token in mapping.items() for digit in digits}
    return ["".join(table.get(c, c) for c in row) for row in grid]


def build_items(sprites):
    multitools = multitool_grids()
    for tier_name, grid in multitools.items():      # tiers may add spikes and shards, never cut the shared silhouette
        check_grid(grid, f"{tier_name}_multitool")
        missing = [(x, y) for y, row in enumerate(MULTITOOL_BASE) for x, token in enumerate(row) if token != "." and grid[y][x] == "."]
        assert not missing, f"{tier_name}_multitool lost pixels of the shared silhouette: {missing}"
    for tier_name, tools in TOOLS.items():
        tier = TIERS[tier_name]
        for tool, grid in {**tools, "multitool": multitools[tier_name]}.items():
            name = f"{tier_name}_{tool}"
            check_grid(grid, name)
            sprites[f"item/{name}"] = (animate(grid, tier, name), tier)

        for piece in ARMOR_PIECES:
            name = f"{tier_name}_{piece}"
            grid = tier.prepare(levels_of(f"item/netherite_{piece}"), seed_of(name))
            grid = patch(grid, ARMOR_ICON_EDITS[(tier_name, piece)])
            sprites[f"item/{name}"] = (animate(grid, tier, name), tier)

        name = f"{tier_name}_ingot"
        grid = patch(tier.prepare(levels_of("item/netherite_ingot"), seed_of(name)), INGOT_EDITS[tier_name])
        sprites[f"item/{name}"] = (animate(grid, tier, name), tier)

    cryonite, infernite, void, aether = (TIERS[n] for n in ("cryonite", "infernite", "void", "aether"))

    # --- Cryonite materials
    raw = to_tokens(levels_of("item/raw_iron"), {"0": "0", "1": "a", "2": "b", "3": "3", "4": "5", "5": "6", "6": "7"})
    sprites["item/raw_cryonite"] = (animate(patch(raw, "5,4:R 10,8:R"), cryonite, "raw_cryonite"), cryonite)
    dust = patch(levels_of("item/glowstone_dust", top=7), "7,6:R 9,9:R 5,10:R")
    sprites["item/cryonite_crystal_dust"] = (animate(dust, cryonite, "cryonite_crystal_dust", frames=1), cryonite)

    # --- Infernite materials: glowing shard with a charred crust, dull cooled billet
    shard = to_tokens(levels_of("item/amethyst_shard"), {"0": "0", "1": "1", "2": "3", "3": "B", "4": "C", "5": "D", "6": "E"})
    shard = patch(shard, "14,1:Q 13,1:R 9,7:3 7,10:2 11,5:4", only_opaque=True)
    sprites["item/molten_infernite_shard"] = (animate(shard, infernite, "molten_infernite_shard"), infernite)
    blank = patch(levels_of("item/nether_brick"), "4,7:A 5,8:A 9,6:A 10,7:A 7,9:A", only_opaque=True)
    sprites["item/cooled_infernite_blank"] = (animate(blank, infernite, "cooled_infernite_blank", frames=1), infernite)

    # --- Void materials
    unstable = voidify(levels_of("item/echo_shard"), seed_of("unstable_void_shard"), stars=0.3)
    unstable = patch(unstable, "2,2:D 13,3:C 1,11:B 14,12:D 12,14:B")
    sprites["item/unstable_void_shard"] = (animate(unstable, void, "unstable_void_shard"), void)
    cluster = voidify(levels_of("block/amethyst_cluster"), seed_of("stabilized_void_cluster"), stars=0.22)
    sprites["item/stabilized_void_cluster"] = (animate(cluster, void, "stabilized_void_cluster"), void)
    # white-hot skin around a darker violet heart
    billet = mask_of(levels_of("item/nether_brick"))
    depth = depth_of(billet)
    molten = ["".join("." if not billet[y, x] else "EDCB"[min(depth[y, x], 4) - 1] for x in range(16)) for y in range(16)]
    molten = patch(molten, "6,6:S 10,8:Q", only_opaque=True)
    sprites["item/molten_void_blank"] = (animate(molten, void, "molten_void_blank"), void)

    # --- Aether: a dormant grey star that wakes up into the ingot
    star = to_tokens(levels_of("item/nether_star"), {"01": "a", "2": "b", "34": "c", "5": "d", "6": "e"})
    star = patch(star, "7,7:34 7,8:43", only_opaque=True)
    sprites["item/inactive_aether"] = (animate(star, aether, "inactive_aether", frames=1), aether)

    # --- Smithing templates: tablet in the handle colours of the tier, pattern in its signature material
    template = levels_of("item/netherite_upgrade_smithing_template", top=7)
    patterns = {"cryonite": {"5": "5", "6": "6", "7": "7"}, "infernite": {"5": "B", "6": "C", "7": "D"}, "void": {"5": "C", "6": "D", "7": "E"}}
    for tier_name, pattern in patterns.items():
        grid = to_tokens(template, {"0": "a", "1": "a", "2": "b", "3": "c", "4": "d", **pattern})
        name = f"{tier_name}_upgrade_smithing_template"
        sprites[f"item/{name}"] = (animate(grid, TIERS[tier_name], name, frames=1), TIERS[tier_name])

# ============================================================================================ blocks

# Ores: tokens are painted over the vanilla stone, '#' is the drop shadow under a crystal.
CRYONITE_ORE = [
    "                ",
    "   67         5 ",
    "  563    76   # ",
    "  23#   6542    ",
    "  ##    32##    ",
    "        ##      ",
    "4     7         ",
    "#    653        ",
    "     #2#    67  ",
    "      #    5642 ",
    "  75       23## ",
    " 642       ##   ",
    " 2##   67       ",
    "      5642   53 ",
    "      23##   #  ",
    "      ##        ",
]

INFERNITE_ORE = [
    "                ",
    "    BC          ",
    "   CD     B     ",
    "    DC   CD     ",
    "     B   DEC    ",
    "          CB    ",
    "  B             ",
    " CDC      B     ",
    "  DEC    CD     ",
    "   CB     DC  B ",
    "              CD",
    "      BC      DC",
    "     CDE        ",
    "      DC   Q    ",
    "  P    B        ",
    "                ",
]

VOID_ORE = [
    "                ",
    "  DE            ",
    " D0PA     DD    ",
    " B00A    D0QA   ",
    "  AA     B00A   ",
    "          AA    ",
    "     DE         ",
    "    D00A      D ",
    "    DR0A     D0A",
    "     BA      B0A",
    "  D           A ",
    " D0A    DED     ",
    "  A    D0P0A    ",
    "       B00QA    ",
    "        BAA   DA",
    "              A ",
]

INFERNITE_BLOCK_CRACKS = ("3,3:BC 5,4:D 6,5:CB 8,5:C 9,4:D 10,3:CB 6,6:D 6,7:C 5,8:D 4,9:CB 3,10:C "
                          "7,8:B 8,9:C 9,9:D 10,10:C 11,10:B 11,11:C 12,12:B 9,6:B 12,5:C 12,6:B 7,11:C 6,12:B 3,6:B")

SMELTER_SIDE = [
    "5666666666666664",
    "2333333333333331",
    "0bdca#     bdca0",
    "0bdca#     bdca0",
    "uwyxvu#   uwyxvu",
    "uvxwvu#   uvxwvu",
    "0bdca# 54# bdca0",
    "0bdca# 42# bdca0",
    "0bdca# 54# bdca0",
    "0bdca# 42# bdca0",
    "uwyxvu#   uwyxvu",
    "uvxwvu#   uvxwvu",
    "0bdca#     bdca0",
    "0bdca#     bdca0",
    "4555555555555553",
    "1222222222222220",
]

SMELTER_FRONT = [
    "5666666666666664",
    "2333333333333331",
    "0dca  uyyu  dca0",
    "0dca  vwwv  dca0",
    "0dca45555553dca0",
    "uyxv4{0}2uyxv",
    "0dca4{1}2dca0",
    "0dca4{2}2dca0",
    "0dca4{3}2dca0",
    "0dca4{4}2dca0",
    "uyxv4{5}2uyxv",
    "0dca4{6}2dca0",
    "0dca32222221dca0",
    "0dca########dca0",
    "4555555555555553",
    "1222222222222220",
]
SMELTER_CAVITY_OFF = ["030030", "030030", "030030", "030030", "030030", "031130", "132231"]
SMELTER_CAVITY_ON = ["04A040", "A4BB4A", "B5CC5B", "C5PQ5C", "P5RR5Q", "Q5ST5R", "R4TT4S"]

SMELTER_TOP_EDITS = "5,5:bddddc 5,6:d0303a 5,7:d0303a 5,8:d0303a 5,9:d0303a 5,10:caaaab 2,2:y 13,2:y 2,13:y 13,13:x"


def darker(pixels: np.ndarray, factor: float) -> np.ndarray:
    out = pixels.astype(float)
    out[..., :3] *= factor
    return np.clip(out, 0, 255).astype(np.uint8)


def build_blocks(sprites):
    cryonite, infernite, void = (TIERS[n] for n in ("cryonite", "infernite", "void"))
    for grid, name in ((CRYONITE_ORE, "cryonite_ore"), (INFERNITE_ORE, "infernite_ore"), (VOID_ORE, "void_ore")):
        check_grid(grid, name)

    sprites["block/cryonite_ore"] = (animate(CRYONITE_ORE, cryonite, "cryonite_ore", background=VANILLA.frame("block/deepslate")), cryonite)
    sprites["block/infernite_ore"] = (animate(INFERNITE_ORE, infernite, "infernite_ore", background=VANILLA.frame("block/blackstone"),
                                              glow=(rgb("#d00000"), 0.32)), infernite)
    sprites["block/void_ore"] = (animate(VOID_ORE, void, "void_ore", background=VANILLA.frame("block/end_stone")), void)

    block = patch(levels_of("block/netherite_block"), INFERNITE_BLOCK_CRACKS)
    sprites["block/infernite_block"] = (animate(block, infernite, "infernite_block"), infernite)

    # Primordial Smelter: dark deepslate tiles behind netherite bands, copper pipes with gold couplings.
    wall = darker(VANILLA.frame("block/deepslate_tiles"), 0.85)
    check_grid(SMELTER_SIDE, "smelter side")
    sprites["block/primordial_smelter_side"] = (animate(SMELTER_SIDE, MACHINE, "smelter_side", frames=1, background=wall), MACHINE)
    for suffix, cavity, frames in (("front", SMELTER_CAVITY_OFF, 1), ("front_on", SMELTER_CAVITY_ON, None)):
        grid = [row.format(*cavity) for row in SMELTER_FRONT]
        check_grid(grid, "smelter " + suffix)
        glow = (rgb("#ff7b00"), 0.0) if frames == 1 else None
        sprites[f"block/primordial_smelter_{suffix}"] = (animate(grid, MACHINE, "smelter_" + suffix, frames=frames, background=wall, glow=glow), MACHINE)
    top = patch(levels_of("block/netherite_block"), SMELTER_TOP_EDITS)
    sprites["block/primordial_smelter_top"] = (animate(top, MACHINE, "smelter_top", frames=1), MACHINE)


# ============================================================================================ worn armor (64x32)
#
# The vanilla netherite layers provide the UV layout and the shading; the hat layer (x + 32), which
# floats half a pixel above the helmet, carries crests, horn stubs, drifting shards and the crown.

def rows_of(x, y0, y1, tokens):
    return " ".join(f"{x},{y}:{tokens}" for y in range(y0, y1 + 1))


ARMOR_SURFACE = {   # painted on existing plates only
    "cryonite": {
        1: (rows_of(11, 0, 7, "67") + " 21,22:76 22,23:76 23,24:76 25,26:7 26,27:7 33,21:7 34,22:76 35,23:76 36,24:7 "
            "44,16:7616 44,17:6171 44,18:1716 44,19:6167 40,20:7676 40,21:6565 52,20:6767 10,9:77 13,10:R 5,27:7 6,28:7"),
        2: "5,21:7 6,22:7 5,23:6 21,28:77 26,29:R 1,22:7 2,23:7",
    },
    "infernite": {
        1: ("9,2:C 10,3:D 11,4:C 12,5:B 13,5:C 10,5:B 21,21:B 22,22:C 23,23:D 24,24:C 24,25:D 25,26:C 26,27:B 22,26:B 23,27:C 22,28:B "
            "33,22:C 34,23:D 35,24:C 36,25:D 37,26:C 38,27:B 34,26:B 41,23:C 42,24:B 45,25:C 46,26:D 53,22:B 54,23:C "
            "5,29:C 6,30:B 1,28:C 13,29:B 2,10:C 3,11:B 20,10:C 21,11:B 27,9:C 28,10:D 29,11:C"),
        2: "5,22:C 6,23:B 1,24:C 2,25:B 13,23:C 14,24:D 9,26:B 21,28:C 22,29:B 26,28:B 33,28:C 34,29:D",
    },
    "void": {1: "", 2: ""},
    "aether": {
        1: ("23,22:CE 23,23:EC 23,24:CE 23,26:CC 23,27:CC 21,21:T 26,21:T "
            "32,21:76 38,21:67 32,22:776 37,22:677 32,23:6776 36,23:6776 33,24:677776 33,25:67 37,25:67 34,26:6 37,26:6 "
            "11,3:CE 11,4:EC 11,9:CE 45,17:TT 45,18:TT 5,28:C 6,28:C"),
        2: "23,27:CE 23,28:EC 5,22:C 6,22:C 5,24:T 21,29:T 26,29:T",
    },
}


def crown():
    """Gold circlet with points around the upper edge of the hat layer, and a halo on its top face."""
    runs = []
    for x in range(32, 64):
        runs.append(f"{x},10:{'5' if x % 2 else '4'}")
        if x % 2 == 0:
            runs.append(f"{x},9:6")
        if x % 4 == 0:
            runs.append(f"{x},8:T")
    for i in range(8):
        runs += [f"{40 + i},0:S", f"{40 + i},7:T", f"40,{i}:S", f"47,{i}:T"]
    return " ".join(runs)


ARMOR_FLOATING = {  # layer 1 only, on the hat layer
    "cryonite": rows_of(43, 1, 6, "75") + " 43,8:64 59,8:46 43,0:11 43,7:76",
    "infernite": "40,8:65 40,9:5 46,8:56 47,9:5 38,8:45 39,9:4 48,8:54 48,9:4 40,7:6 47,7:6 40,6:5 47,6:5",
    "void": "41,2:D 46,5:C 44,3:B 34,10:D 37,13:B 33,14:C 50,9:C 53,12:D 55,14:B 58,11:C 62,9:B 60,13:D 41,14:B 46,15:C",
    "aether": crown(),
}


def build_armor(images):
    for tier_name, tier in TIERS.items():
        for layer in (1, 2):
            name = f"{tier_name}_layer_{layer}"
            grid = tier.prepare(levels_of(f"models/armor/netherite_layer_{layer}", whole=True), seed_of(name))
            check_grid(grid, name, 64, 32)
            grid = patch(grid, ARMOR_SURFACE[tier_name][layer], only_opaque=True)
            if layer == 1:
                grid = patch(grid, ARMOR_FLOATING[tier_name])
            images[f"models/armor/{name}"] = render_grid(grid, tier, 0.0, seed_of(name))

# ============================================================================================ GUI
#
# 256x256 sheet, panel 176x166 at the origin. Every coordinate below mirrors PrimordialSmelterMenu
# (slot positions are the top-left corner of the 16x16 item area) and PrimordialSmelterScreen.

GUI_INPUT = (52, 17)
GUI_CATALYSTS = ((26, 17), (78, 17))
GUI_FUEL = (52, 53)
GUI_OUTPUT = (138, 35)
GUI_PLAYER_INVENTORY = (8, 84)
GUI_HOTBAR = (8, 142)
GUI_FLAME = (53, 36)        # 14x14, lit sprite at u=176 v=0
GUI_ARROW = (104, 35)       # 24x17, lit sprite at u=176 v=14

SLOT_DARK, SLOT_LIGHT, SLOT_CENTER = rgb("#111111"), rgb("#555555"), rgb("#2b2b2b")

FLAME_MASK = [
    "..............",
    ".......#......",
    "......##......",
    "..#...###.....",
    "..##..####..#.",
    ".###.#####.##.",
    ".#########.##.",
    ".############.",
    "##############",
    "##############",
    "##############",
    ".############.",
    ".############.",
    "..##########..",
]

FLASK_ICON = [
    "................",
    "......####......",
    ".......##.......",
    ".......##.......",
    ".......##.......",
    "......#..#......",
    ".....#....#.....",
    "....#......#....",
    "....#~~~~~~#....",
    "...#~~~~~~~~#...",
    "...#~~~~~~~~#...",
    "...#~~~~~~~~#...",
    "....#~~~~~~#....",
    ".....######.....",
    "................",
    "................",
]

STAR_ICON = [
    "................",
    ".......#........",
    ".......#........",
    "......#~#.......",
    "......#~#.......",
    ".....#~~~#......",
    "...##~~~~~##....",
    ".##~~~~~~~~~##..",
    "...##~~~~~##....",
    ".....#~~~#......",
    "......#~#.......",
    "......#~#.......",
    ".......#........",
    ".......#........",
    "................",
    "................",
]


def mask_of(rows):
    return np.array([[c != "." for c in row] for row in rows])


def arrow_mask():
    mask = np.zeros((17, 24), dtype=bool)
    mask[6:11, 0:15] = True
    for x in range(14, 24):
        half = int(round(8 - (x - 14) * 8 / 9))
        mask[8 - half:8 + half + 1, x] = True
    return mask


def depth_of(mask):
    """Distance of every mask pixel to the outside (1 = on the border)."""
    padded = np.pad(mask, 1)
    depth = np.zeros(padded.shape, dtype=int)
    current, level = padded.copy(), 0
    while current.any():
        level += 1
        inner = current & np.roll(current, 1, 0) & np.roll(current, -1, 0) & np.roll(current, 1, 1) & np.roll(current, -1, 1)
        depth[current & ~inner] = level
        current = inner
    return depth[1:-1, 1:-1]


class Canvas:
    def __init__(self, width, height):
        self.px = np.zeros((height, width, 4), dtype=float)

    def fill(self, x, y, w, h, color):
        self.px[y:y + h, x:x + w, :3] = color
        self.px[y:y + h, x:x + w, 3] = 255

    def bevel(self, x, y, w, h, top_left, bottom_right, corner=None):
        """1 px frame: top and left in one colour, bottom and right in the other."""
        self.fill(x, y, w, 1, top_left)
        self.fill(x, y, 1, h, top_left)
        self.fill(x, y + h - 1, w, 1, bottom_right)
        self.fill(x + w - 1, y, 1, h, bottom_right)
        if corner is not None:
            self.fill(x + w - 1, y, 1, 1, corner)
            self.fill(x, y + h - 1, 1, 1, corner)

    def slot(self, x, y):
        """Vanilla sized slot; (x, y) is the item position, the 18x18 frame starts one pixel earlier."""
        self.fill(x, y, 16, 16, SLOT_CENTER)
        self.bevel(x - 1, y - 1, 18, 18, SLOT_DARK, SLOT_LIGHT, corner=mix(SLOT_DARK, SLOT_LIGHT, 0.5))

    def stamp(self, x, y, mask, colors):
        """colors(mx, my) -> rgb for every set mask pixel."""
        for my in range(mask.shape[0]):
            for mx in range(mask.shape[1]):
                if mask[my, mx]:
                    self.px[y + my, x + mx, :3] = colors(mx, my)
                    self.px[y + my, x + mx, 3] = 255

    def icon(self, x, y, rows):
        for my, row in enumerate(rows):
            for mx, c in enumerate(row):
                if c != ".":
                    self.fill(x + mx, y + my, 1, 1, rgb("#454349") if c == "#" else rgb("#37353b"))


def engraved(mask):
    """Dark recess with a lit lower right edge - the empty state of the flame and the arrow."""
    depth = depth_of(mask)

    def color(mx, my):
        if depth[my, mx] > 1:
            return rgb("#17151b")
        lower_right = (my + 1 >= mask.shape[0] or not mask[my + 1, mx]) or (mx + 1 >= mask.shape[1] or not mask[my, mx + 1])
        return rgb("#4b4753") if lower_right else rgb("#0b0a0e")
    return color


def draw_panel(canvas, width, height):
    """Dark netherite plate with a raised bevel, a faint stone grain and rounded corners."""
    outline, light, light2, dark, dark2 = (rgb(c) for c in ("#07060a", "#6d6976", "#56525e", "#131118", "#1f1d25"))
    canvas.fill(0, 0, width, height, outline)
    canvas.fill(1, 1, width - 2, height - 2, light)
    canvas.fill(2, 2, width - 3, height - 3, dark)
    canvas.fill(2, 2, width - 4, height - 4, light2)
    canvas.fill(3, 3, width - 5, height - 5, dark2)
    for y in range(3, height - 3):
        for x in range(3, width - 3):
            grain = (noise(x, y, 11) - 0.5) * 7 + (noise(x // 2, y // 2, 5) - 0.5) * 5
            shade = 1.0 - 0.10 * (y / height)                       # slightly darker towards the bottom
            canvas.px[y, x, :3] = (rgb("#2f2c36") + grain) * shade + np.array([0.0, 0.0, 2.0])
            canvas.px[y, x, 3] = 255
    for cx, cy in ((0, 0), (width - 1, 0), (0, height - 1), (width - 1, height - 1)):   # rounded corners
        sx, sy = (1 if cx == 0 else -1), (1 if cy == 0 else -1)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            canvas.px[cy + dy * sy, cx + dx * sx] = 0
        canvas.fill(cx + sx, cy + sy, 1, 1, outline)


def build_gui() -> np.ndarray:
    canvas = Canvas(256, 256)
    width, height = 176, 166
    draw_panel(canvas, width, height)

    # --- ancient-city trim: engraved line with sculk-teal studs, gold rivets in the corners
    trim_dark, trim_light, teal, teal_hi = (rgb(c) for c in ("#15131a", "#454150", "#0e5560", "#29c7c1"))
    canvas.bevel(5, 5, width - 10, height - 10, trim_dark, trim_light)
    for x in range(17, width - 12, 12):
        for y in (5, height - 6):
            canvas.fill(x, y, 2, 1, teal)
            canvas.fill(x, y, 1, 1, teal_hi if (x // 12) % 2 else teal)
    for y in range(17, height - 12, 12):
        for x in (5, width - 6):
            canvas.fill(x, y, 1, 2, teal)
    for x, y in ((4, 4), (width - 7, 4), (4, height - 7), (width - 7, height - 7)):
        canvas.fill(x, y, 3, 3, rgb("#7a4a12"))
        canvas.fill(x, y, 2, 2, rgb("#e9b12c"))
        canvas.fill(x, y, 1, 1, rgb("#fff7ae"))

    # --- separator between the machine and the player inventory
    canvas.fill(8, 79, width - 16, 1, trim_dark)
    canvas.fill(8, 80, width - 16, 1, trim_light)

    # --- conduits: catalysts -> input, input -> flame -> fuel
    copper_dark, copper, copper_hi = rgb("#4a1f1a"), rgb("#b55c3f"), rgb("#f7bd93")
    for x0 in (GUI_CATALYSTS[0][0] + 17, GUI_INPUT[0] + 17):
        canvas.fill(x0, 23, 8, 4, copper_dark)
        canvas.fill(x0, 24, 8, 2, copper)
        canvas.fill(x0, 24, 8, 1, copper_hi)
    for y0 in (GUI_INPUT[1] + 17, GUI_FUEL[1] - 3):
        canvas.fill(GUI_INPUT[0] + 6, y0, 4, 2, copper_dark)
        canvas.fill(GUI_INPUT[0] + 7, y0, 2, 2, copper)
        canvas.fill(GUI_INPUT[0] + 7, y0, 1, 2, copper_hi)

    # --- machine slots
    canvas.slot(*GUI_INPUT)
    for position, icon in zip(GUI_CATALYSTS, (FLASK_ICON, STAR_ICON)):
        canvas.slot(*position)
        canvas.icon(position[0], position[1], icon)
    canvas.slot(*GUI_FUEL)
    canvas.icon(GUI_FUEL[0] + 1, GUI_FUEL[1] + 1, FLAME_MASK)

    ox, oy = GUI_OUTPUT                                             # large framed output slot (26x26)
    canvas.fill(ox - 5, oy - 5, 26, 26, rgb("#211f27"))
    canvas.bevel(ox - 5, oy - 5, 26, 26, SLOT_DARK, SLOT_LIGHT, corner=mix(SLOT_DARK, SLOT_LIGHT, 0.5))
    canvas.bevel(ox - 4, oy - 4, 24, 24, rgb("#3d3a45"), rgb("#19171e"))
    for cx, cy in ((ox - 3, oy - 3), (ox + 18, oy - 3), (ox - 3, oy + 18), (ox + 18, oy + 18)):
        canvas.fill(cx, cy, 1, 1, rgb("#e9b12c"))
    canvas.slot(ox, oy)

    # --- player inventory and hotbar
    for row in range(3):
        for column in range(9):
            canvas.slot(GUI_PLAYER_INVENTORY[0] + column * 18, GUI_PLAYER_INVENTORY[1] + row * 18)
    for column in range(9):
        canvas.slot(GUI_HOTBAR[0] + column * 18, GUI_HOTBAR[1])

    # --- flame and arrow: engraved when idle, the lit versions sit right of the panel
    flame, arrow = mask_of(FLAME_MASK), arrow_mask()
    canvas.stamp(*GUI_FLAME, flame, engraved(flame))
    canvas.stamp(*GUI_ARROW, arrow, engraved(arrow))

    flame_depth = depth_of(flame)
    fire = ramp("#8f0a0a", "#ff5a00", "#ffc21a", "#3df5e0", "#c4fff6", "#ffffff")

    def flame_color(mx, my):
        heat = flame_depth[my, mx] + (my - 6) * 0.22 - abs(mx - 6.5) * 0.12   # white core low in the centre
        return sample(fire, heat)
    canvas.stamp(176, 0, flame, flame_color)

    arrow_depth = depth_of(arrow)
    journey = ramp("#00b4d8", "#48cae4", "#ff6d00", "#ffba08", "#9d4edd", "#e0aaff", "#ffd166", "#ffffff")

    def arrow_color(mx, my):
        color = sample(journey, mx / 23 * (len(journey) - 1))        # ice -> fire -> void -> aether
        if arrow_depth[my, mx] == 1:
            return mix(color, (20, 8, 40), 0.62)                      # dark, tinted outline
        return mix(color, (255, 255, 255), 0.35) if my <= 7 and arrow_depth[my, mx] == 2 else color
    canvas.stamp(176, 14, arrow, arrow_color)

    return np.clip(np.round(canvas.px), 0, 255).astype(np.uint8)


# ============================================================================================ Kinetic Energy Converter
#
# Create integration (compat/create). The block model (models/block/kinetic_energy_converter.json) is a
# 12 pixel deep casing with a collar around the shaft:
#   side   rows 4..15 are the casing wall (row 4 touches the front), rows 0..1 of x 4..11 wrap the collar
#   front  the face the shaft leaves through, the centre 8x8 is the top of the collar
#   back   the energy socket
# The GUI sheet is 512x256: the 256x196 panel at the origin, the lit energy column and the lit RPM
# gauge right of it. Every rectangle mirrors KineticConverterScreen.

class Converter(Tier):
    """Create's brass on netherite plating, Aether energy in the conduits."""
    name = "converter"
    frames = 16
    metal = ramp("#120d16", "#1d1620", "#2b222c", "#3a3038", "#4b4148", "#5f565c", "#787076", "#978f93")   # netherite
    handle = ramp("#5a3a22", "#8a5c38", "#c4944f", "#eabf66", "#ffeb8c")                                    # brass
    energy = ramp("#b5179e", "#7a3fd0", "#4ea8de", "#72efdd", "#e2fff8")                                    # aether, closed loop
    spark = ramp("#b8743f", "#f4a261", "#ffd166", "#ffe6a3", "#ffffff")                                     # aether gold
    extra = ramp("#3d403d", "#5a5d5a", "#70756f", "#828784", "#8f9691")                                     # andesite shaft

    def energy_px(self, level, x, y, t, seed):
        wave = cyclic(self.energy, (x + y) / 16 * 1.2 - t + level * 0.09)
        crest = max(0.0, math.sin(TAU * ((x + y) / 16 * 1.2 - t))) ** 3
        return mix(wave, (255, 255, 255), 0.4 * crest)

    def spark_px(self, level, x, y, t, seed):
        return sample(self.spark, level + 1.0 * math.sin(TAU * (t + noise(x, y, seed) * 0.5)))


CONVERTER = Converter()

CONVERTER_SIDE = [
    "0000beddddcb0000",     # collar, lit from above
    "0000acccbbba0000",
    "0000000000000000",
    "0000000000000000",
    "cdddddddddddddcb",     # brass rail at the front edge
    "baaaaaaaaaaaaaa0",
    "4555555555555552",     # netherite plate
    "43d3333333333d31",
    "4310000000000131",
    "431BCDEDCBABCD31",     # aether conduit
    "431ABCDCBAABCB31",
    "4311111111111131",
    "43d3333333333d31",
    "3222222222222221",
    "bcccccccccccccba",     # brass rail at the back edge
    "a00000000000000a",
]

CONVERTER_BACK = [
    "cddddddddddddddb",
    "d55555555555554a",
    "d5d3333333333d2a",
    "d533333333333 2a".replace(" ", "3"),
    "d533bddddddc332a",
    "d533d000000b332a",
    "d533d0BCDC0b332a",
    "d533d0CEEC0b332a",
    "d533d0DEED0b332a",
    "d533d0CDCB0b332a",
    "d533d000000b332a",
    "d533cbbbbbba332a",
    "d53333333333332a",
    "d5d3333333333d2a",
    "d42222222222222a",
    "baaaaaaaaaaaaaa0",
]


def converter_front() -> list[str]:
    """Brass frame, netherite plate, a brass collar around the shaft hole and four Aether lamps."""
    rows = []
    for y in range(16):
        row = ""
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            ring = max(abs(x - 7.5), abs(y - 7.5))          # 0.5 in the centre, 7.5 on the border
            lit = (x + y) < 15                              # top left half catches the light
            if edge == 0:
                token = "d" if (y == 0 or x == 0) and lit else "b" if lit else "a"
            elif ring <= 1.5:
                token = "0" if ring <= 0.5 else "1"         # the hole, hidden behind the shaft
            elif ring <= 2.5:
                token = "a"                                 # shadow around the shaft
            elif ring <= 3.5:
                token = "d" if lit else "b"                 # top of the collar
            elif ring <= 4.5:
                token = "1"                                 # shadow under the collar
            elif edge == 1:
                token = "5" if lit else "2"
            else:
                token = "3"
            row += token
        rows.append(row)
    lamps = "7,2:CD 7,13:DC 2,7:C 2,8:D 13,7:D 13,8:C 2,2:d 13,2:c 2,13:c 13,13:b 5,5:e 10,10:a"
    return patch(rows, lamps)


def build_converter(sprites):
    front = converter_front()
    for grid, name in ((CONVERTER_SIDE, "side"), (front, "front"), (CONVERTER_BACK, "back")):
        check_grid(grid, "converter " + name)
        sprites[f"block/kinetic_energy_converter_{name}"] = (animate(grid, CONVERTER, "converter_" + name), CONVERTER)


CONVERTER_GUI_SIZE = (256, 196)
CONVERTER_BAR = (11, 21, 16, 164)            # energy column, lit sprite at u=256 v=0
CONVERTER_GAUGE = (39, 179, 204, 4)          # RPM gauge, lit sprite at u=280 v=0
CONVERTER_READOUT = (34, 143, 214, 43)       # recessed plate behind the three readouts and the gauge
CONVERTER_CONTENT = (34, 214)                # x and width of the widget column
CONVERTER_ROWS = (32, 54, 76, 98, 120)       # y of the five 18 pixel high widget rows

BOLT_ICON = [
    "....##.",
    "...##..",
    "..##...",
    ".#####.",
    "...##..",
    "..##...",
    ".##....",
    ".#.....",
]

COG_ICON = [
    "...##.##...",
    ".#.#####.#.",
    ".#########.",
    "..###.###..",
    "####...####",
    ".###...###.",
    "####...####",
    "..###.###..",
    ".#########.",
    ".#.#####.#.",
    "...##.##...",
]


def build_converter_gui() -> np.ndarray:
    canvas = Canvas(512, 256)
    width, height = CONVERTER_GUI_SIZE
    draw_panel(canvas, width, height)

    brass_dark, brass, brass_hi = rgb("#5a3a22"), rgb("#c4944f"), rgb("#ffeb8c")
    trim_dark, trim_light = rgb("#15131a"), rgb("#454150")

    # --- brass frame with rivets in the corners, a brass rule under the title
    canvas.bevel(5, 5, width - 10, height - 10, trim_dark, trim_light)
    for x, y in ((4, 4), (width - 7, 4), (4, height - 7), (width - 7, height - 7)):
        canvas.fill(x, y, 3, 3, brass_dark)
        canvas.fill(x, y, 2, 2, brass)
        canvas.fill(x, y, 1, 1, brass_hi)
    canvas.fill(10, 17, width - 20, 1, brass_dark)
    canvas.fill(10, 16, width - 20, 1, brass)
    for x in range(22, width - 20, 24):
        canvas.fill(x, 16, 2, 1, brass_hi)

    # --- icons next to the title: energy on the left, rotation on the right
    for (ix, iy), rows, color in (((16, 7), BOLT_ICON, rgb("#72efdd")), ((width - 27, 5), COG_ICON, brass)):
        for my, row in enumerate(rows):
            for mx, c in enumerate(row):
                if c == "#":
                    canvas.fill(ix + mx, iy + my, 1, 1, color)

    # --- energy column: recessed slot with a tick every 10 %
    bx, by, bw, bh = CONVERTER_BAR
    canvas.fill(bx, by, bw, bh, rgb("#0d0b11"))
    canvas.bevel(bx - 1, by - 1, bw + 2, bh + 2, SLOT_DARK, SLOT_LIGHT, corner=mix(SLOT_DARK, SLOT_LIGHT, 0.5))
    for step in range(1, 10):
        y = by + bh - round(bh * step / 10)
        canvas.fill(bx, y, 3 if step != 5 else 5, 1, rgb("#3a3742"))

    # --- faint recesses behind the widget rows, so the buttons sit in the plate
    cx, cw = CONVERTER_CONTENT
    for y in CONVERTER_ROWS:
        canvas.fill(cx - 1, y - 1, cw + 2, 20, rgb("#211f27"))
        canvas.bevel(cx - 1, y - 1, cw + 2, 20, rgb("#19171e"), rgb("#3d3a45"))

    # --- readouts: dark plate with a brass rule above, the RPM gauge at its bottom
    rx, ry, rw, rh = CONVERTER_READOUT
    canvas.fill(rx, ry - 3, rw, 1, brass_dark)
    canvas.fill(rx, ry - 4, rw, 1, brass)
    canvas.fill(rx, ry, rw, rh, rgb("#15131a"))
    canvas.bevel(rx, ry, rw, rh, SLOT_DARK, SLOT_LIGHT, corner=mix(SLOT_DARK, SLOT_LIGHT, 0.5))
    gx, gy, gw, gh = CONVERTER_GAUGE
    canvas.fill(gx, gy, gw, gh, rgb("#08070b"))
    canvas.bevel(gx - 1, gy - 1, gw + 2, gh + 2, rgb("#050407"), rgb("#4b4753"))
    for step in range(1, 8):                                     # a tick every 1006 RPM, a longer one at 256
        canvas.fill(gx + round(gw * step / 8), gy + gh - 1, 1, 1, rgb("#3a3742"))
    canvas.fill(gx + round(gw * 256 / 8048), gy, 1, gh, rgb("#5a3a22"))

    # --- lit energy column (u=256): Aether colours, brighter towards the top, split into cells
    aether = ramp("#b5179e", "#7a3fd0", "#4ea8de", "#72efdd", "#e2fff8")
    for y in range(bh):
        level = (bh - 1 - y) / (bh - 1) * (len(aether) - 1)
        for x in range(bw):
            color = sample(aether, level)
            shade = 1.18 if x in (1, 2) else 0.72 if x in (0, bw - 1) else 0.88 if x >= bw - 3 else 1.0
            if (bh - 1 - y) % 4 == 3:
                shade *= 0.62                                         # cell borders
            canvas.fill(256 + x, y, 1, 1, np.clip(color * shade, 0, 255))

    # --- lit RPM gauge (u=280): the four tiers from left to right
    journey = ramp("#00b4d8", "#48cae4", "#ff6d00", "#ffba08", "#9d4edd", "#e0aaff", "#ffd166", "#ffffff")
    for x in range(gw):
        color = sample(journey, x / (gw - 1) * (len(journey) - 1))
        for y in range(gh):
            shade = 1.15 if y == 1 else 0.7 if y == gh - 1 else 0.9 if y == 0 else 1.0
            canvas.fill(280 + x, y, 1, 1, np.clip(color * shade, 0, 255))

    return np.clip(np.round(canvas.px), 0, 255).astype(np.uint8)


def preview_converter(sprites, images, path: Path):
    """Contact sheet of the converter: its three faces and the panel with the lit sprites in place."""
    scale, pad = 2, 8
    gui = images["gui/container/kinetic_converter_gui"]
    width, height = CONVERTER_GUI_SIZE
    panel = gui[:height, :width].copy()
    bx, by, bw, bh = CONVERTER_BAR
    filled = round(bh * 0.7)
    panel[by + bh - filled:by + bh, bx:bx + bw] = gui[bh - filled:bh, 256:256 + bw]
    gx, gy, gw, gh = CONVERTER_GAUGE
    panel[gy:gy + gh, gx:gx + gw] = gui[0:gh, 280:280 + gw]
    faces = [sprites[f"block/kinetic_energy_converter_{name}"][0][0] for name in ("front", "side", "back")]
    face = 16 * 6
    sheet = Image.new("RGBA", (width * scale + face + 3 * pad, max(height * scale, 3 * (face + pad)) + 2 * pad), (139, 139, 139, 255))
    sheet.alpha_composite(Image.fromarray(panel, "RGBA").resize((width * scale, height * scale), Image.NEAREST), (pad, pad))
    for i, tile in enumerate(faces):
        sheet.alpha_composite(Image.fromarray(tile, "RGBA").resize((face, face), Image.NEAREST), (width * scale + 2 * pad, pad + i * (face + pad)))
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(path)


# ============================================================================================ output


def save_png(path: Path, pixels: np.ndarray):
    path.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray(pixels, "RGBA").save(path, optimize=True)


def preview_multitools(sprites, path: Path):
    """The four multitool icons side by side, three frames of the animation each."""
    scale, pad = 10, 8
    cell = 16 * scale
    names = list(TIERS)
    sheet = Image.new("RGBA", (len(names) * (cell + pad) + pad, 3 * (cell + pad) + pad), (139, 139, 139, 255))
    for column, tier_name in enumerate(names):
        frames = sprites[f"item/{tier_name}_multitool"][0]
        for row in range(3):
            frame = frames[row * len(frames) // 3]
            sheet.alpha_composite(Image.fromarray(frame, "RGBA").resize((cell, cell), Image.NEAREST),
                                  (pad + column * (cell + pad), pad + row * (cell + pad)))
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(path)


def write_sprite(name: str, frames: list[np.ndarray], tier: Tier) -> bool:
    """Writes a still image, or a vertical sprite sheet plus .png.mcmeta. Returns True when animated."""
    path = OUT / (name + ".png")
    meta = path.with_name(path.name + ".mcmeta")
    if all(np.array_equal(frames[0], frame) for frame in frames[1:]):
        save_png(path, frames[0])
        meta.unlink(missing_ok=True)
        return False
    save_png(path, np.concatenate(frames, axis=0))
    animation = {"interpolate": True, "frametime": 2}
    if tier.hold_first:
        animation["frames"] = [{"index": 0, "time": 2 + tier.hold_first}] + list(range(1, len(frames)))
    meta.write_text(json.dumps({"animation": animation}, indent=2) + "\n")
    return True


def check_names(sprites, images):
    """Every texture the generated models reference must exist, and nothing may be left unreferenced."""
    import re
    models = ROOT / "src" / "generated" / "resources" / "assets" / MODID / "models"
    handmade = ROOT / "src" / "main" / "resources" / "assets" / MODID / "models"
    referenced = set()
    for model in list(models.rglob("*.json")) + list(handmade.rglob("*.json")):
        referenced.update(re.findall(rf'"{MODID}:((?:item|block)/[a-z0-9_/]+)"', model.read_text()))
    referenced = {name for name in referenced if (OUT / (name + ".png")).exists() or name in sprites
                  or not any((root / (name + ".json")).exists() for root in (models, handmade))}   # model parents are not textures
    produced = {name for name in sprites if name.startswith(("item/", "block/"))}
    problems = [f"referenced by a model but not generated: {n}" for n in sorted(referenced - produced)]
    problems += [f"generated but not referenced by any model: {n}" for n in sorted(produced - referenced)]
    java = ROOT / "src" / "main" / "java"
    sources = "\n".join(path.read_text() for path in java.rglob("*.java"))
    for name in images:
        if name.startswith("gui/") and f"textures/{name}.png" not in sources:
            problems.append(f"GUI texture is not used by the code: {name}")
    on_disk = {str(p.relative_to(OUT))[:-4].replace(os.sep, "/") for p in OUT.rglob("*.png")}
    problems += [f"stale texture on disk: {n}" for n in sorted(on_disk - set(sprites) - set(images))]
    return problems


def preview(sprites, images, path: Path):
    scale, pad, columns = 6, 4, 14
    cell = 16 * scale + pad
    tiles = []
    for name in sorted(sprites, key=lambda n: (n.split("/")[0] != "item", n)):
        frames = sprites[name][0]
        tiles.append(frames[0])
        if any(not np.array_equal(frames[0], f) for f in frames[1:]):
            tiles.append(frames[len(frames) // 2])
    rows = (len(tiles) + columns - 1) // columns
    armor = [images[n] for n in sorted(images) if n.startswith("models/")]
    gui = images["gui/container/primordial_smelter"][:166, :216]
    armor_scale = 3
    armor_height = (len(armor) + 1) // 2 * (32 * armor_scale + pad)
    sheet_width = max(columns * cell, 2 * (64 * armor_scale + pad) + 216 * 2 + pad)
    sheet = Image.new("RGBA", (sheet_width, rows * cell + max(armor_height, 166 * 2) + 2 * pad), (139, 139, 139, 255))
    for i, tile in enumerate(tiles):
        image = Image.fromarray(tile, "RGBA").resize((16 * scale, 16 * scale), Image.NEAREST)
        sheet.alpha_composite(image, ((i % columns) * cell + pad // 2, (i // columns) * cell + pad // 2))
    top = rows * cell + pad
    for i, layer in enumerate(armor):
        image = Image.fromarray(layer, "RGBA").resize((64 * armor_scale, 32 * armor_scale), Image.NEAREST)
        sheet.alpha_composite(image, ((i % 2) * (64 * armor_scale + pad) + pad // 2, top + (i // 2) * (32 * armor_scale + pad)))
    image = Image.fromarray(gui, "RGBA").resize((216 * 2, 166 * 2), Image.NEAREST)
    sheet.alpha_composite(image, (2 * (64 * armor_scale + pad) + pad, top))
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(path)


def main():
    global VANILLA
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--vanilla", help="directory or jar with assets/minecraft/textures")
    parser.add_argument("--preview", help="also write a contact sheet of everything to this PNG")
    args = parser.parse_args()

    VANILLA = Vanilla(args.vanilla)
    print(f"vanilla textures: {VANILLA.source}")

    sprites: dict[str, tuple[list[np.ndarray], Tier]] = {}
    images: dict[str, np.ndarray] = {}
    build_items(sprites)
    build_blocks(sprites)
    build_converter(sprites)
    build_armor(images)
    images["gui/container/primordial_smelter"] = build_gui()
    images["gui/container/kinetic_converter_gui"] = build_converter_gui()

    animated = sum(write_sprite(name, frames, tier) for name, (frames, tier) in sprites.items())
    for name, pixels in images.items():
        save_png(OUT / (name + ".png"), pixels)
    print(f"written to {OUT.relative_to(ROOT)}: {len(sprites)} item/block textures ({animated} animated), "
          f"{sum(n.startswith('models/') for n in images)} armor layers, "
          f"{sum(n.startswith('gui/') for n in images)} GUI sheets")

    problems = check_names(sprites, images)
    for problem in problems:
        print("  !!", problem)
    print("names: " + ("all textures match the registered models" if not problems else f"{len(problems)} problem(s)"))

    if args.preview:
        preview(sprites, images, Path(args.preview))
        converter = Path(args.preview).with_name("kinetic_converter.png")
        preview_converter(sprites, images, converter)
        multitools = Path(args.preview).with_name("multitools.png")
        preview_multitools(sprites, multitools)
        print(f"preview: {args.preview}, {converter}, {multitools}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
