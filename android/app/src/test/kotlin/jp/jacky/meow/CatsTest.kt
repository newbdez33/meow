package jp.jacky.meow

import org.junit.Assert.assertEquals
import org.junit.Test

class CatsTest {
    @Test
    fun thereAre27CatsWithDistinctResources() {
        assertEquals(27, Cats.all.size)
        assertEquals(27, Cats.all.map { it.image }.distinct().size)
        assertEquals(27, Cats.all.map { it.caption }.distinct().size)
        assertEquals(27, Cats.all.map { it.sound }.distinct().size)
    }

    @Test
    fun theFirstAndLastCatsMatchTheIosOrder() {
        assertEquals(R.drawable.c01, Cats.all.first().image)
        assertEquals(R.string.t01, Cats.all.first().caption)
        assertEquals(R.raw.m_001, Cats.all.first().sound)
        assertEquals(R.drawable.c27, Cats.all.last().image)
        assertEquals(R.string.t27, Cats.all.last().caption)
        assertEquals(R.raw.m_027, Cats.all.last().sound)
    }
}
