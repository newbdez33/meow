"use strict";

function createHingeCatRenderer(container) {
  const image = container.querySelector("img");
  const canvas = container.querySelector("canvas");
  let draw;
  let progress = 0.5;

  function showStill() {
    draw = null;
    container.dataset.renderer = "image";
  }

  function initialize() {
    const gl = canvas.getContext("webgl", { alpha: true, antialias: true });
    if (!gl) return;

    function compile(type, source) {
      const shader = gl.createShader(type);
      gl.shaderSource(shader, source);
      gl.compileShader(shader);
      if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        throw new Error(gl.getShaderInfoLog(shader));
      }
      return shader;
    }

    const vertex = compile(gl.VERTEX_SHADER, `
      attribute vec2 point;
      uniform float fold;
      varying vec2 uv;
      vec2 paw(vec2 center, vec2 movement) {
        vec2 offset = point - center;
        float distance = length(offset / vec2(0.21, 0.38));
        float weight = 1.0 - smoothstep(0.18, 1.0, distance);
        weight *= smoothstep(0.06, 0.32, abs(point.y - 0.5));
        return movement * weight;
      }
      void main() {
        uv = point;
        vec2 position = point;
        position += fold * (
          paw(vec2(0.215, 0.12), vec2(0.075, 0.13)) +
          paw(vec2(0.205, 0.88), vec2(0.075, -0.13)) +
          paw(vec2(0.845, 0.15), vec2(-0.075, 0.12)) +
          paw(vec2(0.845, 0.87), vec2(-0.075, -0.12))
        );
        float body = smoothstep(0.25, 0.42, point.x)
          * (1.0 - smoothstep(0.75, 0.9, point.x));
        position.y = 0.5 + (position.y - 0.5) * (1.0 - 0.08 * fold * body);
        gl_Position = vec4((position.x * 2.0 - 1.0) * 0.94,
          (1.0 - position.y * 2.0) * 0.94, 0.0, 1.0);
      }
    `);
    const fragment = compile(gl.FRAGMENT_SHADER, `
      precision mediump float;
      uniform sampler2D artwork;
      varying vec2 uv;
      void main() { gl_FragColor = texture2D(artwork, uv); }
    `);
    const program = gl.createProgram();
    gl.attachShader(program, vertex);
    gl.attachShader(program, fragment);
    gl.linkProgram(program);
    gl.deleteShader(vertex);
    gl.deleteShader(fragment);
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      throw new Error(gl.getProgramInfoLog(program));
    }
    gl.useProgram(program);

    const points = [];
    const columns = 64;
    const rows = 32;
    for (let row = 0; row < rows; row += 1) {
      for (let column = 0; column < columns; column += 1) {
        const x = column / columns;
        const y = row / rows;
        const right = (column + 1) / columns;
        const bottom = (row + 1) / rows;
        points.push(x, y, right, y, x, bottom, x, bottom, right, y, right, bottom);
      }
    }
    gl.bindBuffer(gl.ARRAY_BUFFER, gl.createBuffer());
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(points), gl.STATIC_DRAW);
    const point = gl.getAttribLocation(program, "point");
    gl.enableVertexAttribArray(point);
    gl.vertexAttribPointer(point, 2, gl.FLOAT, false, 0, 0);
    gl.bindTexture(gl.TEXTURE_2D, gl.createTexture());
    gl.pixelStorei(gl.UNPACK_PREMULTIPLY_ALPHA_WEBGL, true);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.viewport(0, 0, canvas.width, canvas.height);
    const fold = gl.getUniformLocation(program, "fold");
    draw = () => {
      const amount = Math.max(0, (progress - 0.5) * 2);
      const spread = amount * amount * (3 - 2 * amount);
      const curl = Math.max(0, 1 - progress * 2);
      gl.uniform1f(fold, 1 - spread + curl * 0.15);
      gl.clear(gl.COLOR_BUFFER_BIT);
      gl.drawArrays(gl.TRIANGLES, 0, points.length / 2);
    };
    container.dataset.renderer = "webgl";
    draw();
  }

  showStill();
  canvas.addEventListener("webglcontextlost", showStill);
  image.decode().then(initialize).catch(showStill);

  return (value) => {
    progress = Math.max(0, Math.min(1, value));
    container.dataset.renderedProgress = String(progress);
    if (draw) draw();
  };
}
