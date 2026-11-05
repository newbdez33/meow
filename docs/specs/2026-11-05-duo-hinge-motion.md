# Meow: three states and a cat that follows the fold

## Accepted direction

Keep the soundboard. Use three presentations: single screen, partially open
split, and fully open canvas. Add one small black-and-white tuxedo cat,
viewed directly from above as it lies on its back. Its pose follows the hinge
continuously in both directions.
There is no separate play mode, timer, audio effect, or camera feature.

This document replaces the November 4 play concepts. The HTML is a design
preview; the native app and TestFlight 2.3 (7) have not changed.

| State | Layout | Decoration |
| --- | --- | --- |
| Single screen | One scrolling list of the existing 27 cats | Hidden |
| Partially open | 15 cats in one pane; 12 and the reserved ad row in the other | Four paws raised, with visible pink pads |
| Fully open | One heading and one continuous eight-column grid; the ad occupies the final five cells | Cat lies flat with its complete white belly visible; no center divider |

Standard text fits all 27 cats in both inner-display presentations at the
preview sizes. Large text keeps scrolling. The same selected cat and audio
owner survive every layout change.

## Motion

The cat sits near the fold in a small clear area. In a book or fully open
presentation it uses the top navigation space; in a tabletop presentation it uses space
between the panes. It is decorative, accepts no input, and covers no cat
button or ad. Interactive controls stay outside the fold region.

- Use the tuxedo coat pattern: black head, ears, back, flanks, and tail; white
  muzzle, chest, belly, and four paws with pink pads. Keep the belly white in
  dark mode. Use a fixed overhead camera; do not show the cat in side profile.
- At the half-open midpoint all four paws point upward. As the device opens
  farther, all four paws move outward and the whole white belly opens up. Closing reverses this exact pose sequence.
- Stop moving the device and the pose stays still. Do not start a timed
  animation on a pose transition or add an idle loop.
- Fade the decoration only near the closed endpoint. Keep the fully open cat
  visible instead of fading it out. Hide the cat on the outer single screen.
- Under Reduce Motion, keep a static raised-paw pose within the partial state
  and show the static belly pose when fully open. At large
  accessibility text sizes, omit the decoration and keep the content readable.
- Folding alone does not play a sound. A cat button still plays its original
  sound without changing pitch or adding another voice.

## Native implementation boundary

Follow the user's three-state product model without deriving layout from a
stream of angle thresholds. Read active division regions and available space
for layout. Use the system hinge status to choose a partial or full decoration,
then map the continuous hinge angle to the pose. A nil hinge must clear the effect.
An iPad or ordinary iPhone still gets an appropriate single canvas or list.

Keep one selected sound and one audio owner above presentation changes.
Observe transitions only when the active region or available geometry changes;
angle-only updates must not rebuild or repartition the grid. Physical-device
testing must establish the comfortable motion range and any smoothing needed.
Do not assume a fixed angle range, update rate, or angle-derived state boundary.

The existing native `SoundboardView` still uses a split arrangement at regular
width. Implementing this proposal will require removing that unconditional
split for the fully open presentation. This preview does not claim that the
native change has shipped.

## Geometry correction from the supplied screenshot

The user supplied a 2853 by 2007 pixel screenshot. At the simulator's 3x
rendering scale, this is the same 951 by 669 point inner canvas used by the
preview. The outer dimensions were correct; the usable content area was not.

On November 5, iOS 27.1 became available locally. Inspected the existing
2.3 (7) simulator build on a new iPhone Duo instance, in open and partially
open poses, at the standard and extra-small text settings:

- The landscape navigation area occupies about 86 pt above the soundboard.
  The previous HTML incorrectly started the soundboard at the top edge.
- The trailing system bar occupies 84 pt. The gallery therefore has about
  867 by 583 pt before its own padding and headings.
- The observed partial layout leaves approximately 40 pt around the physical
  center, including the region's margins. The older mock used 27 pt.
- The current native build scrolls and clips its bottom row in this viewport.
  The fitted five-row candidate requires more height than is available.
  Standard text shows three columns per pane; extra-small text shows four
  in the fully open pose, matching the supplied screenshot's column count.

The revised HTML includes that navigation area and observed fold clearance.
Book mode uses four columns and four rows per pane. The leading pane leaves
its final cell unused; the trailing pane reserves its fourth row for the ad.
Fully open uses eight columns and four rows. The cat uses top whitespace
instead of adding another row to the grid. These dimensions are preview
fixtures, not device constants to hardcode in the native layout.

The native crop has been reproduced, not fixed in the app by this HTML change.
The next native change must choose a fitted candidate from the actual
available width and height, then verify every card and the ad after folding.

## Preview

Run `python3 tool/serve_iphone_duo_preview.py` and open
`/?pose=book&fold=50`. Drag the opening slider or use the three state buttons.
The half-open direction selector also previews the horizontal fold.

The slider is a normalized design input. Its two endpoints simulate the
single and fully open states; interior values simulate the partially open
state. These are browser controls, not real hardware thresholds. Within the
partial state, only the decorative image mesh changes. There is no animation
clock, playback loop, or repeated grid layout.

## Artwork revision

On November 6, the user clarified the coat and camera. The earlier SVG was
hand-drawn code, not an image generation result. It is now replaced by a
transparent raster illustration from the built-in image generation tool.
The tool did not expose a model version; do not describe it as version 2.6.

The source is [tuxedo-cat-belly-v1.png](../../design/iphone-duo-assets/tuxedo-cat-belly-v1.png).
The [exact generation prompt](../../design/iphone-duo-assets/tuxedo-cat-belly-prompt.md)
records the coat, fixed overhead camera, anatomy, style, and transparency.
The original 1774 by 887 RGBA pixels are preserved without background removal
or recoloring. The texture is about 1.9 MB and is served by the preview exporter.

The browser uses a small WebGL mesh to move the four paws inward at the
half-open midpoint and outward when fully open. This is a 2D pose study,
not a 3D skeleton. The same texture supplies every frame, so the cat does not
fade between inconsistent characters. If WebGL is unavailable or its context
is lost, the original image remains visible. Reduce Motion uses a fixed pose.
This renderer is confined to the HTML preview.

## Validation

Checked the HTML in Chrome on November 5, 2026:

- Moving within the partial range changes the decorative pose without moving any cat button.
  Returning to an earlier input reproduces the same pose. Leaving the control still
  preserves its pose.
- A selected sound remains selected through partial, full, single, and
  tabletop presentations. Each presentation retains all 27 sound buttons.
- Full open has one visible gallery heading, eight columns, no fold, and a
  visible black-and-white cat with a white belly.
- Book, tabletop, and full-open layouts have no grid or label overflow in
  English, Simplified Chinese, or Japanese. The sample ad remains 50 px high.
- The decorative cat overlaps no sound button or ad in either split direction.
- Emulated Reduce Motion keeps a static decorative pose as the slider moves.
  Large text hides the decoration and allows scrolling.
- Hiding the sample ad preserves its slot. A 390 px browser viewport has no
  horizontal page overflow and keeps the opening slider reachable.

Checked the raster revision locally on November 6, 2026:

- The generated PNG has a transparent alpha channel and the intended tuxedo
  coat and overhead belly view. It loads in the preview from the exported assets.
- A 50 to 80 to 50 input sequence changes the rendered pixels, returns to the
  exact initial screenshot pixels, and leaves every sound button in place.
- Book, tabletop, and fully open layouts retain all 27 cards with no grid
  overflow. The artwork overlaps no card, gallery heading, or 50 px ad.
- Emulated Reduce Motion keeps the rendered pixels identical from 50 to 80.
  Single-screen and accessibility-text presentations hide the decoration.
- The local fallback check keeps the image active when WebGL is unavailable.
  JavaScript syntax and whitespace checks pass; Chrome reports no warnings
  or errors. No GitHub Actions run was used.

Hardware sensor mapping and the new native layout transitions remain
unimplemented. The native build was manually inspected in the Duo simulator;
no new native build, native test-suite run, or TestFlight upload is claimed.

## Sources

- [Hinge and scenes skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-hinge-and-scenes/SKILL.md)
- [Adaptive layout skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-adaptive-layout/SKILL.md)
- [DeviceHinge](https://developer.apple.com/documentation/swiftui/devicehinge)
- [ReservedRegion](https://developer.apple.com/documentation/swiftui/reservedregion)
