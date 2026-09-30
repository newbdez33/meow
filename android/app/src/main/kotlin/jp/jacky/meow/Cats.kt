package jp.jacky.meow

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

/** One cat: its drawing, its caption and its sound. */
data class Cat(@DrawableRes val image: Int, @StringRes val caption: Int, @RawRes val sound: Int)

/** The 27 cats in the order the iOS app shows them. */
object Cats {
    val all: List<Cat> = listOf(
        Cat(R.drawable.c01, R.string.t01, R.raw.m_001),
        Cat(R.drawable.c02, R.string.t02, R.raw.m_002),
        Cat(R.drawable.c03, R.string.t03, R.raw.m_003),
        Cat(R.drawable.c04, R.string.t04, R.raw.m_004),
        Cat(R.drawable.c05, R.string.t05, R.raw.m_005),
        Cat(R.drawable.c06, R.string.t06, R.raw.m_006),
        Cat(R.drawable.c07, R.string.t07, R.raw.m_007),
        Cat(R.drawable.c08, R.string.t08, R.raw.m_008),
        Cat(R.drawable.c09, R.string.t09, R.raw.m_009),
        Cat(R.drawable.c10, R.string.t10, R.raw.m_010),
        Cat(R.drawable.c11, R.string.t11, R.raw.m_011),
        Cat(R.drawable.c12, R.string.t12, R.raw.m_012),
        Cat(R.drawable.c13, R.string.t13, R.raw.m_013),
        Cat(R.drawable.c14, R.string.t14, R.raw.m_014),
        Cat(R.drawable.c15, R.string.t15, R.raw.m_015),
        Cat(R.drawable.c16, R.string.t16, R.raw.m_016),
        Cat(R.drawable.c17, R.string.t17, R.raw.m_017),
        Cat(R.drawable.c18, R.string.t18, R.raw.m_018),
        Cat(R.drawable.c19, R.string.t19, R.raw.m_019),
        Cat(R.drawable.c20, R.string.t20, R.raw.m_020),
        Cat(R.drawable.c21, R.string.t21, R.raw.m_021),
        Cat(R.drawable.c22, R.string.t22, R.raw.m_022),
        Cat(R.drawable.c23, R.string.t23, R.raw.m_023),
        Cat(R.drawable.c24, R.string.t24, R.raw.m_024),
        Cat(R.drawable.c25, R.string.t25, R.raw.m_025),
        Cat(R.drawable.c26, R.string.t26, R.raw.m_026),
        Cat(R.drawable.c27, R.string.t27, R.raw.m_027),
    )
}
