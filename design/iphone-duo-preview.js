"use strict";

const poses = {
  closed: { width: 466, height: 678, caption: "闭合 · 一点即播的猫咪网格" },
  open: { width: 951, height: 669, caption: "展开 · 左侧看猫咪，右侧选声音" },
  book: { width: 951, height: 669, caption: "书本 · 两个窗格避开中央折痕" },
  tabletop: { width: 669, height: 951, caption: "桌面 · 上方展示，下方操作" },
  "split-left": { width: 475.5, height: 669, caption: "左分屏 · 操作栏位于左侧外缘" },
  "split-right": { width: 475.5, height: 669, caption: "右分屏 · 操作栏位于右侧外缘" },
};

const $ = (selector) => document.querySelector(selector);
const parameters = new URLSearchParams(location.search);
const state = {
  pose: Object.hasOwn(poses, parameters.get("pose")) ? parameters.get("pose") : "open",
  language: ["en", "zh-Hans", "ja"].includes(parameters.get("lang")) ? parameters.get("lang") : "zh-Hans",
  theme: parameters.get("theme") === "dark" ? "dark" : "light",
  text: parameters.get("text") === "large" ? "large" : "standard",
  selected: null,
};
const device = $("#device");
const scrollArea = $("#sound-scroll");
const player = new Audio();
player.preload = "none";
let data;
let scale = 1;
let playAttempt = 0;
let sheetOpener;
const note = "HTML 交互稿 · 系统栏、折痕与窗口尺寸为设计示意；原生截图见下方。";

function copy(key) {
  return data.strings[state.language][key] ?? key;
}

function updateURL() {
  const next = new URLSearchParams({ pose: state.pose, lang: state.language, theme: state.theme, text: state.text });
  if (state.selected !== null) next.set("cat", String(state.selected));
  history.replaceState(null, "", `?${next}`);
}

function fitDevice() {
  const pose = poses[state.pose];
  const width = pose.width + 16;
  const height = pose.height + 16;
  scale = Math.min(1, Math.max(0.1, ($(".stage").clientWidth - 24) / width));
  device.style.transform = `scale(${scale})`;
  $("#device-space").style.width = `${width * scale}px`;
  $("#device-space").style.height = `${height * scale}px`;
}

function visibleCat() {
  const bounds = scrollArea.getBoundingClientRect();
  return [...$("#cat-grid").children].find((card) => {
    const frame = card.getBoundingClientRect();
    return frame.bottom > bounds.top + 16 * scale && frame.top < bounds.bottom;
  });
}

function applyLayout(retainScroll = true) {
  const anchor = retainScroll ? visibleCat() : null;
  const pose = poses[state.pose];
  device.dataset.pose = state.pose;
  device.dataset.theme = state.theme;
  device.dataset.text = state.text;
  device.style.setProperty("--screen-w", `${pose.width}px`);
  device.style.setProperty("--screen-h", `${pose.height}px`);
  $("#pose-caption").textContent = pose.caption;
  $("#dimensions").textContent = `${pose.width} × ${pose.height} pt · 设计尺寸`;
  document.querySelectorAll("button[data-pose]").forEach((button) => {
    button.setAttribute("aria-pressed", String(button.dataset.pose === state.pose));
  });
  $("#language").value = state.language;
  $("#theme").value = state.theme;
  $("#text-size").value = state.text;
  const replay = $("#replay");
  const parent = state.text === "large" ? scrollArea : $("#gallery");
  if (replay.parentNode !== parent) parent.append(replay);
  fitDevice();
  if (anchor && scrollArea.scrollTop > 0) {
    requestAnimationFrame(() => {
      scrollArea.scrollTop += (anchor.getBoundingClientRect().top - scrollArea.getBoundingClientRect().top) / scale - 16;
    });
  }
  updateURL();
}

function updateSelected() {
  const sound = state.selected === null ? null : data.sounds[state.selected];
  document.querySelectorAll(".cat").forEach((button) => {
    button.setAttribute("aria-pressed", String(Number(button.dataset.id) === state.selected));
  });
  $("#portrait").src = sound?.image ?? "assets/c04.png";
  $("#spotlight-eyebrow").textContent = copy(sound ? "soundboard_selected" : "soundboard_welcome");
  $("#spotlight-title").textContent = copy(sound?.key ?? "soundboard_welcome_title");
  $("#replay").hidden = !sound;
  if (sound) {
    $("#replay-image").src = sound.image;
    $("#replay-name").textContent = copy(sound.key);
  }
}

function translate() {
  $("#screen").lang = state.language;
  document.querySelectorAll("[data-copy]").forEach((element) => {
    element.textContent = copy(element.dataset.copy);
  });
  document.querySelectorAll(".cat").forEach((button) => {
    const sound = data.sounds[Number(button.dataset.id)];
    button.querySelector("span").textContent = copy(sound.key);
    button.setAttribute("aria-label", copy(sound.key));
  });
  $("#replay-button").setAttribute("aria-label", copy("soundboard_replay"));
  $("#support-button").setAttribute("aria-label", copy("tip_button"));
  $("#share-button").setAttribute("aria-label", copy("share_button"));
  $("#close-sheet").setAttribute("aria-label", copy("close_button"));
  $("#can-price").textContent = copy("tip_can").replace("%@", `$${data.prices.can}`);
  $("#coffee-price").textContent = copy("tip_coffee").replace("%@", `$${data.prices.coffee}`);
  updateSelected();
}

async function play(id) {
  const attempt = ++playAttempt;
  state.selected = id;
  updateSelected();
  updateURL();
  player.pause();
  player.src = data.sounds[id].audio;
  try {
    await player.play();
    if (attempt === playAttempt) $("#preview-status").textContent = note;
  } catch (error) {
    if (attempt === playAttempt && error.name !== "AbortError") {
      $("#preview-status").textContent = "声音暂未播放，请再点一次猫咪或重播按钮。";
    }
  }
}

function openSheet(type, opener) {
  sheetOpener = opener;
  $("#support-content").hidden = type !== "support";
  $("#share-content").hidden = type !== "share";
  $(".sheet-panel").setAttribute("aria-label", copy(type === "support" ? "tip_title" : "share_button"));
  $(".sheet-panel").removeAttribute("aria-labelledby");
  $("#sheet-status").textContent = type === "support" ? "排版预览 · 价格来自本地 StoreKit 配置。" : "系统分享页的内容示意。";
  $("#sheet").hidden = false;
  $(".app-panes").inert = true;
  $(".system-bar").inert = true;
  $("#close-sheet").focus({ preventScroll: true });
}

function closeSheet() {
  $("#sheet").hidden = true;
  $(".app-panes").inert = false;
  $(".system-bar").inert = false;
  sheetOpener?.focus({ preventScroll: true });
}

async function initialize() {
  const response = await fetch("iphone-duo-preview-data.json");
  if (!response.ok) throw new Error(`Preview data: ${response.status}`);
  data = await response.json();
  const selected = parameters.has("cat") ? Number(parameters.get("cat")) : null;
  if (Number.isInteger(selected) && selected >= 0 && selected < data.sounds.length) state.selected = selected;

  $("#cat-grid").replaceChildren(...data.sounds.map((sound) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "cat";
    button.dataset.id = sound.id;
    button.id = `cat-${sound.id}`;
    const image = document.createElement("img");
    image.src = sound.image;
    image.alt = "";
    image.draggable = false;
    const label = document.createElement("span");
    button.append(image, label);
    button.addEventListener("click", () => play(sound.id));
    return button;
  }));

  document.querySelectorAll("button[data-pose]").forEach((button) => {
    button.addEventListener("click", () => { state.pose = button.dataset.pose; applyLayout(); });
  });
  $("#language").addEventListener("change", (event) => { state.language = event.target.value; translate(); applyLayout(); });
  $("#theme").addEventListener("change", (event) => { state.theme = event.target.value; applyLayout(); });
  $("#text-size").addEventListener("change", (event) => { state.text = event.target.value; applyLayout(); });
  $("#replay-button").addEventListener("click", () => { if (state.selected !== null) play(state.selected); });
  $("#reset").addEventListener("click", () => {
    ++playAttempt;
    player.pause();
    player.removeAttribute("src");
    player.load();
    state.selected = null;
    scrollArea.scrollTop = 0;
    updateSelected();
    updateURL();
    $("#preview-status").textContent = note;
  });

  $("#support-button").addEventListener("click", (event) => openSheet("support", event.currentTarget));
  $("#share-button").addEventListener("click", (event) => openSheet("share", event.currentTarget));
  $("#close-sheet").addEventListener("click", closeSheet);
  $("#sheet").addEventListener("click", (event) => { if (event.target === $("#sheet")) closeSheet(); });
  for (const selector of ["#can-purchase", "#coffee-purchase", "#restore"]) {
    $(selector).addEventListener("click", () => { $("#sheet-status").textContent = "这是设计预览，不会发起购买或恢复操作。"; });
  }
  $("#sheet").addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeSheet();
    if (event.key !== "Tab") return;
    const controls = [...$("#sheet").querySelectorAll("button, a[href]")].filter((element) => element.getClientRects().length);
    if (event.shiftKey && document.activeElement === controls[0]) {
      controls.at(-1).focus(); event.preventDefault();
    } else if (!event.shiftKey && document.activeElement === controls.at(-1)) {
      controls[0].focus(); event.preventDefault();
    }
  });

  document.querySelectorAll("[data-capture]").forEach((button) => {
    button.addEventListener("click", () => {
      $("#capture-image").src = `captures/${button.dataset.capture}`;
      $("#capture-image").alt = button.querySelector("img").alt;
      $("#capture-dialog").showModal();
    });
  });
  $("#close-capture").addEventListener("click", () => $("#capture-dialog").close());
  $("#capture-dialog").addEventListener("click", (event) => { if (event.target === $("#capture-dialog")) event.target.close(); });
  new ResizeObserver(fitDevice).observe($(".stage"));
  translate();
  applyLayout(false);
}

initialize().catch((error) => {
  $("#pose-caption").textContent = "预览加载失败，请刷新页面。";
  $("#preview-status").textContent = String(error);
});
