package jp.jacky.meow

import android.content.Context
import android.content.Intent

const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=jp.jacky.meow"

/** The system share sheet with "<name> - <subtitle> <Play link>", the Android form of the iOS share button. */
fun shareApp(context: Context) {
    val text = context.getString(R.string.app_name) + " - " + context.getString(R.string.subtitle) + " " + PLAY_STORE_URL
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_button)))
}
