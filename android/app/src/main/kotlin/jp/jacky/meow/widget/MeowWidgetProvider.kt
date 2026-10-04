package jp.jacky.meow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import jp.jacky.meow.R

class MeowWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        update(context, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) { WidgetSettings(context).delete(ids) }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        WidgetSettings(context).restore(oldWidgetIds, newWidgetIds)
        val manager = AppWidgetManager.getInstance(context)
        newWidgetIds.forEach { id ->
            if (Build.VERSION.SDK_INT >= 30) {
                manager.updateAppWidgetOptions(id, Bundle().apply { putBoolean(AppWidgetManager.OPTION_APPWIDGET_RESTORE_COMPLETED, true) })
            }
            update(context, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_LOCALE_CHANGED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            onUpdate(context, AppWidgetManager.getInstance(context), ids(context))
        }
    }

    companion object {
        fun ids(context: Context): IntArray = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, MeowWidgetProvider::class.java))

        fun playbackIntent(context: Context, id: Int): PendingIntent {
            val intent = Intent(context, WidgetPlaybackService::class.java)
                .setData(Uri.parse("meow://widget/$id/play"))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return if (Build.VERSION.SDK_INT >= 26) PendingIntent.getForegroundService(context, id, intent, flags)
            else PendingIntent.getService(context, id, intent, flags)
        }

        fun update(context: Context, id: Int) {
            val manager = AppWidgetManager.getInstance(context)
            val options = manager.getAppWidgetOptions(id)
            val portrait = views(context, id,
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 64),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 80))
            val landscape = views(context, id,
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 64),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 80))
            manager.updateAppWidget(id, RemoteViews(landscape, portrait))
        }

        private fun views(context: Context, id: Int, width: Int, height: Int): RemoteViews {
            val large = width >= 120 && height >= 120
            val selection = WidgetSettings(context).read(id)
            val caption = context.getString(selection.cat.caption)
            val views = RemoteViews(context.packageName, if (large) R.layout.widget_large else R.layout.widget_compact)
            val labelHeight = (20 * context.resources.configuration.fontScale).toInt()
            val showCaption = width >= 48 && height >= 48 + labelHeight
            views.setImageViewBitmap(R.id.widget_art, artwork(context, selection.character.image,
                width - 4, if (large) height else height - 4 - if (showCaption) labelHeight else 0, large))
            views.setTextViewText(R.id.widget_caption, caption)
            views.setViewVisibility(R.id.widget_caption, if (showCaption) View.VISIBLE else View.GONE)
            views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_play, caption))
            views.setOnClickPendingIntent(R.id.widget_root, playbackIntent(context, id))
            return views
        }

        private fun artwork(context: Context, resource: Int, width: Int, height: Int, large: Boolean): Bitmap {
            val scale = minOf(context.resources.displayMetrics.density, 600f / maxOf(width, height, 1))
            val w = (width * scale).toInt().coerceAtLeast(1)
            val h = (height * scale).toInt().coerceAtLeast(1)
            val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val source = BitmapFactory.decodeResource(context.resources, resource)
            val side = minOf(w, h).toFloat()
            val bounds = if (large) RectF(0f, 0f, w.toFloat(), h.toFloat())
                else RectF((w - side) / 2, (h - side) / 2, (w + side) / 2, (h + side) / 2)
            val cropScale = maxOf(bounds.width() / source.width, bounds.height() / source.height)
            val matrix = Matrix().apply {
                setScale(cropScale, cropScale)
                postTranslate(bounds.centerX() - source.width * cropScale / 2, bounds.centerY() - source.height * cropScale / 2)
            }
            val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply { setLocalMatrix(matrix) }
            Canvas(output).drawRoundRect(bounds, side * 0.2f, side * 0.2f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { this.shader = shader })
            source.recycle()
            return output
        }
    }
}
