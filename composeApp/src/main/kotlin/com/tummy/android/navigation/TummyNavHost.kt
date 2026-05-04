package com.tummy.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tummy.android.feature.home.HomeScreen
import com.tummy.android.feature.log.LogIngredientScreen
import com.tummy.android.feature.log.LogSymptomScreen
import kotlinx.datetime.LocalDate

object Routes {
    const val Home = "home"
    const val LogIngredient = "logIngredient/{date}"
    const val LogSymptom = "logSymptom/{date}"

    fun logIngredient(date: LocalDate): String = "logIngredient/$date"
    fun logSymptom(date: LocalDate): String = "logSymptom/$date"
}

@Composable
fun TummyNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Home) {
        composable(Routes.Home) {
            HomeScreen(
                onNavigateToLogIngredient = { nav.navigate(Routes.logIngredient(it)) },
                onNavigateToLogSymptom = { nav.navigate(Routes.logSymptom(it)) },
            )
        }
        composable(Routes.LogIngredient) { entry ->
            val dateStr = entry.arguments?.getString("date")
            if (dateStr != null) {
                LogIngredientScreen(
                    date = LocalDate.parse(dateStr),
                    onClose = { nav.popBackStack() },
                )
            }
        }
        composable(Routes.LogSymptom) { entry ->
            val dateStr = entry.arguments?.getString("date")
            if (dateStr != null) {
                LogSymptomScreen(
                    date = LocalDate.parse(dateStr),
                    onClose = { nav.popBackStack() },
                )
            }
        }
    }
}
