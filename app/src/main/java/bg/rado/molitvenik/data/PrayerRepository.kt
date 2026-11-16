package bg.rado.molitvenik.data

import android.content.Context
import org.json.JSONObject

data class Prayer(
    val id: String,
    val type: String,       // "молитва" или "стих"
    val title: String,
    val category: String,
    val text: String,
    val source: String,
    val tags: List<String>,
) {
    val isVerse: Boolean get() = type == "стих"

    /** Текст за споделяне в Telegram / Viber / копиране. */
    fun shareText(): String =
        if (isVerse) "📖 $title\n\n„$text“\n\n— $source"
        else "🙏 $title\n\n$text"
}

/** Зарежда молитвите и стиховете от assets/prayers.json. */
class PrayerRepository(context: Context) {

    val categories: List<String>
    val items: List<Prayer>

    init {
        val json = context.assets.open("prayers.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)

        val cats = root.getJSONArray("categories")
        categories = List(cats.length()) { cats.getString(it) }

        val arr = root.getJSONArray("items")
        items = List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val tagsArr = o.optJSONArray("tags")
            Prayer(
                id = o.getString("id"),
                type = o.optString("type", "молитва"),
                title = o.getString("title"),
                category = o.optString("category", ""),
                text = o.getString("text"),
                source = o.optString("source", ""),
                tags = if (tagsArr == null) emptyList() else List(tagsArr.length()) { tagsArr.getString(it) },
            )
        }
    }

    fun byId(id: String): Prayer? = items.firstOrNull { it.id == id }

    /** „Стих за деня“ — сменя се всеки ден, еднакъв за всички в групата. */
    fun verseOfDay(dayOfYear: Int): Prayer? {
        val verses = items.filter { it.isVerse }
        if (verses.isEmpty()) return null
        return verses[dayOfYear % verses.size]
    }
}
