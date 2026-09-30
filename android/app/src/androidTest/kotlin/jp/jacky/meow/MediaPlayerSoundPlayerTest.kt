package jp.jacky.meow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaPlayerSoundPlayerTest {
    @Test
    fun sweepingAcrossEveryCatThreeTimesDoesNotThrow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val player = MediaPlayerSoundPlayer(context)
        try {
            repeat(3) {
                for (index in Cats.all.indices) player.play(index)
            }
            Thread.sleep(300)
            player.play(0)
            Thread.sleep(1500)
            player.play(Cats.all.size)      // out of range: ignored, not a crash
            player.play(-1)
        } finally {
            player.release()
        }
    }
}
