package jp.jacky.meow

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/** Plays one cat sound at a time. */
interface SoundPlayer {
    fun play(index: Int)
    fun release()
}

/**
 * One MediaPlayer for the whole app. Each tap stops whatever is playing and starts the new sound,
 * as Sounds.playSounds does on iOS. Audio focus is never requested, so other apps' music keeps playing.
 */
class MediaPlayerSoundPlayer(private val context: Context) : SoundPlayer {
    private val player = MediaPlayer()

    override fun play(index: Int) {
        val cat = Cats.all.getOrNull(index) ?: return
        player.reset()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        player.setOnPreparedListener { it.start() }
        player.setOnErrorListener { _, what, extra ->
            Log.w(TAG, "MediaPlayer error $what/$extra")
            true
        }
        context.resources.openRawResourceFd(cat.sound).use { player.setDataSource(it) }
        player.prepareAsync()
    }

    override fun release() = player.release()

    private companion object {
        const val TAG = "MeowSound"
    }
}
