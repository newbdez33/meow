"use strict";

function hingeCatPose(progress) {
  const opening = Math.max(0, Math.min(1, (progress - 0.5) * 2));
  const spread = opening * opening * (3 - 2 * opening);
  const curl = Math.max(0, 1 - progress * 2);
  const fold = 1 - spread;
  const turn = Math.PI * spread;
  const back = {
    center: 191 + 54 * spread,
    radius: 91 - 20 * spread,
    y: 36 + 21 * spread,
    height: 168 - 42 * spread,
    turn,
  };
  const hipX = back.center - 14.56 * Math.cos(turn);
  const hipY = 102 - 12 * fold;
  const pawX = hipX - (84.44 - 17.44 * spread) * Math.cos(turn) - 6 * curl;
  const pawY = hipY - (58 - 17 * spread + 15 * Math.sin(turn)) - 2 * curl;
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
  const thighImage = container.querySelector("[data-thigh-artwork]");
  const canvas = container.querySelector("canvas");
  const context = canvas.getContext("2d");
  let backRows = [];
  let ready = false;
  let progress = 0.5;

  function readBackContour() {
    const sample = document.createElement("canvas");
    sample.width = 490;
    sample.height = 604;
    const pixels = sample.getContext("2d", { willReadFrequently: true });
    pixels.drawImage(curledImage, 1120, 157, 490, 604, 0, 0, 490, 604);
    const data = pixels.getImageData(0, 0, 490, 604).data;
    backRows = [];
    for (let y = 0; y < 604; y += 2) {
      let left = 490;
      let right = 0;
      const height = Math.min(2, 604 - y);
      for (let row = y; row < y + height; row++) {
        for (let x = 0; x < 490; x++) {
          if (data[(row * 490 + x) * 4 + 3] > 32) {
            left = Math.min(left, x);
            right = Math.max(right, x + 1);
          }
        }
      }
      if (right > left) backRows.push({ y, height, left, right });
    }
  }

  // The fixed paw section keeps the pads round while the black leg changes length.
  function drawLeg(pose, source, spread) {
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
    const thigh = Math.min(1, spread / 0.2);
    const thighBlend = thigh * thigh * (3 - 2 * thigh);
    context.drawImage(image, source.x, source.y,
      source.split - source.x + overlap, source.height,
      left, top, pawLeft - left + overlap * scale, height);
    if (thighBlend > 0) {
      const thickness = 18 + 36 * thighBlend + 6 * Math.sin(Math.PI * spread);
      context.drawImage(thighImage, 175, source.thighY, 890, 400,
        -32, dy - thickness * source.ankleY, dx + 16, thickness);
    }
    context.drawImage(image, source.split, source.y,
      source.x + source.width - source.split, source.height,
      pawLeft, top, (source.x + source.width - source.split) * scale, height);
    context.restore();
  }

  function drawBack(back) {
    const curve = 0.7;
    const edgeDepth = Math.sqrt(1 - curve * curve);
    const visible = Math.max(0, Math.min(1, (1 + Math.cos(back.turn) / curve) / 2));
    if (visible <= 0.2) return;
    const project = (u) => (Math.sin(Math.asin(curve * (2 * u - 1)) + back.turn)
      - Math.sin(back.turn) * edgeDepth) / curve;
    const bridge = Math.max(0, Math.cos(back.turn));
    if (bridge > 0) {
      context.fillStyle = "#242222";
      context.beginPath();
      context.ellipse(back.center + back.radius * project(0.35),
        120, 32.76 * bridge, back.height * 0.24, 0, 0, Math.PI * 2);
      context.fill();
    }
    if (back.turn === 0) {
      context.drawImage(curledImage, 1120, 157, 490, 604,
        100, 36, 182, 168);
      return;
    }

    // Each row follows the rounded silhouette, so the turning edge stays curved.
    // Hide the last edge as the rump turns behind the underside.
    const edge = Math.max(0, Math.min(1, (visible - 0.2) / 0.15));
    context.save();
    context.globalAlpha = edge * edge * (3 - 2 * edge);
    const strips = 20;
    for (const row of backRows) {
      const width = row.right - row.left;
      const center = back.center + ((row.left + row.right) / 490 - 1) * back.radius;
      const radius = width / 490 * back.radius;
      for (let i = 0; i < strips; i++) {
        const u = visible * i / strips;
        const v = visible * (i + 1) / strips;
        const left = center + radius * project(u);
        const right = center + radius * project(v);
        context.drawImage(curledImage, 1120 + row.left + width * u, 157 + row.y,
          width * (v - u), row.height, left, back.y + row.y / 604 * back.height,
          right - left + 0.2, row.height / 604 * back.height + 0.15);
      }
    }
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
      thighY: 200, ankleY: 0.58,
    }, pose.spread);
    drawLeg(pose.lower, {
      x: 62, y: 850, width: 512, height: 212,
      hip: [137, 932], paw: [479, 981], split: 310,
      thighY: 700, ankleY: 0.44,
    }, pose.spread);

    const headWidth = 258 * 285 / 558;
    context.drawImage(image, 42, 183, 258, 276, 40, 50, headWidth, 140);
    context.drawImage(image, 300, 183, 300, 276,
      40 + headWidth, 50, pose.bodyWidth - headWidth, 140);

    drawBack(pose.back);

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
  Promise.all([image.decode(), curledImage.decode(), thighImage.decode()])
    .then(() => { readBackContour(); ready = true; draw(); }).catch(() => {});
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
