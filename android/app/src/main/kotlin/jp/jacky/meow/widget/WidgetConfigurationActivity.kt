package jp.jacky.meow.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.jacky.meow.Cats
import jp.jacky.meow.MediaPlayerSoundPlayer
import jp.jacky.meow.R
import jp.jacky.meow.ui.MeowTheme

class WidgetConfigurationActivity : ComponentActivity() {
    private lateinit var sounds: MediaPlayerSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        enableEdgeToEdge()
        sounds = MediaPlayerSoundPlayer(this)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (id != AppWidgetManager.INVALID_APPWIDGET_ID && id !in MeowWidgetProvider.ids(this)) {
            finish()
            return
        }
        val settings = WidgetSettings(this)
        setContent {
            MeowTheme {
                if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    var ids by remember { mutableStateOf(MeowWidgetProvider.ids(this).toList()) }
                    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
                        ids = MeowWidgetProvider.ids(this@WidgetConfigurationActivity).toList()
                        onPauseOrDispose { }
                    }
                    WidgetList(ids, settings, onSelect = { selected ->
                        startActivity(Intent(this, WidgetConfigurationActivity::class.java)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, selected))
                    }, onClose = { finish() })
                } else {
                    WidgetConfigurationContent(settings.read(id), onAudition = sounds::play,
                        onCancel = { finish() }, onSave = { selection ->
                            if (id !in MeowWidgetProvider.ids(this)) { finish(); return@WidgetConfigurationContent }
                            if (settings.save(id, selection)) {
                                MeowWidgetProvider.update(this, id)
                                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                                finish()
                            } else Toast.makeText(this, R.string.widget_save_error, Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }
    }

    override fun onStop() {
        sounds.release()
        super.onStop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigurationContent(
    initial: WidgetSelection,
    onAudition: (Int) -> Unit,
    onCancel: () -> Unit,
    onSave: (WidgetSelection) -> Unit,
) {
    var sound by rememberSaveable { mutableStateOf(initial.soundID) }
    var character by rememberSaveable { mutableStateOf(initial.characterID) }
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    val selection = WidgetSelection.resolve(sound, character)
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.widget_title)) }) },
        bottomBar = {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).testTag("widget-cancel")) { Text(stringResource(R.string.widget_cancel)) }
                Button(onClick = { onSave(selection) }, modifier = Modifier.weight(1f).testTag("widget-save")) { Text(stringResource(R.string.widget_save)) }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text(stringResource(R.string.widget_description), modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge)
                Box(Modifier.size(220.dp).clip(RoundedCornerShape(32.dp))) {
                    Image(painterResource(selection.character.image), null, Modifier.fillMaxSize())
                    Text(stringResource(selection.cat.caption), color = Color(0xFF413936), maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                            .background(Color(0xEFFFFFFA), RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
            item {
                ListItem(headlineContent = { Text(stringResource(R.string.widget_sound)) },
                    supportingContent = { Text(stringResource(selection.cat.caption)) },
                    trailingContent = { IconButton(onClick = { onAudition(Cats.all.indexOf(selection.cat)) }, modifier = Modifier.testTag("widget-preview")) {
                        Icon(Icons.Default.PlayArrow, stringResource(R.string.widget_audition))
                    } }, modifier = Modifier.clickable { picker = "sound" }.testTag("widget-sound"))
                ListItem(headlineContent = { Text(stringResource(R.string.widget_character)) },
                    supportingContent = { Text(stringResource(selection.character.name)) },
                    trailingContent = { Image(painterResource(selection.character.image), null, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))) },
                    modifier = Modifier.clickable { picker = "character" }.testTag("widget-character"))
            }
        }
    }
    if (picker != null) {
        Dialog(onDismissRequest = { picker = null }) {
            Surface(shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.fillMaxWidth().heightIn(max = 580.dp).padding(16.dp)) {
                    Text(stringResource(if (picker == "sound") R.string.widget_sound else R.string.widget_character),
                        style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp))
                    if (picker == "sound") {
                        LazyColumn(Modifier.weight(1f, fill = false)) {
                            items(Cats.all, key = { it.id }) { cat ->
                                ListItem(headlineContent = { Text(stringResource(cat.caption)) },
                                    leadingContent = { RadioButton(selected = cat.id == sound, onClick = null) },
                                    trailingContent = { IconButton(onClick = { onAudition(Cats.all.indexOf(cat)) }, modifier = Modifier.testTag("audition-${cat.id}")) {
                                        Icon(Icons.Default.PlayArrow, stringResource(R.string.widget_play, stringResource(cat.caption)))
                                    } }, modifier = Modifier.clickable { sound = cat.id; picker = null }.testTag("sound-${cat.id}"))
                            }
                        }
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(WidgetCharacters.all, key = { it.id }) { item ->
                                Column(Modifier.clip(RoundedCornerShape(16.dp)).clickable { character = item.id; picker = null }
                                    .testTag("character-${item.id}"), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box {
                                        Image(painterResource(item.image), stringResource(item.name), Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)))
                                        if (character == item.id) Icon(Icons.Default.Check, null, tint = Color.White,
                                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color(0xFF413936), RoundedCornerShape(12.dp)))
                                    }
                                    Text(stringResource(item.name), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 6.dp))
                                }
                            }
                        }
                    }
                    TextButton(onClick = { picker = null }, modifier = Modifier.align(Alignment.End)) { Text(stringResource(R.string.widget_cancel)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetList(ids: List<Int>, settings: WidgetSettings, onSelect: (Int) -> Unit, onClose: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.widget_manage)) },
        navigationIcon = { TextButton(onClick = onClose) { Text(stringResource(R.string.widget_cancel)) } }) }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            item { Text(stringResource(R.string.widget_empty), modifier = Modifier.padding(24.dp)) }
            items(ids) { id ->
                val selection = settings.read(id)
                ListItem(headlineContent = { Text(stringResource(selection.cat.caption)) },
                    supportingContent = { Text(stringResource(R.string.widget_instance, id.toString())) },
                    leadingContent = { Image(painterResource(selection.character.image), stringResource(selection.character.name), Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))) },
                    modifier = Modifier.clickable { onSelect(id) })
            }
        }
    }
}
