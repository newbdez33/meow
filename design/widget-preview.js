const cats = JSON.parse(document.getElementById('cat-catalogue').textContent);
const characters = [
  { id: 'ginger', names: { en: 'Smug', 'zh-Hans': '欠欠橘猫', ja: 'ドヤ顔' }, descriptions: { en: 'Orange tabby', 'zh-Hans': '有点得意的橘猫', ja: '得意げな茶トラ' } },
  { id: 'black', names: { en: 'Spicy', 'zh-Hans': '炸毛黑猫', ja: 'シャー！' }, descriptions: { en: 'A little attitude', 'zh-Hans': '今天也不好惹', ja: 'ちょっとご機嫌ななめ' } },
  { id: 'gray', names: { en: 'Chill', 'zh-Hans': '淡定灰猫', ja: 'のんびり' }, descriptions: { en: 'Perfectly unbothered', 'zh-Hans': '一切都很安逸', ja: 'いつでもマイペース' } },
  { id: 'sleepy', names: { en: 'Sleepy', 'zh-Hans': '困困虎斑', ja: 'おねむ' }, descriptions: { en: 'Five more minutes', 'zh-Hans': '再睡五分钟嘛', ja: 'あと5分だけ' } },
  { id: 'pumpkin', names: { en: 'Pumpkin', 'zh-Hans': '南瓜猫', ja: 'かぼちゃ' }, descriptions: { en: 'Caught hiding', 'zh-Hans': '被你发现啦', ja: '見つかっちゃった' } },
  { id: 'ghost', names: { en: 'Boo!', 'zh-Hans': '幽灵猫', ja: 'おばけ' }, descriptions: { en: 'Not very scary', 'zh-Hans': '一点也不可怕', ja: 'こわくないよ' } },
  { id: 'classic', names: { en: 'Classic', 'zh-Hans': '经典白猫', ja: 'クラシック' }, descriptions: { en: 'The original white cat', 'zh-Hans': '最初的绿底白猫', ja: 'いつもの白猫' } }
];
const state = {
  language: 'en',
  widgets: { ios: { sound: 'm_004', art: 'ginger' }, android: { sound: 'm_004', art: 'black' } },
  platform: 'ios', galleryPlatform: 'ios', draft: null, screen: 'config'
};
const text = {
  en: {
    app: 'Meow', play: 'Play', cancel: 'Cancel', back: 'Back', done: 'Done', sound: 'Sound', character: 'Character', edit: 'Edit Widget', configure: 'Widget settings', save: 'Save widget', useSound: 'Use this sound', search: 'Search sounds', empty: 'No sounds found.',
    description: 'Your cat. Your kind of meow.', note: 'Sound and character are separate choices. Save any pair you like.', intro: 'Choose a sound. Tap the speaker to listen.', ready: 'Auditioning does not change your selection.', failed: 'Could not play this sound. Keep the preview and its assets together.', playing: 'Playing', saved: 'Widget updated',
    castTitle: 'Meet your home-screen cats.', castSubtitle: 'Six familiar faces. A little more attitude.', target: 'Try artwork on', classic: 'Keep the original white cat', artHint: 'Choose a face. Your sound stays the same.', unchanged: 'Sound unchanged', stopped: 'Audio stopped. Tap a cat to play again.', cancelled: 'Changes cancelled. Your widget stays the same.', finished: 'Finished. Tap a cat to play again.'
  },
  'zh-Hans': {
    app: '猫叫模拟器', play: '播放', cancel: '取消', back: '返回', done: '完成', sound: '声音', character: '角色', edit: '编辑小组件', configure: '小组件设置', save: '保存小组件', useSound: '使用这个声音', search: '搜索声音', empty: '没有找到声音。',
    description: '你的猫，你喜欢的叫声。', note: '声音和角色独立选择，喜欢的组合都可以。', intro: '选择一个声音，轻点扬声器试听。', ready: '试听不会改变已选声音。', failed: '无法播放，请将预览文件与素材保存在一起。', playing: '正在播放', saved: '小组件已更新',
    castTitle: '让熟悉的猫猫住进桌面。', castSubtitle: '六个老朋友，多一点小脾气。', target: '预览到', classic: '保留经典白猫', artHint: '随意换图，声音保持不变。', unchanged: '声音未改变', stopped: '已停止，轻点猫猫可以再听一次。', cancelled: '已取消，小组件保持原样。', finished: '播放结束，轻点猫猫可以再听一次。'
  },
  ja: {
    app: 'ニャー', play: '再生', cancel: 'キャンセル', back: '戻る', done: '完了', sound: '鳴き声', character: 'キャラクター', edit: '設定', configure: '設定', save: '保存', useSound: 'この鳴き声を使う', search: '鳴き声を検索', empty: '鳴き声が見つかりません。',
    description: '好きな猫に、好きな鳴き声を。', note: '鳴き声とキャラクターは自由に組み合わせられます。', intro: '鳴き声を選んでください。スピーカーで試聴できます。', ready: '試聴しても選択は変わりません。', failed: '再生できません。プレビューと素材を同じ場所に置いてください。', playing: '再生中', saved: 'ウィジェットを更新しました',
    castTitle: 'ホーム画面に、お気に入りの猫を。', castSubtitle: 'おなじみの6匹、いろいろな表情。', target: 'プレビュー先', classic: 'いつもの白猫にする', artHint: '顔を変えても、鳴き声はそのまま。', unchanged: '鳴き声はそのまま', stopped: '停止しました。猫をタップすると再生します。', cancelled: '変更をキャンセルしました。', finished: '再生が終わりました。もう一度タップできます。'
  }
};
const $ = id => document.getElementById(id);
const copy = () => text[state.language];
const catFor = id => cats.find(cat => cat.id === id) || cats[3];
const nameFor = id => catFor(id).names[state.language];
const artFor = id => characters.find(character => character.id === id) || characters[0];
const artName = id => artFor(id).names[state.language];
const artPath = id => id === 'classic' ? 'widget-cat.svg' : `imagegen/widget-characters/${artFor(id).id}.png`;
const platformName = platform => platform === 'ios' ? 'iOS' : 'Android';
const svgIcon = id => `<svg aria-hidden="true"><use href="#i-${id}"/></svg>`;
const player = new Audio();
player.preload = 'none';
let playRequest = 0;

const apps = {
  calendar: ['Calendar', 'calendar'], photos: ['Photos', 'photos'], camera: ['Camera', 'camera'], notes: ['Notes', 'notes'], music: ['Music', 'music'], maps: ['Maps', 'map'], settings: ['Settings', 'settings'], mail: ['Mail', 'mail'], browser: ['Safari', 'browser'], messages: ['Messages', 'message'], phone: ['Phone', 'phone'], meow: ['Meow', 'meow-app']
};
function appMarkup(key, label = true, extra = false) {
  const [name, icon] = apps[key];
  const art = key === 'calendar' ? '<small>SAT</small><b>3</b>' : key === 'meow' ? '<img src="../hosting/public/assets/icon.png" alt="">' : svgIcon(icon);
  return `<div class="app${extra ? ' extra-app' : ''}"><div class="app-icon ${key === 'phone' ? 'phone-icon' : icon}">${art}</div>${label ? `<span class="under-label">${name}</span>` : ''}</div>`;
}
$('ios-side-apps').innerHTML = ['calendar', 'photos', 'camera', 'notes'].map(key => appMarkup(key)).join('');
$('ios-apps').innerHTML = ['maps', 'music', 'settings', 'meow'].map(key => appMarkup(key)).join('');
$('ios-dock').innerHTML = ['phone', 'browser', 'messages', 'mail'].map(key => appMarkup(key, false)).join('');
$('android-dock').innerHTML = ['phone', 'messages', 'browser', 'camera'].map(key => appMarkup(key, false)).join('');
$('android-grid').insertAdjacentHTML('beforeend', ['photos', 'music', 'maps', 'meow', 'calendar', 'settings', 'mail'].map((key, index) => appMarkup(key, true, index > 3)).join(''));

function renderWidgets() {
  for (const platform of ['ios', 'android']) {
    const widget = state.widgets[platform];
    const name = nameFor(widget.sound);
    $(`${platform}-sound`).textContent = name;
    $(`${platform}-art`).src = artPath(widget.art);
    $(`${platform}-play`).dataset.art = widget.art;
    $(`${platform}-play`).setAttribute('aria-label', `${copy().play} ${name} · ${artName(widget.art)} (${platformName(platform)})`);
    $(`${platform}-play`).title = `${artName(widget.art)} · ${name}`;
    $(`${platform}-phone`).lang = state.language;
  }
  document.querySelectorAll('.app-name').forEach(node => { node.textContent = copy().app; });
}

function characterCard(character, selected, onSelect, systemList = false) {
  const button = document.createElement('button');
  button.type = 'button'; button.className = systemList ? 'character-system-choice' : 'cast-card'; button.dataset.art = character.id;
  button.setAttribute('aria-pressed', String(character.id === selected));
  const image = document.createElement('img'); image.className = systemList ? '' : 'cast-image'; image.src = artPath(character.id); image.alt = ''; image.width = 180; image.height = 180;
  const name = document.createElement('span'); name.className = systemList ? 'name' : 'cast-name'; name.textContent = character.names[state.language];
  const check = document.createElement('span'); check.className = 'cast-check'; check.textContent = '✓'; check.setAttribute('aria-hidden', 'true');
  button.append(image, name);
  if (systemList) button.append(check);
  else {
    name.append(check);
    const description = document.createElement('span'); description.className = 'cast-description'; description.textContent = character.descriptions[state.language]; button.append(description);
  }
  button.addEventListener('click', () => onSelect(character.id));
  return button;
}
function updateGallerySelection() {
  const selected = state.widgets[state.galleryPlatform].art;
  $('cast-grid').querySelectorAll('.cast-card').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.art === selected)));
  $('classic-choice').setAttribute('aria-pressed', String(selected === 'classic'));
  document.querySelectorAll('[data-cast-platform]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.castPlatform === state.galleryPlatform)));
  $('hero-tile').dataset.art = selected;
  $('hero-art').src = artPath(selected);
  $('hero-art').alt = `${artName(selected)} · ${artFor(selected).descriptions[state.language]}`;
  $('hero-art-label').textContent = `${platformName(state.galleryPlatform)} / ${artName(selected)}`;
}
function selectGalleryArt(id) {
  state.widgets[state.galleryPlatform].art = id;
  renderWidgets(); updateGallerySelection();
  $('cast-status').textContent = `${platformName(state.galleryPlatform)} · ${artName(id)}. ${copy().unchanged}: ${nameFor(state.widgets[state.galleryPlatform].sound)}.`;
}
function renderGallery() {
  $('cast-grid').replaceChildren(...characters.filter(character => character.id !== 'classic').map(character => characterCard(character, state.widgets[state.galleryPlatform].art, selectGalleryArt)));
  $('cast-title').textContent = copy().castTitle; $('cast-subtitle').textContent = copy().castSubtitle;
  $('cast-target-label').textContent = copy().target; $('classic-choice-label').textContent = copy().classic; $('cast-status').textContent = copy().artHint;
  updateGallerySelection();
}

function stopAudio(message = '') {
  playRequest += 1;
  player.pause();
  if (player.readyState > 0) player.currentTime = 0;
  $('stop').disabled = true;
  if (message) $('playback-status').textContent = message;
}
async function playSound(id, source) {
  stopAudio();
  const request = playRequest;
  player.src = `../meow/resources/${catFor(id).id}.mp3`;
  try {
    await player.play();
    if (request !== playRequest) return;
    $('stop').disabled = false;
    $('playback-status').textContent = `${copy().playing}: ${nameFor(id)} · ${source}`;
    if ($('config-dialog').open) $('picker-status').textContent = `${copy().playing}: ${nameFor(id)}`;
  } catch (error) {
    if (request !== playRequest || error.name === 'AbortError') return;
    $('stop').disabled = true; $('playback-status').textContent = copy().failed;
    if ($('config-dialog').open) $('picker-status').textContent = copy().failed;
  }
}
player.addEventListener('ended', () => { $('stop').disabled = true; $('playback-status').textContent = copy().finished; $('picker-status').textContent = copy().ready; });
player.addEventListener('error', () => { $('stop').disabled = true; $('playback-status').textContent = copy().failed; if ($('config-dialog').open) $('picker-status').textContent = copy().failed; });
for (const platform of ['ios', 'android']) $(`${platform}-play`).addEventListener('click', () => playSound(state.widgets[platform].sound, platformName(platform)));
$('stop').addEventListener('click', () => stopAudio(copy().stopped));
document.addEventListener('visibilitychange', () => { if (document.hidden && !player.paused) stopAudio(copy().stopped); });

document.querySelectorAll('button[data-theme]').forEach(button => button.addEventListener('click', () => {
  document.body.dataset.theme = button.dataset.theme;
  document.querySelectorAll('button[data-theme]').forEach(other => other.setAttribute('aria-pressed', String(other === button)));
}));
document.querySelectorAll('button[data-size]').forEach(button => button.addEventListener('click', () => {
  $('android-grid').dataset.size = button.dataset.size;
  $('android-size-label').textContent = button.dataset.size === 'large' ? 'EXPANDED · 2 × 2' : 'COMPACT · 1 × 1';
  document.querySelectorAll('button[data-size]').forEach(other => other.setAttribute('aria-pressed', String(other === button)));
}));
document.querySelectorAll('[data-cast-platform]').forEach(button => button.addEventListener('click', () => { state.galleryPlatform = button.dataset.castPlatform; updateGallerySelection(); $('cast-status').textContent = `${platformName(state.galleryPlatform)} · ${copy().artHint}`; }));
$('classic-choice').addEventListener('click', () => selectGalleryArt('classic'));
$('language').addEventListener('change', event => { state.language = event.target.value; renderWidgets(); renderGallery(); });

function returnToConfig(field) {
  stopAudio(); state.screen = 'config'; renderDialog(); $(`open-${field}-picker`).focus();
}
function renderSounds() {
  const query = $('sound-search').value.trim().toLocaleLowerCase(state.language);
  const matches = cats.filter(cat => cat.names[state.language].toLocaleLowerCase(state.language).includes(query));
  $('sound-list').replaceChildren();
  if (!matches.length) {
    const empty = document.createElement('p'); empty.className = 'empty-result'; empty.textContent = copy().empty; $('sound-list').append(empty); return;
  }
  for (const cat of matches) {
    const row = document.createElement('div'); row.className = 'sound-option';
    const choice = document.createElement('button'); choice.type = 'button'; choice.className = 'sound-choice'; choice.dataset.id = cat.id;
    choice.setAttribute('aria-pressed', String(cat.id === state.draft.sound));
    if (state.platform === 'android') {
      const image = document.createElement('img'); image.src = `../hosting/public/assets/cats/${cat.image}.png`; image.alt = ''; choice.append(image);
    }
    const name = document.createElement('span'); name.className = 'name'; name.textContent = cat.names[state.language];
    const mark = document.createElement('span'); mark.className = 'selection-mark'; mark.setAttribute('aria-hidden', 'true'); mark.textContent = '✓';
    choice.append(name, mark);
    choice.addEventListener('click', () => {
      state.draft.sound = cat.id;
      if (state.platform === 'ios') returnToConfig('sound');
      else $('sound-list').querySelectorAll('.sound-choice').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.id === cat.id)));
    });
    row.append(choice);
    if (state.platform === 'android') {
      const audition = document.createElement('button'); audition.type = 'button'; audition.className = 'audition'; audition.setAttribute('aria-label', `${copy().play} ${cat.names[state.language]}`); audition.innerHTML = svgIcon('speaker'); audition.addEventListener('click', () => playSound(cat.id, 'Sound preview')); row.append(audition);
    }
    $('sound-list').append(row);
  }
}
function renderCharacters() {
  const list = $('character-list'); list.replaceChildren();
  const select = id => { state.draft.art = id; returnToConfig('character'); };
  if (state.platform === 'ios') list.append(...characters.map(character => characterCard(character, state.draft.art, select, true)));
  else {
    const grid = document.createElement('div'); grid.className = 'character-grid';
    grid.append(...characters.filter(character => character.id !== 'classic').map(character => characterCard(character, state.draft.art, select)));
    list.append(grid, characterCard(artFor('classic'), state.draft.art, select, true));
  }
}
function renderDialog() {
  const isIOS = state.platform === 'ios';
  const config = state.screen === 'config';
  const sounds = state.screen === 'sound';
  $('config-dialog').dataset.platform = state.platform; $('config-dialog').lang = state.language;
  $('dialog-platform-label').textContent = isIOS ? 'iOS · System configuration' : 'Android · Meow configuration';
  $('dialog-title').textContent = config ? (isIOS ? copy().edit : copy().configure) : (sounds ? copy().sound : copy().character);
  $('dialog-back').textContent = config ? copy().cancel : copy().back;
  $('dialog-done').textContent = copy().done; $('dialog-done').hidden = !isIOS || !config;
  $('widget-config').hidden = !config;
  $('config-description').textContent = copy().description;
  $('config-art').src = artPath(state.draft.art); $('config-icon').dataset.art = state.draft.art;
  $('sound-field-label').textContent = copy().sound; $('pending-sound').textContent = nameFor(state.draft.sound);
  $('character-field-label').textContent = copy().character; $('pending-character').textContent = artName(state.draft.art); $('pending-character-image').src = artPath(state.draft.art); $('pending-character-image').dataset.art = state.draft.art;
  $('config-note').textContent = copy().note;
  $('picker-intro').hidden = isIOS || !sounds; $('picker-intro').textContent = copy().intro;
  $('search-wrap').hidden = !sounds; $('sound-list').hidden = !sounds;
  $('character-list').hidden = state.screen !== 'character';
  $('sound-search').placeholder = copy().search; $('sound-search').setAttribute('aria-label', copy().search);
  $('dialog-bottom').hidden = isIOS || state.screen === 'character';
  $('save-android').textContent = sounds ? copy().useSound : copy().save;
  $('picker-status').hidden = config; $('picker-status').textContent = copy().ready;
  if (sounds) renderSounds();
  if (state.screen === 'character') renderCharacters();
}
function openConfig(platform) {
  stopAudio(); state.platform = platform; state.draft = { ...state.widgets[platform] }; state.screen = 'config';
  $('sound-search').value = ''; renderDialog(); $('config-dialog').showModal();
}
function saveConfig() {
  state.widgets[state.platform] = { ...state.draft };
  renderWidgets(); updateGallerySelection();
  stopAudio(`${copy().saved}: ${platformName(state.platform)} · ${artName(state.draft.art)} · ${nameFor(state.draft.sound)}`);
  $('config-dialog').close(); state.draft = null;
}
document.querySelectorAll('[data-config]').forEach(button => button.addEventListener('click', () => openConfig(button.dataset.config)));
$('open-sound-picker').addEventListener('click', () => { state.screen = 'sound'; $('sound-search').value = ''; renderDialog(); $('sound-search').focus(); });
$('open-character-picker').addEventListener('click', () => { state.screen = 'character'; renderDialog(); $('character-list').querySelector('button').focus(); });
$('sound-search').addEventListener('input', renderSounds);
$('dialog-back').addEventListener('click', () => {
  if (state.screen !== 'config') returnToConfig(state.screen);
  else { stopAudio(copy().cancelled); $('config-dialog').close(); state.draft = null; }
});
$('dialog-done').addEventListener('click', saveConfig);
$('save-android').addEventListener('click', () => state.screen === 'sound' ? returnToConfig('sound') : saveConfig());
$('config-dialog').addEventListener('cancel', () => { stopAudio(copy().cancelled); state.draft = null; });
renderWidgets(); renderGallery();
