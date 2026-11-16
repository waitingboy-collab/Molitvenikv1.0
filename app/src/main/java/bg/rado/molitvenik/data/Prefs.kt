package bg.rado.molitvenik.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Човек от молитвения списък (член на групата и за какво да се молим). */
data class ListEntry(
    val id: Long,
    val name: String,
    val need: String,
)

/** Локално съхранение на телефона: любими, размер на шрифта, молитвен списък. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("molitvenik", Context.MODE_PRIVATE)

    fun favorites(): Set<String> = sp.getStringSet(KEY_FAV, emptySet())?.toSet() ?: emptySet()

    fun saveFavorites(ids: Set<String>) {
        sp.edit().putStringSet(KEY_FAV, ids.toSet()).apply()
    }

    fun fontSize(): Float = sp.getFloat(KEY_FONT, 19f)

    fun saveFontSize(size: Float) {
        sp.edit().putFloat(KEY_FONT, size).apply()
    }

    fun listEntries(): List<ListEntry> {
        val raw = sp.getString(KEY_LIST, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                ListEntry(o.getLong("id"), o.getString("name"), o.optString("need", ""))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveList(entries: List<ListEntry>) {
        val arr = JSONArray()
        entries.forEach {
            arr.put(JSONObject().put("id", it.id).put("name", it.name).put("need", it.need))
        }
        sp.edit().putString(KEY_LIST, arr.toString()).apply()
    }

    private companion object {
        const val KEY_FAV = "favorites"
        const val KEY_FONT = "font_size"
        const val KEY_LIST = "prayer_list"
    }
}
