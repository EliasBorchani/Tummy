package com.tummy.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tummy.android.feature.diary.DiaryScreen
import com.tummy.android.feature.meal.MealScreen
import com.tummy.android.feature.settings.SettingsScreen

object Routes {
    const val Meal = "meal"
    const val Diary = "diary"
    const val Settings = "settings"
}

@Composable
fun TummyNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Diary) {
        composable(Routes.Diary)    { DiaryScreen(onOpenMeals = { nav.navigate(Routes.Meal) }) }
        composable(Routes.Meal)     { MealScreen(onOpenDetail = { /* detail route à venir */ }) }
        composable(Routes.Settings) { SettingsScreen() }
    }
}
