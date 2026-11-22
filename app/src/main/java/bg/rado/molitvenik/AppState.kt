package bg.rado.molitvenik

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bg.rado.molitvenik.data.ListEntry
import bg.rado.molitvenik.data.Prayer
import bg.rado.molitvenik.data.PrayerRepository
import bg.rado.molitvenik.data.Prefs

/** Общото състояние на приложението (живее докато приложението е отворено). */
class AppState(val repo: PrayerRepository, private val prefs: Prefs, val speaker: Speaker) {

    val items: List<Prayer> get() = repo.items
    val categories: List<String> get() = repo.categories

    var query by mutableStateOf("")
    var category by mutableStateOf<String?>(null)

    var favorites by mutableStateOf(prefs.favorites())
        private set

    var fontSize by mutableStateOf(prefs.fontSize())
        private set

    val listEntries = mutableStateListOf<ListEntry>().apply { addAll(prefs.listEntries()) }

    fun isFavorite(id: String) = id in favorites

    fun toggleFavorite(id: String) {
        favorites = if (id in favorites) favorites - id else favorites + id
        prefs.saveFavorites(favorites)
    }

    fun changeFont(delta: Float) {
        fontSize = (fontSize + delta).coerceIn(14f, 34f)
        prefs.saveFontSize(fontSize)
    }

    fun addEntry(name: String, need: String) {
        if (name.isBlank()) return
        listEntries.add(ListEntry(System.currentTimeMillis(), name.trim(), need.trim()))
        prefs.saveList(listEntries)
    }

    fun removeEntry(entry: ListEntry) {
        listEntries.remove(entry)
        prefs.saveList(listEntries)
    }

    /** Молитва с всички имена от списъка — готова за изпращане в групата. */
    fun listPrayerText(): String {
        val names = listEntries.joinToString("\n") { e ->
            if (e.need.isBlank()) "• ${e.name}" else "• ${e.name} — ${e.need}"
        }
        return "🙏 Молитва за братята и сестрите\n\n" +
            "Господи Иисусе Христе, Боже наш, помени и помилвай рабите Твои:\n\n" +
            names +
            "\n\nДай им по Твоята милост всичко, от което имат нужда: здраве, мир, търпение " +
            "и укрепване във вярата. Пази ги от всяко зло и не ги оставяй. Амин."
    }
}

private val TELEGRAM_PACKAGES = listOf(
    "org.telegram.messenger",        // Telegram
    "org.telegram.messenger.web",    // Telegram (от сайта)
    "org.thunderdog.challegram",     // Telegram X
)

/** Споделяне на текст. При [telegram] = true се опитва да отвори директно Telegram. */
fun shareText(context: Context, text: String, telegram: Boolean) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    if (telegram) {
        for (pkg in TELEGRAM_PACKAGES) {
            try {
                context.startActivity(Intent(send).setPackage(pkg))
                return
            } catch (_: ActivityNotFoundException) {
                // пробваме следващия
            }
        }
        Toast.makeText(context, "Telegram не е намерен — избери приложение", Toast.LENGTH_SHORT).show()
    }
    context.startActivity(Intent.createChooser(send, "Сподели чрез"))
}
