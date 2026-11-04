"use strict";

const $ = (selector) => document.querySelector(selector);
const voices = {
  left: { name: "小奶喵", file: "assets/m_005.mp3", wanted: false, playing: false, ticket: 0 },
  right: { name: "大橘喵", file: "assets/m_004.mp3", wanted: false, playing: false, ticket: 0 },
};
const buffers = new Map();
let audio;
let fold = 50;
let demoActive = false;
let demoTicket = 0;
let demoFrame;
let demoTimer;
let audioError = "";

function renderVoices() {
  for (const [id, voice] of Object.entries(voices)) {
    const state = voice.playing ? "playing" : voice.wanted ? "loading" : "idle";
    $(`#performer-${id}`).dataset.active = String(voice.playing);
    $(`#performer-${id}`).dataset.audioState = state;
    $(`#voice-${id}`).setAttribute("aria-pressed", String(voice.wanted));
    $(`#voice-${id} span`).textContent = `${voice.name} · ${voice.wanted ? "休息" : "开唱"}`;
    $(`#bubble-${id}`).textContent = voice.playing ? "喵～～～" : voice.wanted ? "准备开唱…" : "等你开唱";
  }
  const playing = Object.values(voices).filter((voice) => voice.playing).length;
  const loading = Object.values(voices).some((voice) => voice.wanted && !voice.playing);
  $("#audio-status").textContent = audioError || (loading ? "正在准备声音…" : playing === 2
    ? "双猫合奏中 · 拖动滑杆听听变化" : playing === 1 ? "一只猫开唱了 · 另一只也可以加入" : "猫咪休息中 · 点击才会播放声音");
  $("#demo").setAttribute("aria-pressed", String(demoActive));
  $("#demo").textContent = demoActive ? "正在试听 · 6 秒后停止" : "听 6 秒合奏 ↗";
}

function playbackRate() {
  const semitones = (fold - 50) / 50 * 5;
  return 2 ** (semitones / 12);
}

function setFold(value) {
  fold = Math.max(0, Math.min(100, Number(value)));
  $("#fold-control").value = String(Math.round(fold));
  $("#duo").style.setProperty("--bend", String((fold - 50) / 50));
  const label = fold < 33 ? "低低的喵" : fold > 67 ? "尖尖的喵" : "自然的喵";
  $("#pitch-label").textContent = label;
  $("#fold-control").setAttribute("aria-valuetext", label);
  if (!audio) return;
  for (const voice of Object.values(voices)) {
    if (!voice.source) continue;
    voice.source.playbackRate.setTargetAtTime(playbackRate(), audio.currentTime, 0.035);
  }
}

async function loadBuffer(file) {
  if (!buffers.has(file)) {
    buffers.set(file, fetch(file).then(async (response) => {
      if (!response.ok) throw new Error(`Audio resource: ${response.status}`);
      return audio.decodeAudioData(await response.arrayBuffer());
    }).catch((error) => { buffers.delete(file); throw error; }));
  }
  return buffers.get(file);
}

async function startVoice(id) {
  const voice = voices[id];
  const ticket = ++voice.ticket;
  voice.wanted = true;
  audioError = "";
  renderVoices();
  try {
    const AudioContextType = window.AudioContext || window.webkitAudioContext;
    if (!AudioContextType) throw new Error("Web Audio is unavailable");
    audio ??= new AudioContextType();
    await audio.resume();
    const buffer = await loadBuffer(voice.file);
    if (ticket !== voice.ticket || !voice.wanted || document.hidden) return false;
    const source = audio.createBufferSource();
    const gain = audio.createGain();
    source.buffer = buffer;
    source.loop = true;
    source.playbackRate.value = playbackRate();
    gain.gain.setValueAtTime(0, audio.currentTime);
    gain.gain.linearRampToValueAtTime(0.18, audio.currentTime + 0.04);
    source.connect(gain).connect(audio.destination);
    voice.source = source;
    voice.gain = gain;
    source.onended = () => { source.disconnect(); gain.disconnect(); };
    source.start();
    voice.playing = true;
    renderVoices();
    return true;
  } catch (error) {
    if (ticket !== voice.ticket) return false;
    voice.wanted = false;
    voice.playing = false;
    audioError = "声音暂未准备好，请再点一次开唱。";
    console.warn("Audio preview failed:", error.message);
    renderVoices();
    return false;
  }
}

function stopVoice(id) {
  const voice = voices[id];
  ++voice.ticket;
  voice.wanted = false;
  voice.playing = false;
  if (voice.source) {
    const now = audio.currentTime;
    voice.gain.gain.cancelScheduledValues(now);
    voice.gain.gain.setValueAtTime(voice.gain.gain.value, now);
    voice.gain.gain.linearRampToValueAtTime(0, now + 0.04);
    voice.source.stop(now + 0.05);
    voice.source = null;
    voice.gain = null;
  }
  renderVoices();
}

function cancelDemo() {
  ++demoTicket;
  demoActive = false;
  cancelAnimationFrame(demoFrame);
  clearTimeout(demoTimer);
}

function stopAll() {
  cancelDemo();
  for (const id of Object.keys(voices)) stopVoice(id);
}

function toggleVoice(id) {
  cancelDemo();
  if (voices[id].wanted) stopVoice(id);
  else void startVoice(id);
}

async function startDemo() {
  stopAll();
  demoActive = true;
  const ticket = ++demoTicket;
  const ready = await Promise.all(Object.keys(voices).map(startVoice));
  if (ticket !== demoTicket) return;
  if (!ready.every(Boolean)) { stopAll(); return; }
  const start = performance.now();
  demoTimer = setTimeout(() => { stopAll(); setFold(50); }, 6000);
  function animate(now) {
    if (ticket !== demoTicket) return;
    setFold(50 + 42 * Math.sin((now - start) / 1000 * Math.PI / 2));
    demoFrame = requestAnimationFrame(animate);
  }
  renderVoices();
  demoFrame = requestAnimationFrame(animate);
}

function setPose(pose) {
  $("#duo").dataset.pose = pose;
  $("#device-hint").textContent = pose === "tabletop" ? "上方看表演，下方打节拍" : "一边一只猫，一起开唱";
  $("#device-footer").textContent = pose === "tabletop" ? "桌面模式保留触控变调，无需移动手机" : "点亮猫爪，再试试下方的开合滑杆";
  for (const button of document.querySelectorAll("button[data-pose]")) {
    button.setAttribute("aria-pressed", String(button.dataset.pose === pose));
  }
  history.replaceState(null, "", `?pose=${pose}`);
}

for (const id of Object.keys(voices)) {
  $(`#voice-${id}`).addEventListener("click", () => toggleVoice(id));
}
$("#fold-control").addEventListener("input", (event) => {
  cancelAnimationFrame(demoFrame);
  setFold(event.target.value);
});
$("#demo").addEventListener("click", () => { if (demoActive) stopAll(); else void startDemo(); });
$("#stop").addEventListener("click", stopAll);
for (const button of document.querySelectorAll("button[data-pose]")) {
  button.addEventListener("click", () => setPose(button.dataset.pose));
}
document.addEventListener("keydown", (event) => {
  if (event.repeat || event.altKey || event.ctrlKey || event.metaKey || event.target.matches("input, select, textarea, [contenteditable]")) return;
  const id = { KeyA: "left", KeyK: "right" }[event.code];
  if (id) { event.preventDefault(); toggleVoice(id); }
  if (event.code === "Escape") stopAll();
});
document.addEventListener("visibilitychange", () => { if (document.hidden) stopAll(); });
window.addEventListener("pagehide", stopAll);
setPose(new URLSearchParams(location.search).get("pose") === "tabletop" ? "tabletop" : "book");
setFold(50);
