package jp.jacky.meow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jp.jacky.meow.ui.MeowTheme

class MainActivity : ComponentActivity() {
    private lateinit var sounds: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sounds = MediaPlayerSoundPlayer(this)
        setContent {
            MeowTheme {
                MeowScreen(
                    state = MeowUiState(),
                    onPlay = sounds::play,
                    onShare = { shareApp(this) },
                    onTip = {},
                    onPrivacyOptions = {},
                )
            }
        }
    }

    override fun onDestroy() {
        sounds.release()
        super.onDestroy()
    }
}
