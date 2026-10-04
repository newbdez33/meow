package jp.jacky.meow.widget

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.jacky.meow.Cats
import jp.jacky.meow.R

data class WidgetCharacter(val id: String, @StringRes val name: Int, @DrawableRes val image: Int)

object WidgetCharacters {
    val all = listOf(
        WidgetCharacter("ginger", R.string.widget_ginger, R.drawable.widget_ginger),
        WidgetCharacter("black", R.string.widget_black, R.drawable.widget_black),
        WidgetCharacter("gray", R.string.widget_gray, R.drawable.widget_gray),
        WidgetCharacter("sleepy", R.string.widget_sleepy, R.drawable.widget_sleepy),
        WidgetCharacter("pumpkin", R.string.widget_pumpkin, R.drawable.widget_pumpkin),
        WidgetCharacter("ghost", R.string.widget_ghost, R.drawable.widget_ghost),
        WidgetCharacter("classic", R.string.widget_classic, R.drawable.widget_classic),
    )
}

data class WidgetSelection(val soundID: String = "m_004", val characterID: String = "ginger") {
    val cat get() = Cats.all.first { it.id == soundID }
    val character get() = WidgetCharacters.all.first { it.id == characterID }

    companion object {
        fun resolve(soundID: String?, characterID: String?) = WidgetSelection(
            soundID = soundID?.takeIf { id -> Cats.all.any { it.id == id } } ?: "m_004",
            characterID = characterID?.takeIf { id -> WidgetCharacters.all.any { it.id == id } } ?: "ginger",
        )
    }
}
