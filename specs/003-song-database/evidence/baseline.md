# Feature 003 Pre-Change Catalog Baseline

**Captured**: 2026-09-17
**Revision**: `1aee35539d08fbd5bf904a88d988083aab88b604` plus untracked feature-003 planning documents
**Status**: Pre-implementation observation; no feature outcome is claimed

## Source identity

- `app/src/main/assets/songs.xml`: SHA-256 `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`
- `app/src/test/resources/catalog-baseline.xml`: SHA-256 `d63120cb5bf336c65e0ade44bc28f554cf096e41675ff3d85a4bb08f3edce658`
- Raw source entries: 422
- Effective last-entry-wins titles after U+0020 removal: 414

## Duplicate winners

Historical positions are zero-based. Winner hashes cover the effective lyric after U+0020 removal.

| Title | Positions | Winning position | Winning effective lyric SHA-256 | First non-empty line |
|---|---:|---:|---|---|
| 轻轻听 | 42, 320 | 320 | `8666c405428d6fae272c12ca7ba1a0479307bd129004273adbaf97a1d9af69d9` | 轻轻细心听　轻轻细心听您声 |
| 以色列的圣者 | 53, 373 | 373 | `8d6eb24128e51acda5e3e68540ddfe132b64d7e281c34344740630355458b811` | 以色列的圣者　为我牺牲自己 |
| 耶稣基督是主 | 87, 123 | 123 | `eaa21174bb65c7fd6eb962f417b124423cec1e45d4c89cf56002274880671a38` | 因为祂是万王之王，因为祂是万主之主， |
| 耶稣耶稣 | 102, 110 | 110 | `da277046c9f7d5ed8cedc5d4f87e2016cb1b4b65e2df89acc98c5449259ee57a` | 我们敬拜的，和平之君；我们称颂的，明亮的晨星； |
| 天堂在我心 | 109, 351 | 351 | `e5a34c2512e468747d4413696618d94c38b32a45ec52a37b9af9add3f88af349` | 谁说　沉睡已久的花蕾　不能绽于一季缤纷 |
| 全地宣告 | 143, 144 | 144 | `b0f4eddc29cef4e3a6aab5265ee297c657e7e408018f93a82d3da647f22676c1` | 全地宣告　荣耀复活救世主 |
| 像天空的鸽子 | 213, 214 | 214 | `c07870f8a9233ca9f807c5704de03de2284b0f0ec826ad21e7cb56437dc81215` | 哦主耶稣为您自己的荣耀 |
| 愿您崇高 | 341, 342 | 342 | `6f943816946c0bf8227f2baedfe847105f089fa76a33acd9e6820153083699aa` | 我要在万民中称谢您 |

## Baseline command

```text
./gradlew :app:testDebugUnitTest --no-build-cache --rerun-tasks
```

Observed: **PASS**, 24 tasks executed, in 12 seconds. Existing compiler deprecation/opt-in warnings remain baseline warnings; no feature-003 implementation existed.

## Planned evidence matrix

| Area | Initial status |
|---|---|
| Canonical XML deduplicated to 414 | Unexecuted |
| Python generator/schema tests | Unexecuted |
| Generated SQLite logical parity | Unexecuted |
| Gradle generated-asset packaging | Unexecuted |
| SQLite runtime reader instrumentation | Unexecuted |
| Offline API37 list/reader parity | Unexecuted |
| Maintainer add/edit/rename/delete drill | Unexecuted |
| Search-readiness boundary | Unexecuted |
| In-place upgrade | Unexecuted |
| Final constitutional code review | Unexecuted |

Historical feature 001/002 specifications, evidence, `catalog-baseline.xml`, and `legacy-api37.json` remain untouched.
