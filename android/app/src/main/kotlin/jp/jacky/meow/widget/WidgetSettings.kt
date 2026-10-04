package jp.jacky.meow.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class WidgetSettings(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("widgets", Context.MODE_PRIVATE))

    fun read(id: Int): WidgetSelection {
        val pair = preferences.getString(id.toString(), null)?.split('|')
        return WidgetSelection.resolve(pair?.getOrNull(0), pair?.getOrNull(1))
    }

    fun save(id: Int, selection: WidgetSelection): Boolean {
        val valid = WidgetSelection.resolve(selection.soundID, selection.characterID)
        return preferences.edit().putString(id.toString(), "${valid.soundID}|${valid.characterID}").commit()
    }

    fun delete(ids: IntArray) {
        preferences.edit().apply { ids.forEach { remove(it.toString()) } }.apply()
    }

    @SuppressLint("ApplySharedPref")
    fun restore(oldIDs: IntArray, newIDs: IntArray) {
        // Read before writing: restored IDs can overlap with the old IDs.
        val restored = oldIDs.zip(newIDs).map { (old, new) -> new to read(old) }
        preferences.edit().apply {
            oldIDs.forEach { remove(it.toString()) }
            restored.forEach { (id, pair) -> putString(id.toString(), "${pair.soundID}|${pair.characterID}") }
        }.commit()
    }
}
