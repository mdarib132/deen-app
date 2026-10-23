package com.deen.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deen.app.data.AppState
import com.deen.app.prayer.CITIES
import com.deen.app.prayer.PrayerCalc
import com.deen.app.prayer.Prayer
import com.deen.app.prayer.Reminders
import java.time.LocalDate

@Composable
fun DeenApp(state: AppState) {
    val dark = isDark(state.themeMode)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    DeenTheme(dark) {
        val c = LocalDeen.current
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val route = entry?.destination?.route?.substringBefore('?')?.substringBefore('/')
        val tab = NAV_ITEMS.firstOrNull { it.route == route }?.route

        val go: (String) -> Unit = { target ->
            if (NAV_ITEMS.any { it.route == target }) nav.navigateTab(target)
            else nav.navigate(target)
        }
        val back: () -> Unit = { if (!nav.popBackStack()) nav.navigateTab("home") }

        Box(Modifier.fillMaxSize().background(c.bg)) {
            NavHost(
                nav, startDestination = "home",
                enterTransition = { fadeIn(tween(220)) }, exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(220)) }, popExitTransition = { fadeOut(tween(160)) },
            ) {
                composable("home") { HomeScreen(state, go) }
                composable("duas?cat={cat}", listOf(navArgument("cat") { type = NavType.StringType; nullable = true; defaultValue = null })) {
                    DuasScreen(state, it.arguments?.getString("cat"), go)
                }
                composable("dua/{id}") { DuaDetailScreen(state, it.arguments?.getString("id") ?: "eat_before", back, go) }
                composable("salah?guide={guide}", listOf(navArgument("guide") { type = NavType.StringType; nullable = true; defaultValue = null })) {
                    SalahScreen(state, it.arguments?.getString("guide"), go)
                }
                composable("surahs") { SurahsScreen(state, go) }
                composable("surah/{id}", listOf(navArgument("id") { type = NavType.IntType })) {
                    SurahDetailScreen(state, it.arguments?.getInt("id") ?: 1, back)
                }
                composable("zikr?set={set}", listOf(navArgument("set") { type = NavType.StringType; nullable = true; defaultValue = null })) {
                    ZikrScreen(it.arguments?.getString("set"), go)
                }
                composable("fazilat") { FazilatScreen(back, go) }
                composable("settings") { SettingsScreen(state, back) }
            }
            if (tab != null) {
                PillNav(
                    tab, { nav.navigateTab(it) },
                    Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SettingsScreen(state: AppState, back: () -> Unit) {
    val c = LocalDeen.current
    val ctx = LocalContext.current
    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.setReminders(granted)
        Reminders.scheduleNext(ctx)
    }
    val today = LocalDate.now()
    val times = remember(state.cityIndex, state.hanafiAsr) { PrayerCalc.times(state.prefs, today) }

    Screen(bottomExtra = 32.dp) {
        item { TopBar("সেটিংস", back) }
        item {
            DCard {
                Txt("নামাজের রিমাইন্ডার", 16, weight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Txt("প্রতি ওয়াক্তের শুরুতে একটি নোটিফিকেশন", 14, Modifier.weight(1f), color = c.body)
                    Switch(
                        checked = state.reminders,
                        onCheckedChange = { on ->
                            if (on && Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else { state.setReminders(on); Reminders.scheduleNext(ctx) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = c.green, checkedThumbColor = Color.White,
                            uncheckedTrackColor = c.soft, uncheckedThumbColor = c.muted, uncheckedBorderColor = c.border
                        )
                    )
                }
            }
        }
        item {
            DCard {
                Txt("শহর", 16, weight = FontWeight.Bold)
                Txt("নামাজের সময় এই শহর অনুযায়ী হিসাব হয়", 13, color = c.muted)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CITIES.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { city ->
                                val i = CITIES.indexOf(city)
                                Box(Modifier.weight(1f)) {
                                    Segmented(listOf(city.name), if (state.cityIndex == i) 0 else -1) {
                                        state.setCity(i); Reminders.scheduleNext(ctx)
                                    }
                                }
                            }
                            repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        item {
            DCard {
                Txt("আসরের সময়", 16, weight = FontWeight.Bold)
                Segmented(listOf("হানাফি", "শাফেয়ি / অন্যান্য"), if (state.hanafiAsr) 0 else 1) {
                    state.setHanafi(it == 0); Reminders.scheduleNext(ctx)
                }
                Txt("পদ্ধতি: করাচি (ফজর ও ইশা ১৮°)। স্থানীয় মসজিদের সময়ের সাথে ১–২ মিনিট পার্থক্য হতে পারে।", 12, color = c.muted)
            }
        }
        item {
            DCard {
                Txt("আজকের সময়সূচি · ${CITIES[state.cityIndex].name}", 16, weight = FontWeight.Bold)
                Prayer.entries.forEach { p ->
                    Row(Modifier.fillMaxWidth()) {
                        Txt(p.label(today), 14, Modifier.weight(1f), color = c.body)
                        Txt(times[p]?.let { "${Bn.partOfDay(it)} ${Bn.clock(it)}" } ?: "", 14, weight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            DCard {
                Txt("থিম", 16, weight = FontWeight.Bold)
                Segmented(listOf("ফোনের মতো", "লাইট", "ডার্ক"), state.themeMode) { state.setTheme(it) }
            }
        }
        item {
            DCard {
                Txt("কনটেন্টের উৎস", 16, weight = FontWeight.Bold)
                Txt(
                    "কুরআনের আরবি: The Noble Qur’an Encyclopedia (উসমানি লিপি)। বাংলা অর্থ: মুহিউদ্দীন খান (Tanzil.net)। তিলাওয়াত: মিশারী রাশিদ আল-আফাসি (everyayah.com)। দোয়া ও হাদিস: প্রতিটির সাথে হাদিস নম্বর দেওয়া আছে। বাংলা উচ্চারণ সহায়তার জন্য — একজন আলেম বা কারীর কাছে মিলিয়ে নিন।",
                    13, color = c.body
                )
            }
        }
    }
}
