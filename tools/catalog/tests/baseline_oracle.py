"""Independent legacy catalog oracle used only by generator tests.

This module deliberately does not import the production generator. It models the
established effective behavior: remove U+0020 from fields and let the last raw
entry for an effective title win.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import xml.etree.ElementTree as ET


DUPLICATE_POSITIONS: dict[str, tuple[int, int]] = {
    "轻轻听": (42, 320),
    "以色列的圣者": (53, 373),
    "耶稣基督是主": (87, 123),
    "耶稣耶稣": (102, 110),
    "天堂在我心": (109, 351),
    "全地宣告": (143, 144),
    "像天空的鸽子": (213, 214),
    "愿您崇高": (341, 342),
}


@dataclass(frozen=True)
class BaselineSong:
    position: int
    title: str
    lyric: str


def _effective(value: str | None) -> str:
    return (value or "").replace(" ", "")


def raw_songs(path: Path) -> list[BaselineSong]:
    root = ET.fromstring(path.read_bytes())
    songs: list[BaselineSong] = []
    for position, element in enumerate(root.findall("song")):
        songs.append(
            BaselineSong(
                position=position,
                title=_effective(element.findtext("name")),
                lyric=_effective(element.findtext("lyric")),
            )
        )
    return songs


def effective_catalog(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for song in raw_songs(path):
        result[song.title] = song.lyric
    return result


def duplicate_winners(path: Path) -> dict[str, BaselineSong]:
    songs = raw_songs(path)
    return {title: songs[positions[-1]] for title, positions in DUPLICATE_POSITIONS.items()}
