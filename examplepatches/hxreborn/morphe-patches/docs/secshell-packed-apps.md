# SecShell packed apps

Castle (`com.external.castle`) ships behind SecShell (Bangcle): `com.SecShell.SecShell.AP` as the
manifest `appComponentFactory`, `lib/<abi>/libSecShell.so`, a stub `classes.dex`, and the real dex
files in an encrypted payload. Measured against 2.1.1 (148) and 2.1.3 (157).

`shared/misc/secshell/` reads and rewrites the payload from a `rawResourcePatch`. A patch calls
`editSecShellPayload { }` and works on payload classes with `replaceBody`, `method`,
`prependToMethodUsingString` and `addExtensionPackage`.

## Payload layouts

Both versions cipher the first 0x20000 bytes of the payload with one fixed XOR keystream. The two
builds share it.

| Version | Where the payload lives | Body cipher |
|---|---|---|
| 2.1.1 | `assets/classes0.jar`, a zip of nine dex files | every byte after the head is XOR 0xac |
| 2.1.3 | five dex files appended inside the stub `classes.dex`, whose header `file_size` covers them | none; only each dex's own head is ciphered |

2.1.3 lays the dex files out in 4 KB aligned slots with random padding, then a 3876-byte table in a
cipher not recovered here, then a footer: the stub's data end as a little endian u32 and `fdex`.
The shell accepts a dex shorter than its slot, so a rebuilt dex is written in place and no slot
offset changes.

In 2.1.1 the jar's first entry starts inside the ciphered head. `SecShellJar` edits only the
entries past it, which hold every class the Castle patch touches.

## The keystream comes from the APK

`SecShellEmbeddedDex` derives the keystream at patch time instead of shipping a key. The plain body
of a payload dex is enough to rebuild its ciphered head:

- the `map_list` at the end of the dex gives every header field
- `string_ids`, which fill the rest of the head, are the offsets of the `string_data` items in order
- bytes 8 to 31 come from recomputing SHA-1 and adler32 over the decrypted file

It takes the dex with the most strings, so the rebuilt head covers all 0x20000 bytes, and checks the
result against every payload dex: each decrypted header checksum must equal that dex's adler32.

## What the packer checks at run time

- SecShell compares every APK entry's CRC32 with the stock value and kills the process with SIGBUS
  `BUS_ADRALN` at fault address `0xa01` about two seconds after launch. A new entry fails the same
  way. The signing key does not matter.
- In 2.1.3 ART verifies the stub dex checksum, which now covers the payload:
  `Failure to verify dex file ... Bad checksum`, then `ClassNotFoundException` for
  `com.SecShell.SecShell.AP`.
- Reading `/proc/<pid>/mem` or attaching Frida kills the app; `process_vm_readv` reads the in-memory
  dex without a kill.

2.1.1 restores the jar entry's CRC32 with four forged bytes in the inner zip's end record comment.
2.1.3 has to keep both the stub adler32 and the `classes.dex` CRC32 right at once. It writes byte
pairs 65521 apart as `0x00` and `0xff` in the padding a rebuilt dex left free, stores the adler32,
then flips bits in both bytes of a pair to reach the stock CRC32 through a GF(2) solve. A pair whose
bytes sum to 255 at positions 65521 apart leaves both adler32 sums unchanged. This needs one free
region of at least 65527 bytes in a rebuilt slot.

## On a version bump

1. Apply the bundle to the new stock APK. `Applied:` means the container parsed, the keystream
   validated against every payload dex, and each anchor resolved.
2. Compare entries with stock: only the payload file (`assets/classes0.jar` or `classes.dex`) may
   change content, and its CRC32 must equal the stock value.
3. Decode the patched payload and disassemble the edited methods.
4. Launch the patched build and confirm the process outlives the integrity check, then exercise the
   gated surfaces against a control build.

A layout the patch does not know fails with a `PatchException`:

| Failure | Message |
| --- | --- |
| Neither payload form is present | `Neither assets/classes0.jar nor a payload in classes.dex, this is not a SecShell build` |
| A payload dex head does not decipher with the derived keystream | `SecShell payload dex at <offset> does not decipher with the derived key` |
| A rebuilt dex is larger than its slot | `Rebuilt <name> is <size> bytes, its slot holds <slot size>` |
