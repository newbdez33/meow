"use strict";

function hingeCatPose(progress) {
  const opening = Math.max(0, Math.min(1, (progress - 0.5) * 2));
  const spread = opening * opening * (3 - 2 * opening);
  const curl = Math.max(0, 1 - progress * 2);
  const fold = 1 - spread;
  const back = {
    x: 100 + 152 * spread,
    y: 36 + 14 * spread,
    width: 182 * fold,
    height: 168 - 28 * spread,
  };
  const hipX = back.x + back.width * 0.42;
  const hipY = 102 - 12 * fold;
  const pawX = 92 + 244 * spread - 6 * curl;
  const pawY = 32 + 8 * spread - 2 * curl;
  return {
    spread,
    back,
    bodyWidth: 285 - 48 * fold,
    upper: { hip: [hipX, hipY], paw: [pawX, pawY] },
    lower: { hip: [hipX, 240 - hipY], paw: [pawX, 240 - pawY] },
  };
}

function createHingeCatRenderer(container) {
  const image = container.querySelector("img");
  const curledImage = container.querySelector("[data-curled-artwork]");
  const canvas = container.querySelector("canvas");
  const context = canvas.getContext("2d");
  let ready = false;
  let progress = 0.5;

  // The fixed paw section keeps the pads round while the black leg changes length.
  function drawLeg(pose, source) {
    const [hx, hy] = pose.hip;
    const [px, py] = pose.paw;
    const scale = 0.21;
    const dy = (source.paw[1] - source.hip[1]) * scale;
    const distance = Math.hypot(px - hx, py - hy);
    const dx = Math.sqrt(Math.max(1, distance * distance - dy * dy));
    const angle = Math.atan2(py - hy, px - hx) - Math.atan2(dy, dx);
    const left = (source.x - source.hip[0]) * scale;
    const top = (source.y - source.hip[1]) * scale;
    const pawLeft = dx - (source.paw[0] - source.split) * scale;
    const height = source.height * scale;
    const overlap = 12;
    context.save();
    context.translate(hx, hy);
    context.rotate(angle);
    context.drawImage(image, source.x, source.y,
      source.split - source.x + overlap, source.height,
      left, top, pawLeft - left + overlap * scale, height);
    context.drawImage(image, source.split, source.y,
      source.x + source.width - source.split, source.height,
      pawLeft, top, (source.x + source.width - source.split) * scale, height);
    context.restore();
  }

  function draw() {
    if (!ready || !context) return;
    const pose = hingeCatPose(progress);
    context.setTransform(canvas.width / 400, 0, 0, canvas.height / 240, 0, 0);
    context.clearRect(0, 0, 400, 240);
    drawLeg(pose.upper, {
      x: 678, y: 214, width: 528, height: 210,
      hip: [750, 333], paw: [1110, 306], split: 940,
    });
    drawLeg(pose.lower, {
      x: 62, y: 850, width: 512, height: 212,
      hip: [137, 932], paw: [479, 981], split: 310,
    });

    const headWidth = 258 * 285 / 558;
    context.drawImage(image, 42, 183, 258, 276, 40, 50, headWidth, 140);
    context.drawImage(image, 300, 183, 300, 276,
      40 + headWidth, 50, pose.bodyWidth - headWidth, 140);

    if (pose.back.width > 0) {
      const back = pose.back;
      // The folded torso covers the abdomen; its rump mark turns with the sprite.
      context.fillStyle = "#242222";
      context.beginPath();
      context.ellipse(back.x + back.width * 0.35, 120,
        back.width * 0.18, back.height * 0.24, 0, 0, Math.PI * 2);
      context.fill();
      context.drawImage(curledImage, 1120, 157, 490, 604,
        back.x, back.y, back.width, back.height);
    }

    // Only the face and front paws sit in front of the tightly curled lower body.
    context.save();
    context.beginPath();
    context.rect(40, 50, 106 + (headWidth - 106) * pose.spread, 140);
    context.moveTo(166, 85);
    context.ellipse(143, 85, 23, 25, 0, 0, Math.PI * 2);
    context.moveTo(166, 165);
    context.ellipse(143, 165, 23, 25, 0, 0, Math.PI * 2);
    context.clip();
    context.drawImage(image, 42, 183, 258, 276, 40, 50, headWidth, 140);
    context.restore();
    container.dataset.renderer = "canvas";
  }

  container.dataset.renderer = "image";
  Promise.all([image.decode(), curledImage.decode()])
    .then(() => { ready = true; draw(); }).catch(() => {});
  canvas.addEventListener("contextlost", () => {
    ready = false;
    container.dataset.renderer = "image";
  });
  canvas.addEventListener("contextrestored", () => { ready = true; draw(); });

  return (value) => {
    progress = Number.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0.5;
    container.dataset.renderedProgress = String(progress);
    draw();
  };
}
