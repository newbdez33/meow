# Widget characters

Six approved illustrations for the Meow home-screen widget, generated on
2026-10-03 with the built-in `image_gen` tool. The owner requested expressive
character choices inspired by Duolingo-style widgets and invited existing
Meow cats to appear. These images are widget artwork, independent of the
selected sound.

| File | Character | Identity reference |
| --- | --- | --- |
| [ginger.png](ginger.png) | Smug orange tabby | `c04`, Whispering |
| [black.png](black.png) | Spicy black cat | `c02`, I'm angry! |
| [gray.png](gray.png) | Chill gray tabby | `c01`, I'm Good |
| [sleepy.png](sleepy.png) | Sleepy beige tabby | `c17`, Sleepy.. |
| [pumpkin.png](pumpkin.png) | Black cat in a pumpkin | `c16`, Small World |
| [ghost.png](ghost.png) | Ghost cat with a black tail | `c23`, Mini |

All six PNG files are the unchanged 1254 × 1254 tool outputs. The orange
cat was generated first; the other five use it as a second reference for
style and scale, alongside their original iOS artwork. The original white
cat remains available as Classic in [the preview](../../widget-preview.html).

[prompts.json](prompts.json) contains the shared art brief and character
prompts. [generation.json](generation.json) records the exact tool arguments,
input paths, original output paths, and final filenames. No CLI/API fallback
was used. The source images are preserved at their generation paths.

Use rounded clipping at display time. The raster images already include
their backgrounds. Keep the sound label separate from the image. All six
illustrations and Classic were approved on 2026-10-03.

[export_widget_art.py](../../../tool/export_widget_art.py) produces 600 px
native assets for iOS and Android, plus iOS template masks for system tinting.
It leaves these originals unchanged. See the [widget design record](../../../docs/specs/2026-10-03-widget-design.md)
for native screenshots and remaining device checks.
