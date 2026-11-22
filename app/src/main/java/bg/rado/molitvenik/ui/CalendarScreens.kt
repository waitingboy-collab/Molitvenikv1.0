package bg.rado.molitvenik.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bg.rado.molitvenik.data.ChurchCalendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val BG = Locale("bg")
private val DAY_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM", BG)
private val SHORT_FMT = DateTimeFormatter.ofPattern("d MMMM, EEEE", BG)

private fun daysWord(n: Long) = when (n) {
    0L -> "днес"
    1L -> "утре"
    else -> "след $n дни"
}

// ---------------------------------------------------------------------------
// Карта „Днес“ — на началния екран и в календара
// ---------------------------------------------------------------------------

@Composable
fun TodayCard(date: LocalDate, onClick: (() -> Unit)? = null) {
    val day = remember(date) { ChurchCalendar.day(date) }
    val fast = remember(date) { ChurchCalendar.fast(date) }
    val next = remember(date) { if (day.feasts.isEmpty()) ChurchCalendar.nextMajor(date) else null }

    val mod = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable { onClick() } else it }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = mod,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                date.format(DAY_FMT).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            if (day.feasts.isEmpty()) {
                Text("Днес няма голям празник.", style = MaterialTheme.typography.bodyMedium)
                if (next != null) {
                    val n = ChronoUnit.DAYS.between(date, next.date)
                    Text(
                        "Следва: ${next.feasts.first { it.major }.title} — ${next.date.format(SHORT_FMT)} (${daysWord(n)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                day.feasts.forEach { f ->
                    Text(
                        f.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = if (f.major) FontWeight.Bold else FontWeight.Normal,
                        color = if (f.major) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (day.names.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Имен ден: ${day.names.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "🍞 $fast",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Календар
// ---------------------------------------------------------------------------

@Composable
fun CalendarScreen() {
    val today = remember { LocalDate.now() }
    var name by rememberSaveable { mutableStateOf("") }
    val nameResults = remember(name) { ChurchCalendar.findName(name, today) }
    val upcoming = remember { ChurchCalendar.upcoming(today.plusDays(1), 90) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Църковен календар",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item { TodayCard(today) }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Кога има имен ден… (напр. Иван)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) { Icon(Icons.Filled.Clear, contentDescription = "Изчисти") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        }
        if (name.trim().length >= 2) {
            if (nameResults.isEmpty()) {
                item {
                    Text(
                        "Не намерих имен ден за „${name.trim()}“ в календара.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(nameResults, key = { "n" + it.first.date.toString() }) { (d, matched) ->
                val n = ChronoUnit.DAYS.between(today, d.date)
                CalendarRow(
                    dateText = d.date.format(SHORT_FMT) + " · " + daysWord(n),
                    title = d.feasts.first { matched in it.names }.title,
                    subtitle = "Празнува: $matched",
                    major = false,
                )
            }
        } else {
            item {
                Text(
                    "Предстоящи празници",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            items(upcoming, key = { it.date.toString() }) { d ->
                val n = ChronoUnit.DAYS.between(today, d.date)
                CalendarRow(
                    dateText = d.date.format(SHORT_FMT) + " · " + daysWord(n),
                    title = d.feasts.joinToString(" · ") { it.title },
                    subtitle = if (d.names.isEmpty()) null else "Имен ден: ${d.names.joinToString(", ")}",
                    major = d.feasts.any { it.major },
                )
            }
            item {
                Text(
                    "Календарът и постите са опростени. За точни разпоредби виж църковния календар на БПЦ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun CalendarRow(dateText: String, title: String, subtitle: String?, major: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(dateText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(2.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (major) FontWeight.Bold else FontWeight.Normal,
                color = if (major) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Чотка (броеница) за Иисусовата молитва
// ---------------------------------------------------------------------------

private fun vibrate(context: Context, ms: Long) {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
}

@Composable
fun ChotkaScreen() {
    val ctx = LocalContext.current
    val view = LocalView.current
    var total by rememberSaveable { mutableIntStateOf(0) }
    var goal by rememberSaveable { mutableIntStateOf(33) }

    // Екранът не изгасва, докато се молиш с чотката.
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val inRound = when {
        goal == 0 -> total
        total > 0 && total % goal == 0 -> goal
        else -> total % goal
    }
    val rounds = if (goal == 0) 0 else total / goal

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Чотка",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Господи Иисусе Христе, Сине Божий,\nпомилуй мене грешния.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 19.sp,
            lineHeight = 27.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(33, 50, 100, 0).forEach { g ->
                FilterChip(
                    selected = goal == g,
                    onClick = { goal = g; total = 0 },
                    label = { Text(if (g == 0) "Без край" else "$g") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            if (goal > 0) {
                CircularProgressIndicator(
                    progress = { inRound.toFloat() / goal },
                    modifier = Modifier.size(260.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(224.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable {
                        total += 1
                        if (goal > 0 && total % goal == 0) vibrate(ctx, 400) else vibrate(ctx, 25)
                    },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$inRound",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        if (goal == 0) "докосни за всяка молитва" else "от $goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        if (goal > 0) {
            Text(
                "Завършени броеници: $rounds   ·   Общо: $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { total = 0 }) { Text("Започни отначало") }
        Spacer(Modifier.height(8.dp))
        Text(
            "Телефонът вибрира леко при всяко докосване и по-силно при завършена броеница.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(300.dp),
        )
    }
}
