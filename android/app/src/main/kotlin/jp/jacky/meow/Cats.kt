package jp.jacky.meow

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

/** One cat: its drawing, its caption and its sound. */
data class Cat(val id: String, @DrawableRes val image: Int, @StringRes val caption: Int, @RawRes val sound: Int)

/** The 27 cats in the order the iOS app shows them. */
object Cats {
    val all: List<Cat> = listOf(
        Cat("m_001", R.drawable.c01, R.string.t01, R.raw.m_001),
        Cat("m_002", R.drawable.c02, R.string.t02, R.raw.m_002),
        Cat("m_003", R.drawable.c03, R.string.t03, R.raw.m_003),
        Cat("m_004", R.drawable.c04, R.string.t04, R.raw.m_004),
        Cat("m_005", R.drawable.c05, R.string.t05, R.raw.m_005),
        Cat("m_006", R.drawable.c06, R.string.t06, R.raw.m_006),
        Cat("m_007", R.drawable.c07, R.string.t07, R.raw.m_007),
        Cat("m_008", R.drawable.c08, R.string.t08, R.raw.m_008),
        Cat("m_009", R.drawable.c09, R.string.t09, R.raw.m_009),
        Cat("m_010", R.drawable.c10, R.string.t10, R.raw.m_010),
        Cat("m_011", R.drawable.c11, R.string.t11, R.raw.m_011),
        Cat("m_012", R.drawable.c12, R.string.t12, R.raw.m_012),
        Cat("m_013", R.drawable.c13, R.string.t13, R.raw.m_013),
        Cat("m_014", R.drawable.c14, R.string.t14, R.raw.m_014),
        Cat("m_015", R.drawable.c15, R.string.t15, R.raw.m_015),
        Cat("m_016", R.drawable.c16, R.string.t16, R.raw.m_016),
        Cat("m_017", R.drawable.c17, R.string.t17, R.raw.m_017),
        Cat("m_018", R.drawable.c18, R.string.t18, R.raw.m_018),
        Cat("m_019", R.drawable.c19, R.string.t19, R.raw.m_019),
        Cat("m_020", R.drawable.c20, R.string.t20, R.raw.m_020),
        Cat("m_021", R.drawable.c21, R.string.t21, R.raw.m_021),
        Cat("m_022", R.drawable.c22, R.string.t22, R.raw.m_022),
        Cat("m_023", R.drawable.c23, R.string.t23, R.raw.m_023),
        Cat("m_024", R.drawable.c24, R.string.t24, R.raw.m_024),
        Cat("m_025", R.drawable.c25, R.string.t25, R.raw.m_025),
        Cat("m_026", R.drawable.c26, R.string.t26, R.raw.m_026),
        Cat("m_027", R.drawable.c27, R.string.t27, R.raw.m_027),
    )
}
