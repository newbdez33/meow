"use strict";

const poses = {
  closed: { width: 466, height: 678, caption: "单屏 · 一张猫叫列表，照常点按播放" },
  open: { width: 951, height: 669, caption: "全开 · 一张完整网格，黑白猫摊开白肚皮" },
  book: { width: 951, height: 669, caption: "半开 · 左右 split，蜷起下半身，藏住白肚皮" },
  tabletop: { width: 669, height: 951, caption: "半开 · 上下 split，蜷起下半身，藏住白肚皮" },
};

const $ = (selector) => document.querySelector(selector);
const parameters = new URLSearchParams(location.search);
const initialPose = Object.hasOwn(poses, parameters.get("pose")) ? parameters.get("pose") : "book";
const initialProgress = parameters.has("fold") ? Number(parameters.get("fold"))
  : initialPose === "closed" ? 0 : initialPose === "open" ? 100 : 50;
const state = {
  pose: initialPose,
  axis: initialPose === "tabletop" || parameters.get("axis") === "tabletop" ? "tabletop" : "book",
  progress: Number.isFinite(initialProgress) ? Math.max(0, Math.min(100, initialProgress)) : 50,
  language: ["en", "zh-Hans", "ja"].includes(parameters.get("lang")) ? parameters.get("lang") : "zh-Hans",
  theme: parameters.get("theme") === "dark" ? "dark" : "light",
  text: parameters.get("text") === "large" ? "large" : "standard",
  ad: parameters.get("ad") === "empty" ? "empty" : "sample",
  selected: null,
};
const device = $("#device");
const adSlot = $("#ad-slot");
const scrollArea = $("#sound-scroll");
const leadingScrollArea = $("#leading-scroll");
const scrollAreas = [leadingScrollArea, scrollArea];
const player = new Audio();
player.preload = "none";
const reducedMotion = matchMedia("(prefers-reduced-motion: reduce)");
const renderCatArtwork = createHingeCatRenderer($("#hinge-cat"));
const renderCatStudy = createHingeCatRenderer($("#cat-study"));
let data;
let scale = 1;
let playAttempt = 0;
let sheetOpener;
let catButtons = [];
let gridsAreSplit = false;
let activeScrollArea = scrollArea;
const note = "原生 2.3 (8) 已上传，正在等待 TestFlight 处理；本页开合比例与系统栏为设计示意。";

function copy(key) {
  return data.strings[state.language][key] ?? key;
}

function updateURL() {
  const next = new URLSearchParams({ pose: state.pose, fold: state.progress, axis: state.axis, lang: state.language, theme: state.theme, text: state.text, ad: state.ad });
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

function visibleCat(area) {
  const bounds = area.getBoundingClientRect();
  return [...area.querySelectorAll(".cat")].find((card) => {
    const frame = card.getBoundingClientRect();
    return frame.bottom > bounds.top + 16 * scale && frame.top < bounds.bottom;
  });
}

function arrangeGrids(split) {
  $("#leading-gallery").hidden = !split;
  const leadingCount = 15;
  if (split !== gridsAreSplit) {
    $("#leading-grid").replaceChildren(...(split ? catButtons.slice(0, leadingCount) : []));
    $("#cat-grid").replaceChildren(...(split ? catButtons.slice(leadingCount) : catButtons), adSlot);
    gridsAreSplit = split;
  }
  if (split || state.pose === "open") $("#cat-grid").append(adSlot);
  else scrollArea.insertBefore(adSlot, $("#cat-grid"));
  adSlot.hidden = state.ad === "empty" && !((split || state.pose === "open") && state.text === "standard");
  $("#ad-creative").hidden = state.ad === "empty";
  $("#leading-range").textContent = `01–${leadingCount}`;
  $("#trailing-range").textContent = `${leadingCount + 1}–${catButtons.length}`;
  $("#trailing-range").hidden = !split;
  $("#gallery-title").textContent = split
    ? { en: "More meows", "zh-Hans": "更多猫叫", ja: "もっとニャー" }[state.language]
    : copy("soundboard_title");
}

function poseForProgress() {
  // These endpoints simulate system states. Native layout uses active division regions.
  return state.progress === 0 ? "closed" : state.progress === 100 ? "open" : state.axis;
}

function renderHinge() {
  const partial = state.pose === "book" || state.pose === "tabletop";
  const stage = partial ? "partial" : state.pose === "open" ? "full" : "single";
  const label = partial ? `半开 · ${Math.round(state.progress)}% 示意`
    : stage === "full" ? "全开 · 一个大屏" : "合拢 · 单屏";
  $("#hinge-progress").value = String(state.progress);
  $("#hinge-progress").setAttribute("aria-valuetext", label);
  $("#hinge-value").textContent = label;
  $("#fold-axis").value = state.axis;
  $("#fold-axis").disabled = !partial;
  for (const button of document.querySelectorAll("button[data-stage]")) {
    button.setAttribute("aria-pressed", String(button.dataset.stage === stage));
  }
  const cat = $("#hinge-cat");
  cat.hidden = stage === "single";
  cat.dataset.progress = String(state.progress);
  $("#cat-detail-progress").value = String(state.progress);
  $("#cat-detail-caption").textContent = stage === "single" ? "单屏时收起小猫。" : label;
  $("#cat-study").hidden = stage === "single";
  if (cat.hidden) { cat.style.opacity = "0"; return; }

  const progress = reducedMotion.matches && partial ? 0.5 : state.progress / 100;
  cat.style.opacity = String(reducedMotion.matches ? 1 : Math.min(1, progress / 0.12));
  renderCatArtwork(progress);
  renderCatStudy(progress);
}

function setProgress(value) {
  state.progress = Math.max(0, Math.min(100, Number(value)));
  const pose = poseForProgress();
  if (pose !== state.pose) {
    state.pose = pose;
    applyLayout();
  } else {
    renderHinge();
    updateURL();
  }
}

function applyLayout(retainScroll = true) {
  const areas = scrollAreas.filter((area) => area.clientHeight > 0);
  const anchors = retainScroll ? areas.map((area) => ({ area, card: visibleCat(area) })) : [];
  anchors.sort((a, b) => Number(a.area === activeScrollArea) - Number(b.area === activeScrollArea));
  for (const anchor of anchors) {
    anchor.offset = anchor.card ? (anchor.card.getBoundingClientRect().top - anchor.area.getBoundingClientRect().top) / scale : 0;
  }
  const pose = poses[state.pose];
  const split = ["book", "tabletop"].includes(state.pose);
  arrangeGrids(split);
  device.dataset.pose = state.pose;
  device.dataset.layout = "dual-grid";
  device.dataset.theme = state.theme;
  device.dataset.text = state.text;
  device.dataset.fit = String((split || state.pose === "open") && state.text === "standard");
  device.style.setProperty("--screen-w", `${pose.width}px`);
  device.style.setProperty("--screen-h", `${pose.height}px`);
  $("#pose-caption").textContent = pose.caption;
  $("#layout-note").textContent = state.pose === "closed"
    ? "原来的猫叫列表；选择与播放状态保留。小猫装饰在单屏时收起。"
    : state.text === "large" ? "辅助大字号保留滚动，让图标和文字保持清晰。"
    : split ? "下半身像虾一样蜷起，肚皮藏住、猫脸露出；展开时身体和后腿一起舒展。两屏保留 15＋12 个猫叫和一行广告。"
    : "全开后，27 只猫合为一张 8 列网格。黑白猫留在顶部，完整露出白肚皮。";
  $("#dimensions").textContent = state.pose === "book" || state.pose === "open"
    ? `${pose.width} × ${pose.height} pt · 顶部 86 pt / 侧栏 84 pt`
    : `${pose.width} × ${pose.height} pt · 设计尺寸`;
  $("#language").value = state.language;
  $("#theme").value = state.theme;
  $("#text-size").value = state.text;
  $("#ad-state").value = state.ad;
  $("#ad-note").textContent = state.ad === "sample"
    ? (state.pose === "closed" ? "50 pt 高的模拟横幅，放在猫咪列表上方；不请求真实广告。" : "50 pt 高的模拟横幅，居中放在预留位置；不请求真实广告。")
    : "当前隐藏示例广告；标准字号的大屏布局保留广告空间，方便对照空白效果。";
  const replay = $("#replay");
  const parent = state.text === "large" ? scrollArea : $("#gallery");
  if (replay.parentNode !== parent) parent.append(replay);
  fitDevice();
  renderHinge();
  if (anchors.length) {
    requestAnimationFrame(() => {
      for (const { card, offset } of anchors) {
        if (!card) continue;
        const area = card.closest(".sound-scroll");
        area.scrollTop += (card.getBoundingClientRect().top - area.getBoundingClientRect().top) / scale - offset;
      }
    });
  }
  updateURL();
}

function updateSelected() {
  const sound = state.selected === null ? null : data.sounds[state.selected];
  document.querySelectorAll(".cat").forEach((button) => {
    button.setAttribute("aria-pressed", String(Number(button.dataset.id) === state.selected));
  });
  $("#replay").hidden = !sound;
  if (sound) {
    $("#replay-image").src = sound.image;
    $("#replay-name").textContent = copy(sound.key);
  }
}

function translate() {
  $("#screen").lang = state.language;
  const adCopy = {
    en: { badge: "Ad", preview: "Sample creative", title: "A little treat for your cat", action: "View" },
    "zh-Hans": { badge: "广告", preview: "示例素材", title: "给猫咪加一份小惊喜", action: "看看" },
    ja: { badge: "広告", preview: "サンプル", title: "猫に小さなごほうびを", action: "見る" },
  }[state.language];
  $("#ad-badge").textContent = adCopy.badge;
  $("#ad-preview-label").textContent = adCopy.preview;
  $("#ad-title").textContent = adCopy.title;
  $("#ad-action").textContent = adCopy.action;
  adSlot.setAttribute("aria-label", `${adCopy.badge} · ${adCopy.preview}`);
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
  activeScrollArea = catButtons[id].closest(".sound-scroll");
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

  catButtons = data.sounds.map((sound) => {
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
  });
  $("#cat-grid").replaceChildren(...catButtons, adSlot);

  document.querySelectorAll("button[data-stage]").forEach((button) => {
    button.addEventListener("click", () => setProgress({ single: 0, partial: 50, full: 100 }[button.dataset.stage]));
  });
  $("#hinge-progress").addEventListener("input", (event) => setProgress(event.target.value));
  $("#fold-axis").addEventListener("change", (event) => {
    state.axis = event.target.value;
    state.pose = poseForProgress();
    applyLayout();
  });
  reducedMotion.addEventListener("change", renderHinge);
  for (const area of scrollAreas) {
    area.addEventListener("pointerdown", () => { activeScrollArea = area; });
    area.addEventListener("wheel", () => { activeScrollArea = area; }, { passive: true });
    area.addEventListener("focusin", () => { activeScrollArea = area; });
  }
  $("#language").addEventListener("change", (event) => { state.language = event.target.value; translate(); applyLayout(); });
  $("#theme").addEventListener("change", (event) => { state.theme = event.target.value; applyLayout(); });
  $("#text-size").addEventListener("change", (event) => { state.text = event.target.value; applyLayout(); });
  $("#ad-state").addEventListener("change", (event) => { state.ad = event.target.value; applyLayout(false); });
  $("#ad-action").addEventListener("click", () => {
    $("#preview-status").textContent = "这是模拟广告，仅用于确认横幅尺寸和位置，不会打开广告网站。";
  });
  $("#replay-button").addEventListener("click", () => { if (state.selected !== null) play(state.selected); });
  $("#reset").addEventListener("click", () => {
    ++playAttempt;
    player.pause();
    player.removeAttribute("src");
    player.load();
    state.selected = null;
    for (const area of scrollAreas) area.scrollTop = 0;
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
  $("#cat-detail-open").addEventListener("click", () => $("#cat-detail-dialog").showModal());
  $("#cat-detail-close").addEventListener("click", () => $("#cat-detail-dialog").close());
  $("#cat-detail-progress").addEventListener("input", (event) => setProgress(event.target.value));
  $("#close-capture").addEventListener("click", () => $("#capture-dialog").close());
  $("#capture-dialog").addEventListener("click", (event) => { if (event.target === $("#capture-dialog")) event.target.close(); });
  new ResizeObserver(fitDevice).observe($(".stage"));
  state.pose = poseForProgress();
  translate();
  applyLayout(false);
}

initialize().catch((error) => {
  $("#pose-caption").textContent = "预览加载失败，请刷新页面。";
  $("#preview-status").textContent = String(error);
});
