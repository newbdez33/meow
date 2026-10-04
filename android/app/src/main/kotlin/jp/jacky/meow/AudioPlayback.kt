package jp.jacky.meow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat

/** One retained player across the grid, configuration auditions, and widget service. */
class AudioPlayback(private val context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var owner: Any? = null
    private var completion: (() -> Unit)? = null
    private var focus: AudioFocusRequest? = null
    private var focusListener: AudioManager.OnAudioFocusChangeListener? = null
    private var receiverRegistered = false
    var soundID: String? = null
        private set

    private val noisy = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { stop() }
    }

    @MainThread
    fun play(owner: Any, index: Int, requestFocus: Boolean = false, onFinished: () -> Unit = {}) {
        val cat = Cats.all.getOrNull(index) ?: return
        stop()
        this.owner = owner
        completion = onFinished
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        try {
            val next = MediaPlayer()
            player = next
            if (requestFocus && !acquireFocus(attributes, next)) {
                stop()
                return
            }
            soundID = cat.id
            next.setAudioAttributes(attributes)
            next.setOnPreparedListener { if (player === it) it.start() }
            next.setOnCompletionListener { if (player === it) stop() }
            next.setOnErrorListener { current, what, extra ->
                Log.w("MeowSound", "Playback error $what/$extra")
                if (player === current) stop()
                true
            }
            context.resources.openRawResourceFd(cat.sound).use { next.setDataSource(it) }
            ContextCompat.registerReceiver(context, noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_NOT_EXPORTED)
            receiverRegistered = true
            next.prepareAsync()
        } catch (error: Exception) {
            Log.w("MeowSound", "Cannot play ${cat.id}", error)
            stop()
        }
    }

    @Suppress("DEPRECATION")
    private fun acquireFocus(attributes: AudioAttributes, next: MediaPlayer): Boolean {
        val listener = AudioManager.OnAudioFocusChangeListener { change ->
            if (change < AudioManager.AUDIOFOCUS_GAIN && player === next) stop()
        }
        focusListener = listener
        val result = if (Build.VERSION.SDK_INT >= 26) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes).setOnAudioFocusChangeListener(listener, handler)
                .setWillPauseWhenDucked(true).build()
            focus = request
            audio.requestAudioFocus(request)
        } else {
            audio.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    @MainThread
    @Suppress("DEPRECATION")
    fun stop(owner: Any? = null) {
        if (owner != null && this.owner !== owner) return
        player?.release()
        player = null
        soundID = null
        this.owner = null
        if (Build.VERSION.SDK_INT >= 26) focus?.let { audio.abandonAudioFocusRequest(it) }
        else focusListener?.let { audio.abandonAudioFocus(it) }
        focus = null
        focusListener = null
        if (receiverRegistered) context.unregisterReceiver(noisy)
        receiverRegistered = false
        val finished = completion
        completion = null
        finished?.invoke()
    }
}
