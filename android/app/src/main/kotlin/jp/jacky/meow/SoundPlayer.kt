package jp.jacky.meow

import android.content.Context
import android.os.Handler
import android.os.Looper

interface SoundPlayer {
    fun play(index: Int)
    fun release()
}

/** A view's playback handle. Releasing an old view cannot stop a newer widget sound. */
class MediaPlayerSoundPlayer(context: Context) : SoundPlayer {
    private val audio = (context.applicationContext as MeowApplication).audio
    private val handler = Handler(Looper.getMainLooper())

    override fun play(index: Int) = onMain { audio.play(this, index) }
    override fun release() = onMain { audio.stop(this) }

    private fun onMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action() else handler.post(action)
    }
}
