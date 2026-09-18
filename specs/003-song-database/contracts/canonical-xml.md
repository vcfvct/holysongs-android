# Canonical Song XML Contract

**Authority**: `app/src/main/assets/songs.xml`
**Encoding**: UTF-8
**Cardinality after feature migration**: exactly 414 songs

## Grammar

The supported content model is intentionally narrow:

```xml
<songs>
    <song>
        <name>title text</name>
        <lyric>multiline
lyric text</lyric>
    </song>
</songs>
```

- `<songs>` is the single document root.
- Its content entries are direct `<song>` children.
- Each `<song>` has exactly one direct `<name>` followed by exactly one direct `<lyric>`.
- `name` and `lyric` contain text only. Nested verse, formatting, HTML, or other elements are rejected rather than partially read.
- XML indentation is permitted. DTDs, external entities, XInclude, and remote references are not part of this contract.

## Effective-value transform

For both title and lyric:

1. Parse the XML text as UTF-8.
2. Read the complete direct text value.
3. Remove every U+0020 ASCII SPACE.
4. Preserve every other code point exactly.

The transform does not trim, collapse, or normalize:

- U+000A line feeds;
- repeated/blank lines;
- leading or trailing newlines;
- U+3000 IDEOGRAPHIC SPACE;
- tabs or carriage returns if present;
- punctuation, Latin text, or Chinese characters;
- Unicode normalization forms.

The generator must prove the current canonical output equals the frozen pre-migration effective baseline. It must not assume visual equivalence.

## Validation rules

Generation fails before publishing a database when:

- XML is malformed or not UTF-8;
- the root or child structure differs from the grammar;
- a song has missing, repeated, nested, or unexpected fields;
- an effective title or lyric is empty;
- two raw titles become equal after U+0020 removal;
- source count is not 414 for production generation;
- generated identity or source-order constraints fail.

Fixture/test mode may use a non-414 count so add/edit/delete and failure behavior can be tested. Production mode must enforce 414.

## Duplicate cleanup

The old 422-entry source had eight duplicate pairs with different lyrics. Remove only the historical earlier entries at zero-based positions:

```text
42, 53, 87, 102, 109, 143, 213, 341
```

Retain the historical later entries for:

```text
轻轻听
以色列的圣者
耶稣基督是主
耶稣耶稣
天堂在我心
全地宣告
像天空的鸽子
愿您崇高
```

Their effective lyrics must equal the last-entry winners in the untouched `app/src/test/resources/catalog-baseline.xml` and `app/src/androidTest/assets/legacy-api37.json` fixtures.

## Maintenance rule

Maintainers edit XML only. Generated SQLite files are never hand-edited. A title rename is an intentional identity change; adding a song requires a new unique effective title; deleting a song removes its XML block. Every change must run the generator and parity/validation workflow described in `../quickstart.md`.
