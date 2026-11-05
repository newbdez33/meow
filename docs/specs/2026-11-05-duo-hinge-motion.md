# Meow: three states and a cat that follows the fold

## Accepted direction

Keep the soundboard. Use three presentations: single screen, partially open
split, and fully open canvas. Add one small black-and-white tuxedo cat,
viewed directly from above as it lies on its back. Match the original cat
icons with simple flat fills and bold outlines. Its pose follows the hinge
continuously in both directions.
There is no separate play mode, timer, audio effect, or camera feature.

This document replaces the November 4 play concepts. The HTML is a design
preview; the native app and TestFlight 2.3 (7) have not changed.

| State | Layout | Decoration |
| --- | --- | --- |
| Single screen | One scrolling list of the existing 27 cats | Hidden |
| Partially open | 15 cats in one pane; 12 and the reserved ad row in the other | Lower body curls over the belly; hind legs spread beside the visible face; a small X marks the rump |
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
- At the initial half-open pose, curl the entire lower body toward the head
  like a shrimp. The black back of the rump covers the white belly and chest.
  Keep the hind legs spread in a wide V, with one rear paw on each side of
  the head. Leave the face and the two small front paws visible between them.
- Put a small light-gray X on the exposed rump as a cartoon privacy mark.
  Attach it to the lower body so it turns away with that body part.
- As the screen opens, unfold the lower body and lower both hind legs along
  separate paths. Reveal the white belly gradually. Keep the face and front
  paws fixed. Closing reverses the same motion. The initial pose must hide
  the belly, rather than only moving the legs above a flat torso.
- Stop moving the device and the pose stays still. Do not start a timed
  animation on a pose transition or add an idle loop.
- Fade the decoration only near the closed endpoint. Keep the fully open cat
  visible instead of fading it out. Hide the cat on the outer single screen.
- Under Reduce Motion, keep a static folded-leg pose within the partial state
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
The half-open direction selector also previews the horizontal fold. The
character detail dialog has a larger view and a second slider connected to
the same opening state.

The slider is a normalized design input. Its two endpoints simulate the
single and fully open states; interior values simulate the partially open
state. These are browser controls, not real hardware thresholds. Within the
partial state, only the decorative body and leg placement changes. There is no animation
clock, playback loop, or repeated grid layout.

## Artwork

Use the app's c02, c05, and c06 icons as style references: smooth dark outlines,
flat black and warm-white fills, yellow eyes, and simple pink paw pads. The
current artwork replaces the earlier soft fur rendering. Keep the camera
strictly overhead; the head is at the left and the rump is at the right.

The [base atlas](../../design/iphone-duo-assets/tuxedo-cat-rig-v2.png)
contains a body with two front paws and two separate hind legs. Its old
folded reference is superseded by the
[curled body sheet](../../design/iphone-duo-assets/tuxedo-cat-curled-v3.png):
the left cell shows the complete curl; the right cell isolates the lower
body. Both include the small rump X. The
[character reference](../../design/iphone-duo-assets/tuxedo-cat-flat-v2.png),
[base prompts](../../design/iphone-duo-assets/tuxedo-cat-rig-prompts.md), and
[curled body prompts](../../design/iphone-duo-assets/tuxedo-cat-curled-prompts.md)
are saved with them. These were generated with the built-in image tool,
which did not expose a model version. Do not describe the output as version
2.6. The 1254 by 1254 base atlas and 1774 by 887 curled sheet retain their
original pixels and alpha.

The browser draws the atlas parts with Canvas 2D. Both hip anchors follow
the folded lower body, and each hind leg has a separate paw target. The paw
portion keeps a fixed size while the black leg changes length, so the pads
do not stretch. A black lower-body layer covers the abdomen at the initial
pose, moves toward the rump, and narrows to zero as the cat opens. The X is
part of that layer. It disappears at full open, when the underside faces
the camera. The face and front paws are drawn above this layer and remain
visible. The continuous base body prevents gaps between the head and belly.

This is a layered 2D pose study. The complete curled pose is a static fallback
if Canvas 2D is unavailable or its context is lost. Reduce Motion holds the
folded pose until the fully open state. This renderer and the detail dialog
are confined to the HTML preview.

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

Checked the flat artwork and folded-leg revision locally on November 6, 2026:

- The folded pose places the rear paws on opposite sides of the head. At
  full open they extend past the rump. The face stays visible in the middle.
- Browser screenshots show identical face pixels at 50, 75, and 100. A
  50 to 75 to 100 to 50 sequence restores the exact initial artwork pixels.
  The sound buttons stay fixed while the partial-state input changes.
- The main and enlarged sliders share the same state. Escape closes the
  detail dialog. Its controls fit inside a 390 px browser viewport.
- In English, all 27 cards fit in book, tabletop, and fully open layouts.
  The decoration overlaps no gallery heading, card, or 50 px ad.
- Emulated Reduce Motion keeps the artwork pixels fixed from 50 to 80.
  Single-screen and accessibility-text presentations hide the decoration.
- Local JavaScript checks cover 101 input values: paw sizes and head placement
  stay fixed, paw targets move in the expected direction, and every source
  rectangle stays inside the atlas. Canvas loss, restoration, and unavailable
  context paths pass with a stub context.
- JavaScript syntax and whitespace checks pass. Chrome reports no warnings
  or errors. No GitHub Actions run was used.

Checked the curled-body and rump-X revision locally on November 6, 2026:

- The initial pose hides the white belly and chest while keeping the face
  and four paws visible. At 75 the belly is partly visible; at 100 it is
  fully visible and the rump mark has turned away.
- Browser pixel samples in the abdomen are dark at 50 and warm white at
  75 and 100. A 50 to 100 to 50 sequence restores the exact artwork pixels.
  Reduce Motion keeps the same folded pixels from 50 to 80.
- All 27 cards fit in book, tabletop, and fully open layouts. The ad remains
  50 px high, with no decoration overlap on headings, cards, or the ad.
  Button positions do not change from 50 to 75.
- A local renderer check covers 101 inputs, lower-body hip attachment,
  valid source rectangles, the hidden back layer at full open, context
  loss and restoration, image failure, and unavailable drawing context.
- The served images match the source files. JavaScript syntax and whitespace
  checks pass. Chrome reports no warnings or errors. No GitHub Actions run
  was used.

Hardware sensor mapping and the new native layout transitions remain
unimplemented. The native build was manually inspected in the Duo simulator;
no new native build, native test-suite run, or TestFlight upload is claimed.

## Sources

- [Hinge and scenes skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-hinge-and-scenes/SKILL.md)
- [Adaptive layout skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-adaptive-layout/SKILL.md)
- [DeviceHinge](https://developer.apple.com/documentation/swiftui/devicehinge)
- [ReservedRegion](https://developer.apple.com/documentation/swiftui/reservedregion)
