# Meow play ideas for iPhone Duo

## Recommendation

Start with **Meow Accordion**: two cat voices form a small instrument, and
opening or closing the hinge within a comfortable partially open range bends
their pitch. A touch slider provides the same effect on other devices.

The product goal is a toy that someone can understand in a few seconds and
show to a friend. Reuse the existing cat artwork and sound catalogue. Keep the
soundboard as the main utility, with a separate play entry point.

| Concept | Interaction | Duo capability | Scope |
| --- | --- | --- | --- |
| Meow Accordion | Start one or two voices, then bend their pitch with the hinge | Continuous hinge angle, two touch regions, tabletop play | Recommended first feature; requires a shared audio mixer |
| Cardboard Hide-and-Seek | Opening reveals eyes, then a cat; a treat tap makes it jump out | Hinge status and continuous reveal effects | More character animation and reveal states |
| Cat Portrait Studio | Show an attention cue near the camera while the owner frames a photo | Camera capture content on the outer display, main UI inside | New capture flow, permissions, and device checks |

These are product proposals. They are not part of TestFlight 2.3 (7).

## Meow Accordion

1. Enter play mode and choose two voices from the existing catalogue.
2. Tap a cat paw to start a voice; tap again to stop. Holding a pad can be
   added as a second interaction after testing the grip on a real device.
3. Gently change the hinge angle to bend the active voices. Cat movement and
   speech marks show the effect even with the volume down.
4. Stop, leave the mode, close the device, or put the app in the background
   to stop playback. Opening a device must never start audio by itself.

| Available presentation | Experience |
| --- | --- |
| Book | A cat and its voice pad on each side; both controls stay away from the fold |
| Tabletop | A shared cat stage above and stable voice pads below; keep the touch pitch control available |
| Open flat | Keep both voices and reset hinge modulation to neutral; touch modulation still works |
| Compact or no hinge | Keep both voices with a touch slider and a layout that fits the window |

First release: two voices, pitch control, play/stop, animation, and pose
continuity. Defer recording, export, beat loops, scorekeeping, and additional
characters until the basic interaction works well. A proposed play entry
point can live beside the existing soundboard. Keep ads in the soundboard
rather than between performance controls; this placement needs product review.

## Hardware and audio boundaries

Checked the local Xcode 27.2 SwiftUI interface and Apple's reference on
November 4, 2026:

- `DeviceHinge` exposes an angle and a system-provided status. Changes arrive
  through `onHingeChange(isEnabled:_:)`, available from iOS 27.1.
- The hinge can be absent. Use the system status rather than calculating
  closed, partially open, or fully open from invented angle thresholds.
- Calibrate and smooth the effect on a physical device. Do not assume an
  update rate, exact precision, or a fixed 0–180 degree hardware range.
- Use hinge values for the effect. Use available space, arrangements, and
  reserved regions for layout; controls must not track every angle update.
- A camera capture accessory is conditional on an active capture session and
  system availability. It is not a general-purpose second-screen channel.

The current `Sounds` owner stops one `AVAudioPlayer` before starting another.
Two simultaneous voices therefore need deliberate audio work, not a second
independent player owner. Extend the shared owner with a play-mode mixer and
independent pitch processing. Keep one audio-session policy, and define how a
widget request interrupts play mode. Preserve interruption, route-change,
foreground, and media-reset cleanup. Validate these boundaries before shipping.

The portrait concept should start with photos and an explicit shutter. Do not
add recording, automatic capture, or an assumption that a sound makes every
cat look toward the camera. Keep all capture controls on the main display.

## Interactive prototype

Run the existing preview server and open `/play-lab.html`:

```sh
python3 tool/serve_iphone_duo_preview.py --bind 127.0.0.1 --port 8801
```

The prototype includes two actual bundled sounds, independent voice toggles,
a normalized bend slider, a six-second demo, book/tabletop presentations,
keyboard controls, and playback cleanup on stop or page hiding. Audio begins
only after an explicit interaction. Cat animation respects reduced motion.

The slider simulates the interaction; it does not read a hinge. Web Audio
changes playback rate, so pitch and speed change together. The native proposal
calls for independent pitch control. The 0–100 control is a design input, not
a claim about the hardware's angle range. The other two concept cards are
descriptions, not implemented camera or animation features.

## Prototype validation

Checked in Chrome on November 4, 2026:

- Both bundled sounds decode and enter playback. Each voice can start and
  stop while the other continues. The stop button clears both voices.
- The six-second demo starts both voices, changes the bend input, then stops
  both and returns the input to neutral.
- Manual bend input updates the pitch label. Switching between book and
  tabletop preserves the active voice.
- Both presentations fit a 390 px viewport without horizontal overflow.
  Voice pads remain at least 60 px high, and all cat images load.
- The browser reports no warnings or errors during these checks. JavaScript
  syntax, Python syntax, and whitespace checks pass.

Background cleanup is implemented but was not verified: creating or focusing
another browser tab did not produce an observed hidden state in this session.
Actual sound quality, independent native pitch, and hinge response still need
listening and physical-device checks.

## Sources

- Applied the [hinge and scenes skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-hinge-and-scenes/SKILL.md)
  and [games skill](https://github.com/mirzaaghazadeh/iphone-duo-skills/blob/c3fa17c363ff555c16e44e3e94f16929fcab9646/skills/iphone-duo-games/SKILL.md).
- [DeviceHinge](https://developer.apple.com/documentation/swiftui/devicehinge)
- [onHingeChange](https://developer.apple.com/documentation/swiftui/view/onhingechange(isenabled:_:))
- [CameraCaptureAccessory](https://developer.apple.com/documentation/swiftui/cameracaptureaccessory)
- [Designing for iPhone Duo](https://developer.apple.com/design/human-interface-guidelines/designing-for-iphone-duo)
