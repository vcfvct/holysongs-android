# Dependency provenance and license-risk audit

Date: 2026-09-15
Feature: 001-modernize-build-system
Artifact inspected: `libs/pinyin4j-2.5.0.jar`

## 1. Inspected artifact and checksum

Executed verification:

```bash
cd /home/hanli3/GIT/holysongs-android
python3 - <<'PY'
import hashlib, zipfile
p='libs/pinyin4j-2.5.0.jar'
with open(p, 'rb') as f:
    data = f.read()
print('sha256', hashlib.sha256(data).hexdigest())
with zipfile.ZipFile(p) as z:
    print('entries', len(z.namelist()))
    for name in z.namelist()[:20]:
        print(name)
    print('manifest')
    print(z.read('META-INF/MANIFEST.MF').decode('utf-8', 'replace'))
PY
```

Observed output:

```text
sha256 6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68
entries 107
META-INF/
META-INF/MANIFEST.MF
com/
com/hp/
com/hp/hpl/
com/hp/hpl/sparta/
com/hp/hpl/sparta/xpath/
net/
net/sourceforge/
net/sourceforge/pinyin4j/
net/sourceforge/pinyin4j/format/
net/sourceforge/pinyin4j/format/exception/
manifest:
Manifest-Version: 1.0
Ant-Version: Apache Ant 1.6.5
Created-By: 1.6.0-beta-b59g (Sun Microsystems Inc.)
Built-By: Li Min
Main-Class: demo.Pinyin4jAppletDemo
```

This preserves the checksum required by the project record: `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`.

## 2. Provenance observations

The JAR was inspected byte-for-byte without modifying the file.

Observations from the archive:

- The archive contains `net/sourceforge/pinyin4j/...` classes, which is consistent with the `pinyin4j` project namespace.
- The manifest shows an Apache Ant build and a `Main-Class: demo.Pinyin4jAppletDemo` entry.
- `Built-By: Li Min` is recorded in the archive, but the archive does not provide a full upstream chain of custody or a signed provenance record.
- No bundled `META-INF/LICENSE`, `META-INF/NOTICE`, or similarly named license file was found in the JAR.

## 3. Uncertainty and risk record

This file intentionally preserves uncertainty rather than claiming license clearance.

- The archive is a retained vendored dependency and is not being replaced or deleted in this task.
- The package namespace and manifest are suggestive of the open-source pinyin4j project, but the repository does not include an upstream license file or an authoritative packaging statement.
- Because no license metadata is embedded in the JAR and no external verified license receipt is in the repo, there is an unresolved distribution risk.
- The project must remain in a read-only dependency state for this slice and must not claim license acceptance or a cleared distribution path based on the incomplete evidence available here.

## 4. Decision boundary

This dependency record is a baseline risk note for the modernization work. It does not authorize app distribution, replacement, or removal of the JAR. The additive build work later must preserve the existing checksum and document any future change to dependency provenance before making a replacement.
