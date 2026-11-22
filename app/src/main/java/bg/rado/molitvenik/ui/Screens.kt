package bg.rado.molitvenik.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bg.rado.molitvenik.AppState
import bg.rado.molitvenik.data.Prayer
import bg.rado.molitvenik.data.Search
import bg.rado.molitvenik.shareText
import java.time.LocalDate

// ---------------------------------------------------------------------------
// Търсене (начален екран)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(state: AppState, onOpen: (String) -> Unit, onCalendar: () -> Unit) {
    val focus = LocalFocusManager.current
    val results = remember(state.query, state.category) {
        Search.search(state.items, state.query, state.category)
    }
    val showHome = state.query.isBlank() && state.category == null
    val verse = remember { state.repo.verseOfDay(LocalDate.now().dayOfYear) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Джобен молитвеник",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = { state.query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Търси: здраве, път, скръб, деца…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { state.query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Изчисти")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                state.categories.forEach { c ->
                    FilterChip(
                        selected = state.category == c,
                        onClick = { state.category = if (state.category == c) null else c },
                        label = { Text(c) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        }
        if (showHome) {
            item { TodayCard(LocalDate.now(), onClick = onCalendar) }
        }
        if (showHome && verse != null) {
            item { VerseOfDayCard(verse) { onOpen(verse.id) } }
        }
        item {
            Text(
                if (showHome) "Всички молитви и стихове" else "Намерени: ${results.size}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (results.isEmpty()) {
            item {
                Text(
                    "Нищо не е намерено. Опитай с друга дума, например „болен“, „работа“, „страх“.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(results, key = { it.id }) { p ->
            PrayerCard(p, state.isFavorite(p.id)) { onOpen(p.id) }
        }
    }
}

@Composable
private fun VerseOfDayCard(verse: Prayer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "СТИХ ЗА ДЕНЯ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "„${verse.text}“",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "— ${verse.source}",
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun PrayerCard(p: Prayer, favorite: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (p.isVerse) "СТИХ · ${p.source}" else p.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (p.isVerse) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (favorite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "В любими",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                p.text.replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Четене на молитва / стих
// ---------------------------------------------------------------------------

@Composable
fun PrayerScreen(state: AppState, id: String, onBack: () -> Unit) {
    val p = state.repo.byId(id)
    if (p == null) {
        Text("Молитвата не е намерена.", Modifier.padding(24.dp))
        return
    }
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val speaking = state.speaker.speakingId == p.id

    // Спира четенето, когато излезеш от молитвата.
    DisposableEffect(p.id) { onDispose { state.speaker.stop() } }

    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                p.category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = { state.speaker.toggle(p.id, p.title + ". " + p.text) }) {
                Icon(
                    if (speaking) Icons.Filled.Close else Icons.Filled.PlayArrow,
                    contentDescription = if (speaking) "Спри четенето" else "Прочети на глас",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(onClick = { state.changeFont(-2f) }) { Text("A−") }
            TextButton(onClick = { state.changeFont(2f) }) { Text("A+", fontWeight = FontWeight.Bold) }
            IconButton(onClick = { state.toggleFavorite(p.id) }) {
                Icon(
                    if (state.isFavorite(p.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Любими",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                p.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                if (p.isVerse) "„${p.text}“" else p.text,
                fontFamily = FontFamily.Serif,
                fontSize = state.fontSize.sp,
                lineHeight = (state.fontSize * 1.5f).sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (p.source.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "— ${p.source}",
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { shareText(ctx, p.shareText(), telegram = true) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("В Telegram")
            }
            OutlinedButton(onClick = { shareText(ctx, p.shareText(), telegram = false) }) {
                Icon(Icons.Filled.Share, contentDescription = "Сподели", modifier = Modifier.size(18.dp))
            }
            OutlinedButton(onClick = {
                clipboard.setText(AnnotatedString(p.shareText()))
                Toast.makeText(ctx, "Копирано", Toast.LENGTH_SHORT).show()
            }) {
                Text("Копирай")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Любими
// ---------------------------------------------------------------------------

@Composable
fun FavoritesScreen(state: AppState, onOpen: (String) -> Unit) {
    val favs = state.items.filter { state.isFavorite(it.id) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Любими",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (favs.isEmpty()) {
            item {
                Text(
                    "Още няма любими. Отвори молитва и натисни ♡ горе вдясно, за да я добавиш тук.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(favs, key = { it.id }) { p -> PrayerCard(p, true) { onOpen(p.id) } }
    }
}

// ---------------------------------------------------------------------------
// Молитвен списък — членовете на групата
// ---------------------------------------------------------------------------

@Composable
fun PrayerListScreen(state: AppState) {
    val ctx = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var need by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Молитвен списък",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Запиши имената на членовете на групата и за какво да се молим. " +
                    "Списъкът се пази само на твоя телефон.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Име") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    )
                    OutlinedTextField(
                        value = need,
                        onValueChange = { need = it },
                        label = { Text("За какво (по желание)") },
                        placeholder = { Text("здраве, път, работа…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            state.addEntry(name, need)
                            name = ""
                            need = ""
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Добави")
                    }
                }
            }
        }

        if (state.listEntries.isEmpty()) {
            item {
                Text("Списъкът е празен.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(state.listEntries, key = { it.id }) { e ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.name, style = MaterialTheme.typography.titleMedium)
                        if (e.need.isNotBlank()) {
                            Text(e.need, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { state.removeEntry(e) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Премахни", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (state.listEntries.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { shareText(ctx, state.listPrayerText(), telegram = true) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Изпрати молитвата в Telegram")
                    }
                    OutlinedButton(onClick = { shareText(ctx, state.listPrayerText(), telegram = false) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Сподели", modifier = Modifier.size(18.dp))
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        state.listPrayerText(),
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
