# Flat tuxedo cat and folded pose

Generated with the built-in image tool on November 6, 2026. The tool did not
expose a model version. Both PNG files retain the original pixels and alpha.

The character uses the app's c02, c05, and c06 cat icons as style references.
The rig atlas has a body, two separate hind legs, and a folded pose reference.
Runtime drawing uses the atlas directly; it does not crop or rewrite the file.

Files: [character reference](tuxedo-cat-flat-v2.png) and
[animation atlas](tuxedo-cat-rig-v2.png).

## Character prompt

Use case: stylized-concept.
Asset type: a transparent 2D sticker mascot that must belong to the SAME icon family as the three supplied Meow app cat images.
Input image 1 is the black-cat coat, face and drawing-style reference. Input images 2 and 3 are exact stroke, flat coloring and simplified-shape style references. Do not copy their poses.
Draw ONE black-and-white TUXEDO CAT lying on its back with its white belly facing the viewer. Fixed strict overhead camera, not a side view. Horizontal composition: head at LEFT, hips at RIGHT. The whole body is visible. Two small front paws are bent gently beside the cheeks/chest, and two longer hind legs are relaxed outward toward the two right corners, showing white feet with pink paw pads. Exactly four paws. The face must be clearly visible, with black ears and head, a white muzzle and small white nose bridge, cheerful yellow eyes and a small pink nose. Black body sides and tail, continuous warm-white chest and belly. No scattered cow spots. One black tail curls off the rump.
MATCH THE REFERENCES CLOSELY: flat cartoon sticker drawing, chunky smooth dark-gray outlines, very simple rounded anatomy, solid matte fills, one sparse flat shadow shape, the same bold readability and playful expression as the supplied cats. Keep the head large enough for the face to read at icon size. Black fill, warm off-white belly and socks, restrained pink pads, yellow eyes. The result should look like another member of this existing cat icon pack.
NO rendered fur, NO individual hairs, NO painterly texture, NO soft realistic shading, NO gradients, NO plush toy, NO photographic realism, NO 3D, NO thin sketchy lines. No clothes or police accessories.
Truly transparent alpha background. No ground or cast shadow. No labels, words, watermark, surrounding decorations or extra cats. Leave generous transparent padding around all paws and ears.

## Rig prompt

Input image 1 is `tuxedo-cat-flat-v2.png`; images 2 and 3 are c02 and c05.

Use case: stylized-concept.
Asset type: transparent production sprite atlas for a foldable black-and-white cat sticker.
Input image 1 is the exact character identity and simplified cartoon style to retain. Input images 2 and 3 are the app's original flat cat stickers and are the final style authority.
Create ONE square image divided invisibly into an exact 2-by-2 grid of equal cells. Each cell contains one isolated item with generous empty padding. No visible grid, no labels or text. Keep every pixel of one item inside its own cell and keep all four cell borders fully transparent.
TOP LEFT CELL: the cat's body, head, TWO SMALL FRONT PAWS, and curled tail, from the exact horizontal overhead belly-up view in image 1. Head on the left, rump and tail on the right. REMOVE BOTH HIND LEGS ENTIRELY from this body part; complete the outline of the rounded black rump smoothly. Keep the entire white belly, black head, white muzzle, big yellow eyes, pink nose and playful smile. Front paws stay next to the cheeks. This body part is one continuous horizontal sticker. No hind feet or stumps.
TOP RIGHT CELL: one isolated long UPPER HIND LEG for that same cat. Draw it horizontally: rounded BLACK HIP ROOT at LEFT, a long supple black leg extending to the RIGHT, and a large WHITE SOCK PAW with visible pink paw pads at the RIGHT TIP. A clean, gently tapered limb silhouette; use the same bold outlines as the body. Pink pads face the camera. No other body parts.
BOTTOM LEFT CELL: the matching LOWER HIND LEG, again horizontally with BLACK HIP ROOT at LEFT and WHITE SOCK PAW with pink pads at RIGHT. Mirror the gentle curve vertically relative to the top-right leg. Same length, scale and style.
BOTTOM RIGHT CELL: a complete pose reference of this SAME cat curled on its back like a folded book. The two LONG HIND LEGS lift up and fold forward toward the HEAD, spreading widely in a V. The two rear paws reach the LEFT side of the composition, one ABOVE the head and one BELOW it, framing the face. The yellow-eyed cat face remains COMPLETELY VISIBLE BETWEEN THE REAR LEGS. Its two small front paws stay tucked near the cheeks. Rump and belly curl toward the head. Fixed overhead belly-up view, never seated and never side profile. Exactly four paws total, one tail. This is a pose reference, not additional detachable parts.
STYLE: flat 2D cartoon sticker, solid black and warm off-white fills, bold dark gray outlines, a single flat beige shadow shape if needed, simple pink pads. Match original Meow icons. No fur texture, no realistic rendering, no gradients, no diffuse lighting, no glossy highlights. IMPORTANT: remove ALL glow and ALL shadow surrounding the character. Each sticker ends exactly at its crisp dark outline; outside the outline must be alpha zero. Do not add a white sticker border. Transparent alpha background throughout, not gray/black/white backdrop. No words, watermark, costumes, scenery or extra items.
