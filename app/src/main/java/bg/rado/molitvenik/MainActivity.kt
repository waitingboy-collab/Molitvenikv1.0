package bg.rado.molitvenik

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import bg.rado.molitvenik.data.PrayerRepository
import bg.rado.molitvenik.data.Prefs
import bg.rado.molitvenik.ui.CalendarScreen
import bg.rado.molitvenik.ui.ChotkaScreen
import bg.rado.molitvenik.ui.FavoritesScreen
import bg.rado.molitvenik.ui.MolitvenikTheme
import bg.rado.molitvenik.ui.PrayerListScreen
import bg.rado.molitvenik.ui.PrayerScreen
import bg.rado.molitvenik.ui.SearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Светла тема винаги: тъмни икони в статус лентата, независимо от системния режим.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        speaker = Speaker(applicationContext)
        val state = AppState(PrayerRepository(applicationContext), Prefs(applicationContext), speaker!!)
        setContent {
            MolitvenikTheme { MolitvenikApp(state) }
        }
    }

    private var speaker: Speaker? = null

    override fun onDestroy() {
        speaker?.shutdown()
        speaker = null
        super.onDestroy()
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("search", "Търсене", Icons.Filled.Search),
    Tab("calendar", "Календар", Icons.Filled.DateRange),
    Tab("chotka", "Чотка", Icons.Filled.Refresh),
    Tab("favorites", "Любими", Icons.Filled.Favorite),
    Tab("list", "Списък", Icons.AutoMirrored.Filled.List),
)

@Composable
fun MolitvenikApp(state: AppState) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabRoutes = remember { TABS.map { it.route }.toSet() }

    val openPrayer: (String) -> Unit = { id -> nav.navigate("prayer/$id") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route == null || route in tabRoutes) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = "search", modifier = Modifier.padding(padding)) {
            composable("search") {
                SearchScreen(state, openPrayer, onCalendar = {
                    nav.navigate("calendar") {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable("calendar") { CalendarScreen() }
            composable("chotka") { ChotkaScreen() }
            composable("favorites") { FavoritesScreen(state, openPrayer) }
            composable("list") { PrayerListScreen(state) }
            composable("prayer/{id}") { entry ->
                PrayerScreen(state, entry.arguments?.getString("id") ?: "", onBack = { nav.popBackStack() })
            }
        }
    }
}
