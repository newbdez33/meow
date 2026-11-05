# Meow: three states and a cat that follows the fold

## Accepted direction

Keep the soundboard. Use three presentations: single screen, partially open
split, and fully open canvas. Add one small decorative cat in the partially
open state. Its stretch follows the hinge continuously in both directions.
There is no separate play mode, timer, audio effect, or camera feature.

This document replaces the November 4 play concepts. The HTML is a design
preview; the native app and TestFlight 2.3 (7) have not changed.

| State | Layout | Decoration |
| --- | --- | --- |
| Single screen | One scrolling list of the existing 27 cats | Hidden |
| Partially open | 15 cats in one pane; 12 and the reserved ad row in the other | Small cat curls or stretches with the fold |
| Fully open | One heading and one continuous six-column grid; the ad occupies the final three cells | Hidden; no center divider |

Standard text fits all 27 cats in both inner-display presentations at the
preview sizes. Large text keeps scrolling. The same selected cat and audio
owner survive every layout change.

## Motion

The cat sits near the fold in a small clear area. In a book presentation it
uses space below the two headings; in a tabletop presentation it uses space
between the panes. It is decorative, accepts no input, and covers no cat
button or ad. Interactive controls stay outside the fold region.

- As the device opens, the head moves forward, front paws extend, the back
  lowers, and the tail uncurls. Closing reverses the same pose.
- Stop moving the device and the pose stays still. Do not start a timed
  animation on a pose transition or add an idle loop.
- Fade the decoration near the ends of the illustrated partial range. Reset
  and hide it outside the partially open state.
- Under Reduce Motion, show a static cat only in the partial state. At large
  accessibility text sizes, omit the decoration and keep the content readable.
- Folding alone does not play a sound. A cat button still plays its original
  sound without changing pitch or adding another voice.

## Native implementation boundary

Follow the user's three-state product model without deriving layout from a
stream of angle thresholds. Read active division regions and available space
for layout. Use the system hinge status to enable the decoration, then map
the continuous hinge angle to its pose. A nil hinge must clear the effect.
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

## Preview

Run `python3 tool/serve_iphone_duo_preview.py` and open
`/?pose=book&fold=50`. Drag the opening slider or use the three state buttons.
The half-open direction selector also previews the horizontal fold.

The slider is a normalized design input. Its two endpoints simulate the
single and fully open states; interior values simulate the partially open
state. These are browser controls, not real hardware thresholds. Within the
partial state, only SVG paths and transforms change. There is no animation
clock, playback loop, or repeated grid layout.

## Validation

Checked the HTML in Chrome on November 5, 2026:

- Moving from 20 to 80 changes the SVG pose without moving any cat button.
  Returning to 20 reproduces the exact same path. Leaving the control still
  preserves its pose.
- A selected sound remains selected through partial, full, single, and
  tabletop presentations. Each presentation retains all 27 sound buttons.
- Full open has one visible heading, six columns, and no fold or decoration.
- Book, tabletop, and full-open layouts have no grid or label overflow in
  English, Simplified Chinese, or Japanese. The sample ad remains 50 px high.
- The decorative cat overlaps no sound button or ad in either split direction.
- Emulated Reduce Motion keeps a static SVG pose as the slider moves.
  Large text hides the decoration and allows scrolling.
- Hiding the sample ad preserves its slot. A 390 px browser viewport has no
  horizontal page overflow and keeps the opening slider reachable.

Hardware sensor mapping and native layout transitions remain unimplemented.
No native test run or new TestFlight upload is claimed for this design change.

## Sources

- [Hinge and scenes skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-hinge-and-scenes/SKILL.md)
- [Adaptive layout skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-adaptive-layout/SKILL.md)
- [DeviceHinge](https://developer.apple.com/documentation/swiftui/devicehinge)
- [ReservedRegion](https://developer.apple.com/documentation/swiftui/reservedregion)
