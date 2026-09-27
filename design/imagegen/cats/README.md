# Redrawn cats

c01.png … c27.png (24 files) were made on 2026-09-27 with the Codex CLI built-in image generation
tool (codex-cli 0.157.1, model gpt-6-astra), prompt in ../cats-prompt.txt, one call per cat with the
128 px iOS drawing `meow/Assets.xcassets/cats/cNN.imageset/cNN@2x.png` as the reference.
`android/tool/export_art.py` turns them into the 512 px `drawable-nodpi` files; a cat without a file
here falls back to the upscaled iOS drawing. c06, c08 and c24 have no file: two attempts each came
back with a white smear (c06, c08) or a tiny off-centre bat (c24), so those three stay on the iOS
drawing. After the owner's review, c02 (a sitting cat instead of the arched Halloween pose) and c23
(the ghost had lost its black tail) were generated a third time with one sentence appended to the
prompt describing the pose and the tail.
