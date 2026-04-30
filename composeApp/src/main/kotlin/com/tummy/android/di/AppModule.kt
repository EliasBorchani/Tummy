package com.tummy.android.di

import com.tummy.features.diary.DiaryViewModel
import com.tummy.features.meal.MealViewModel
import com.tummy.features.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Module Android pour câbler les VM au cycle de vie ViewModelStoreOwner.
 * Les VM sont déjà `@Factory`, mais côté Android on veut la version
 * koinViewModel() qui lie au NavBackStackEntry — d'où ce module.
 */
val androidAppModule = module {
    viewModelOf(::MealViewModel)
    viewModelOf(::DiaryViewModel)
    viewModelOf(::SettingsViewModel)
}
