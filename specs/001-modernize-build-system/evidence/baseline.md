# T004 baseline capture

Date: 2026-09-15
Feature: 001-modernize-build-system
Original HEAD: 634f98624b654fe9926c7b1c8186fbad56df6673
Repository: /home/hanli3/GIT/holysongs-android
Status: source-derived baseline only; this file does not claim runtime validation or render success.

## 1. Source and hash inventory

Executed verification command:

```bash
cd /home/hanli3/GIT/holysongs-android
sha256sum assets/songs.xml libs/pinyin4j-2.5.0.jar src/com/goodtrendltd/HolySongs/MainActivity.java res/values/strings.xml
```

Observed output:

```text
88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89  assets/songs.xml
6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68  libs/pinyin4j-2.5.0.jar
07cd8e7afd9bda43ef7a76d1f6323b50e0a5932a9beab628681d016241482f1e  src/com/goodtrendltd/HolySongs/MainActivity.java
be8ae056a8f7ee952ee23a1e0f6f3066baec674359b2bb1e83515371c5d3caff  res/values/strings.xml
```

These hashes identify the original inputs. Asset and JAR bytes must remain unchanged. MainActivity and strings.xml hashes identify the pre-change baseline; scoped compatibility changes to those files are permitted by the plan and must be explained.

## 2. Preservation contracts recorded from source

These are repository-level preservation statements taken from the original implementation and data, not runtime pass claims:

- Preserve original order and all 422 entries in `assets/songs.xml`.
- Preserve bytes; no normalization or new IDs in the song title/lyric data.
- Preserve bytes and representative rendered output for the lyric text in the app's user-facing screens.
- "414 distinct keys; used by the current title-to-lyric map".

The `MainActivity` source retains the legacy semantics that later compatibility work must honor:

```java
String myData = getXml("songs.xml");
setDataFromXML(parser, myData);
final List<String> titleList = new ArrayList<String>(songLyricMap.keySet());
Collections.sort(titleList, new ChineseCharComp());
```

```java
private String getXml(String path) {
    ...
    return xmlString == null ? null : xmlString.replaceAll(" ", "");
}
```

```java
private void setDataFromXML(XMLParser parser, String xml) {
    ...
    String name = parser.getValue(e, "name");
    String lyric = parser.getValue(e, "lyric");
    songLyricMap.put(name, lyric);
}
```

The relevant behavior is:

- ASCII-space removal happens during XML read time (`replaceAll(" ", "")`), before the parsed title map is built.
- `songLyricMap` is a `HashMap`; when a duplicate title occurs, the last source occurrence wins.
- The effective title list is sorted by `Collections.sort(titleList, new ChineseCharComp())`, which uses `Collator.getInstance(Locale.CHINA)`.

## 3. Exact duplicate-title inventory and winner semantics

Reproducible extraction command:

```bash
cd /home/hanli3/GIT/holysongs-android
python3 - <<'PY'
import xml.etree.ElementTree as ET, hashlib, json
from collections import defaultdict
root = ET.parse('assets/songs.xml').getroot()
entries = []
for i, song in enumerate(root.findall('song')):
    name = song.findtext('name') or ''
    lyric = song.findtext('lyric') or ''
    entries.append({'index': i, 'name': name, 'lyric': lyric})
by_name = defaultdict(list)
for entry in entries:
    by_name[entry['name']].append(entry)
for name, values in sorted(by_name.items()):
    if len(values) > 1:
        winner = values[-1]
        raw = winner['lyric']
        stripped = raw.replace(' ', '')
        print({
            'title': name,
            'indexes': [v['index'] for v in values],
            'winner_index': winner['index'],
            'raw_chars': len(raw),
            'newline_count': raw.count('\n'),
            'raw_sha256': hashlib.sha256(raw.encode('utf-8')).hexdigest(),
            'space_removed_sha256': hashlib.sha256(stripped.encode('utf-8')).hexdigest(),
            'space_removed_text': json.dumps(stripped, ensure_ascii=False),
        })
PY
```

Observed duplicate title set (source-derived, not a runtime pass):

| Title | Source indexes | Winner index | Raw chars | Newline count | Raw SHA-256 | ASCII-space-removed SHA-256 |
|---|---:|---:|---:|---:|---|---|
| 以色列的圣者 | [53, 373] | 373 | 128 | 5 | `ab1750c2e604daee604c7be7b726e749e2f5908eb4a41324eb30e796d351a148` | `8d6eb24128e51acda5e3e68540ddfe132b64d7e281c34344740630355458b811` |
| 像天空的鸽子 | [213, 214] | 214 | 194 | 9 | `89525c02135ba05611290f2f04b0baaf58c6127cf3893602d12e1b6e1869c69d` | `c07870f8a9233ca9f807c5704de03de2284b0f0ec826ad21e7cb56437dc81215` |
| 全地宣告 | [143, 144] | 144 | 216 | 10 | `aaea64977139bdc927efa4e903c64f408939cffb1d8fd05181942891a922f90e` | `b0f4eddc29cef4e3a6aab5265ee297c657e7e408018f93a82d3da647f22676c1` |
| 天堂在我心 | [109, 351] | 351 | 305 | 11 | `36c91138544a275c88e27808be85d021f62bf184c446e6d59cadc93f8e60ebd7` | `e5a34c2512e468747d4413696618d94c38b32a45ec52a37b9af9add3f88af349` |
| 愿您崇高 | [341, 342] | 342 | 222 | 12 | `accd9da4ef55477c1025f79dbbbab00917e6c547db224356aa6b4a64805f328b` | `6f943816946c0bf8227f2baedfe847105f089fa76a33acd9e6820153083699aa` |
| 耶稣基督是主 | [87, 123] | 123 | 294 | 10 | `3db0cf07bbe89304caa9bbdda65d68e223374b03c090c1e8e8162fdbd531a83c` | `eaa21174bb65c7fd6eb962f417b124423cec1e45d4c89cf56002274880671a38` |
| 耶稣耶稣 | [102, 110] | 110 | 155 | 5 | `d6cc33d0c03a71bf05235679493da55e7f2d23baf233456a9add3d2010425829` | `da277046c9f7d5ed8cedc5d4f87e2016cb1b4b65e2df89acc98c5449259ee57a` |
| 轻轻听 | [42, 320] | 320 | 240 | 11 | `2a2b492761e0545a70d12bc443be07e473af1f1198fae8295e9f925cad36f922` | `8666c405428d6fae272c12ca7ba1a0479307bd129004273adbaf97a1d9af69d9` |

The effective title map retains exactly eight duplicate-title winner values; the raw duplicate entries remain in source data and are part of the baseline.

## 4. Full expected space-removed winner text for each duplicate title

These exact values are source-derived and intentionally not claimed as runtime-rendered output. They are the baseline for later device checks.

1. `以色列的圣者` winner at index 373:

```json
"\n以色列的圣者　为我牺牲自己\n神羔羊　你是弥赛亚　耶稣和平之君\n我要跪拜要敬拜你主啊　因为你是万王之王\n我要跪拜要敬拜你我神　宝贵耶稣和平之君\n"
```

2. `像天空的鸽子` winner at index 214:

```json
"\n哦主耶稣为您自己的荣耀\n我在这里求您差遗\n我愿您使我成为福音的使者\n传十字架永恒不变的爱\n为传扬您天国和平的佳音\n使我成为传福音的鸽子\n向那些忧伤困苦的人们\n传主喜乐平安的信息\n"
```

3. `全地宣告` winner at index 144:

```json
"\n全地宣告　荣耀复活救世主\n无与伦比　救世主耶稣荣美\n祂是永永远远　在宝座上羔羊\n欢欣跪拜我主　只想敬拜祂名\n\n我要宣告　荣耀复活救世主\n被杀羔羊　世人得与神和好\n你是永永远远　在宝座上羔羊　\n欢欣跪拜我主　只想敬拜你名　\n"
```

4. `天堂在我心` winner at index 351:

```json
"\n谁说　沉睡已久的花蕾　不能绽于一季缤纷\n谁说　寒冬之后的大地　不能展现一片青翠\n谁说　曾经受伤的翅膀　不能再度自由地飞\n我要抬起头张开双臂　拥抱神所赐的世界\n\nIwanttobefree　自由地飞\n在爱中无惧怕　在爱中无伤悲\nIwanttobefree　不再流泪\nBecauseIbelievethatheavenishere\n我有天堂在我的心\n"
```

5. `愿您崇高` winner at index 342:

```json
"\n我要在万民中称谢您\n我要在列邦中歌颂您\n您的慈爱高于诸天\n您的信实直达穹苍\n您的慈爱高于诸天\n您的信实直达穹苍\n\n哈利路亚哈利路亚\n您的慈爱高于诸天\n哈利路亚哈利路亚\n您的信实直达穹苍\n"
```

6. `耶稣基督是主` winner at index 123:

```json
"\n因为祂是万王之王，因为祂是万主之主，\n唯有祂从死里复活我要敬拜祂(2X)\n万膝要跪拜，万口要承认，耶稣基督祂是主，\n万膝要跪拜，万口要承认，耶稣基督祂是主。\n高唱荣耀哈利路亚，欢呼荣耀哈利路亚，\n祂是我的主，我的王，我要敬拜祂，\n哈利路亚，哈利路亚，耶稣基督祂是主，\n哈利路亚，哈利路亚，耶稣基督祂是主。\nTheend-耶稣基督祂是主（多次）\n"
```

7. `耶稣耶稣` winner at index 110:

```json
"\n我们敬拜的，和平之君；我们称颂的，明亮的晨星；\n我们等候的，再来的王，我们仰望的，公义的太阳。\n耶稣！耶稣！永生神的儿子，耶稣！耶稣！我的救赎主，\n耶稣！耶稣！世界的光，耶稣！耶稣！我心所盼望。\n"
```

8. `轻轻听` winner at index 320:

```json
"\n轻轻细心听　轻轻细心听您声\n轻轻细心听　轻轻细心听您声\n细细听　轻轻细细听\n倾听您说话儿共对应\n细细说　轻轻细细说\n因知道我牧人在细听\n上帝您是我独一生命光\n羊属您必清楚听您声\n牧养引导我　轻声教导我\n一生听您话儿共对应\n"
```

## 5. Internal ASCII-space contrast and long-form examples

The XML read path deliberately removes ASCII spaces before the title map is built. One direct internal contrast is in index 6, title `主我愿单属你`:

- raw lyric contains `"哦 耶稣"`
- post-`replaceAll(" ", "")` lyric contains `"哦耶稣"`

Exact source-derived values for that entry:

```text
index: 6
name: 主我愿单属你
raw_len: 130
space_removed_len: 73
raw_sha256: cc78e40bf8df7724295f7136a63aec39a955d3abab124fa56a6bf988d037034f
space_removed_sha256: 66d900ee407e3a2461e9a84a607802f46519bc862f3fa862580b6a9548b2f78c
```

```json
"\n我的主，我心爱你，我的主，我渴慕你，\n愿你爱来吸引我，使我心单单爱你。\n哦耶稣，我需要你，炼净我，完全属你，\n愿你爱来摸着我，使我心全然属你。\n"
```

Measured long-form example 1: `index 418`, title `主你是我力量`

```text
raw_len: 794
space_removed_len: 373
raw_sha256: 08acaf1c523a0ec0e091a47559ab6090441edf6002ff4d88333aed00ead129b9
space_removed_sha256: 6aae093c90d6534f485851c62e664c10b0382281fb700dca08a5c18c91165174
```

```json
"\n我的诗歌我的拯救\n祢是我患难中随时的帮助\n众山怎样围绕耶路撒冷\n祢必围绕我到永远\n主祢是我力量主祢是我高台\n坚固磐石我信靠祢必不动摇\n主祢是我力量主祢是避难所\n我的盼望只在乎祢\n\n我的诗歌我的拯救\n祢是我患难中随时的帮助\n众山怎样围绕耶路撒冷\n祢必围绕我到永远\n主祢是我力量主祢是我高台\n坚固磐石我信靠祢必不动摇\n主祢是我力量主祢是避难所\n我的盼望只在乎祢\n\n主祢是我力量主祢是我高台\n坚固磐石我信靠祢必不动摇\n主祢是我力量主祢是避难所\n我的盼望只在乎祢\n\n我的盼望只在乎祢\n我的盼望只在乎祢\n主祢是我力量主祢是我高台\n坚固磐石我信靠祢必不动摇\n主祢是我力量主祢是避难所\n我的盼望只在乎祢\n\n主祢是我力量主祢是我高台\n坚固磐石我信靠祢必不动摇\n主祢是我力量主祢是避难所\n我的盼望只在乎祢\n\n我的盼望只在乎祢\n我的盼望只在乎祢\n我的盼望只在乎祢\n"
```

Measured long-form example 2: `index 421`, title `我一生`

```text
raw_len: 778
space_removed_len: 338
raw_sha256: f24d31c6d3447066608e59ff9d95f802aaa1279aef0121d66e4909af98605aac
space_removed_sha256: bbc156799e27932dee8f900d2182c915b9385ba07b3fd54b1300d574d3716933
```

```json
"\n我在主面前拍拍手\n在主前跳跳舞\n在主面前唱一首欢乐的歌\n耶稣爱我我知道\n耶稣爱我不改变\n耶稣爱我，哈利路亚\n我在主面前拍拍手\n在主前跳跳舞\n在主面前唱一首欢乐的歌\n耶稣爱我我知道\n耶稣爱我不改变\n耶稣爱我，哈利路亚\n我一生要赞美主\n我一生要事奉主\n我一生要来彰显主荣美\n我一生要为主而活\n我一生要高举主\n我一生要跟随主\n我一生要来彰显主荣美\n我一生要为主而活\n\n我要赞美主，赞美主\n我要全心全意来赞美主\n我要赞美主，赞美主\n我要一生一世来赞美主\n我要赞美主，赞美主\n我要全心全意来赞美主\n我要赞美主，赞美主\n我要一生一世来赞美主\n我一生要赞美主\n我一生要事奉主\n我一生要来彰显主荣美\n我一生要为主而活\n我一生要高举主\n我一生要跟随主\n我一生要来彰显主荣美\n我一生要为主而活\n"
```

## 6. Summary

The repository baseline is stable and source-derived:

- `assets/songs.xml` is unchanged at SHA-256 `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`.
- `libs/pinyin4j-2.5.0.jar` is unchanged at SHA-256 `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`.
- The XML has 422 raw song entries and 414 distinct title keys.
- Eight titles are duplicated in source; the last duplicate wins in `songLyricMap`.
- The ASCII-space removal step is an observed compatibility contract, not a corrupted asset fix.
- Later runtime validation must still check the actual Android render. This file is a baseline record, not a pass result.
