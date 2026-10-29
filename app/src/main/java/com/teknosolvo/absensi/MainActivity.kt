package com.teknosolvo.absensi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teknosolvo.absensi.data.TYPE_IN
import com.teknosolvo.absensi.ui.screens.CheckInScreen
import com.teknosolvo.absensi.ui.screens.DetailScreen
import com.teknosolvo.absensi.ui.screens.HistoryScreen
import com.teknosolvo.absensi.ui.screens.HomeScreen
import com.teknosolvo.absensi.ui.screens.SettingsScreen
import com.teknosolvo.absensi.ui.screens.SetupScreen
import com.teknosolvo.absensi.ui.theme.AbsensiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AbsensiApp
        setContent {
            AbsensiTheme {
                AbsensiNavHost(needsSetup = app.settings.state.value.employeeName.isBlank())
            }
        }
    }
}

@Composable
private fun AbsensiNavHost(needsSetup: Boolean) {
    val nav = rememberNavController()
    val start = remember { if (needsSetup) "setup" else "home" }
    NavHost(navController = nav, startDestination = start) {
        composable("setup") {
            SetupScreen(onDone = {
                nav.navigate("home") { popUpTo("setup") { inclusive = true } }
            })
        }
        composable("home") {
            HomeScreen(
                onCheck = { type -> nav.navigate("checkin/$type") },
                onHistory = { nav.navigate("history") },
                onSettings = { nav.navigate("settings") },
            )
        }
        composable("checkin/{type}") { entry ->
            CheckInScreen(
                type = entry.arguments?.getString("type") ?: TYPE_IN,
                onDone = { nav.popBackStack() },
                onBack = { nav.popBackStack() },
            )
        }
        composable("history") {
            HistoryScreen(onOpen = { id -> nav.navigate("detail/$id") }, onBack = { nav.popBackStack() })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            DetailScreen(id = entry.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
