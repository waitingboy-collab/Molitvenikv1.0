package bg.rado.molitvenik.data

import java.util.Locale

/**
 * Търсене по ключови думи на български.
 *
 * - не прави разлика между главни и малки букви;
 * - разпознава различни форми на думата („болен“, „болни“, „болест“; „път“, „пътуване“)
 *   чрез просто отрязване на окончанието;
 * - тагове и заглавие тежат повече от текста;
 * - по-напред излизат резултатите, които съвпадат с повече от въведените думи.
 */
object Search {

    private val BG = Locale("bg")

    private val STOP_WORDS = setOf(
        "за", "на", "и", "от", "в", "във", "с", "със", "при", "по", "да", "се", "ми", "ме",
        "ни", "си", "не", "до", "към", "или", "че", "как", "какво", "молитва", "молитви",
    )

    fun normalize(s: String): String =
        s.lowercase(BG)
            .replace('ѝ', 'и')
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun words(s: String): List<String> = normalize(s).split(' ').filter { it.isNotEmpty() }

    /** Груб корен: „болести“ → „болес“, „пътуване“ → „пътува“, къси думи остават цели. */
    private fun stem(w: String): String =
        if (w.length <= 4) w else w.substring(0, maxOf(4, w.length - 2))

    private fun matches(word: String, token: String, tokenStem: String): Boolean =
        word == token ||
            word.startsWith(tokenStem) ||
            (word.length >= 4 && token.startsWith(stem(word)))

    private class Indexed(val prayer: Prayer) {
        val tagWords = prayer.tags.flatMap { words(it) } + words(prayer.category)
        val titleWords = words(prayer.title)
        val textWords = words(prayer.text) + words(prayer.source)
    }

    private var cacheKey: List<Prayer>? = null
    private var cache: List<Indexed> = emptyList()

    private fun index(items: List<Prayer>): List<Indexed> {
        if (cacheKey !== items) {
            cache = items.map { Indexed(it) }
            cacheKey = items
        }
        return cache
    }

    fun search(items: List<Prayer>, query: String, category: String?): List<Prayer> {
        val pool = index(items).filter { category == null || it.prayer.category == category }

        val allTokens = words(query)
        // Ако човек напише само „молитва“, не изхвърляме думата.
        val tokens = allTokens.filter { it !in STOP_WORDS }.ifEmpty { allTokens.filter { it.length >= 3 } }
        if (tokens.isEmpty()) return pool.map { it.prayer }

        return pool
            .mapNotNull { ix ->
                var matched = 0
                var score = 0
                for (t in tokens) {
                    val st = stem(t)
                    var s = 0
                    if (ix.tagWords.any { matches(it, t, st) }) s += 5
                    if (ix.titleWords.any { matches(it, t, st) }) s += 3
                    if (ix.textWords.any { matches(it, t, st) }) s += 1
                    if (s > 0) matched++
                    score += s
                }
                if (matched == 0) null else Triple(ix.prayer, matched, score)
            }
            .sortedWith(compareByDescending<Triple<Prayer, Int, Int>> { it.second }.thenByDescending { it.third })
            .map { it.first }
    }
}
