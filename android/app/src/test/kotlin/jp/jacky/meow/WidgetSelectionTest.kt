package jp.jacky.meow

import jp.jacky.meow.widget.WidgetSelection
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSelectionTest {
    @Test
    fun staleSoundKeepsTheChosenCharacter() {
        assertEquals(WidgetSelection("m_004", "ghost"), WidgetSelection.resolve("removed", "ghost"))
    }

    @Test
    fun staleCharacterKeepsTheChosenSound() {
        assertEquals(WidgetSelection("m_027", "ginger"), WidgetSelection.resolve("m_027", "removed"))
    }

    @Test
    fun eachSoundHasAStableResourceName() {
        assertEquals((1..27).map { "m_%03d".format(it) }, Cats.all.map { it.id })
        for (cat in Cats.all) {
            assertEquals(cat.id, WidgetSelection.resolve(cat.id, "sleepy").soundID)
        }
    }
}
