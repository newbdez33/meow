package jp.jacky.meow.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import jp.jacky.meow.Cats
import jp.jacky.meow.MeowApplication
import jp.jacky.meow.R

class WidgetPlaybackService : Service() {
    private val audio get() = (application as MeowApplication).audio
    private lateinit var session: MediaSession
    private var generation = 0

    override fun onCreate() {
        super.onCreate()
        session = MediaSession(this, "MeowWidget").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onStop() { audio.stop(this@WidgetPlaybackService) }
                override fun onPause() { onStop() }
            })
        }
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, getString(R.string.widget_channel), NotificationManager.IMPORTANCE_LOW).apply { setSound(null, null) }
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            audio.stop(this)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val id = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (id == null || id == AppWidgetManager.INVALID_APPWIDGET_ID ||
            AppWidgetManager.getInstance(this).getAppWidgetInfo(id)?.provider?.className != MeowWidgetProvider::class.java.name) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val selection = WidgetSettings(this).read(id)
        val caption = getString(selection.cat.caption)
        val stop = PendingIntent.getService(this, 0, Intent(this, WidgetPlaybackService::class.java).setAction(STOP), PendingIntent.FLAG_IMMUTABLE)
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL) else Notification.Builder(this)
        val notification = builder.setSmallIcon(R.drawable.ic_widget_audio)
            .setContentTitle(getString(R.string.app_name)).setContentText(caption)
            .setCategory(Notification.CATEGORY_TRANSPORT).setOngoing(true).setShowWhen(false)
            .addAction(Notification.Action.Builder(Icon.createWithResource(this, android.R.drawable.ic_media_pause), getString(R.string.widget_stop), stop).build())
            .setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken).setShowActionsInCompactView(0)).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(NOTIFICATION, notification)
        val current = ++generation
        session.isActive = true
        session.setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, caption).build())
        session.setPlaybackState(PlaybackState.Builder().setActions(PlaybackState.ACTION_STOP or PlaybackState.ACTION_PAUSE)
            .setState(PlaybackState.STATE_PLAYING, 0, 1f).build())
        // Enter the foreground before requesting focus on Android 15 and later.
        audio.play(this, Cats.all.indexOf(selection.cat), requestFocus = true) {
            if (generation == current) {
                session.isActive = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelfResult(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ++generation
        audio.stop(this)
        session.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val CHANNEL = "widget_playback"
        const val NOTIFICATION = 42
        const val STOP = "jp.jacky.meow.widget.STOP"
    }
}
