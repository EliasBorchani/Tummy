package com.tummy.umbrella

import com.tummy.data.meal.di.mealModule
import com.tummy.data.nutrition.di.nutritionModule
import com.tummy.data.user.di.userModule
import com.tummy.features.diary.DiaryViewModel
import com.tummy.features.meal.MealViewModel
import com.tummy.features.settings.SettingsViewModel
import com.tummy.utilities.network.NetworkConfig
import com.tummy.utilities.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * Point d'entrée DI partagé. Appelé depuis composeApp (Android) et iosApp (Swift).
 *
 * Nommé `startTummyKoin` (pas `initTummyKoin`) pour éviter le conflit avec les
 * initializers Swift lors de l'interop SKIE.
 *
 * @param appDeclaration hook plateforme (androidContext, androidLogger, modules en plus…).
 */
fun startTummyKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(
        networkModule,
        mealModule,
        userModule,
        nutritionModule,
        viewModelsModule,
    )
}

fun stopTummyKoin() = stopKoin()

private val networkModule = module {
    single<HttpClient> {
        createHttpClient(
            NetworkConfig(baseUrl = "https://api.tummy.app", enableLogging = true),
        )
    }
}

/**
 * ViewModels côté "pur Koin" (factory). Pour Android, composeApp re-déclare
 * ces mêmes VM avec `viewModelOf(...)` pour le binding ViewModelStoreOwner.
 */
private val viewModelsModule = module {
    factory { MealViewModel(get(), get()) }
    factory { DiaryViewModel(get(), get()) }
    factory { SettingsViewModel(get()) }
}

/**
 * Façade DI typée pour Swift. Évite d'avoir à passer par
 * `KoinPlatformTools.defaultContext().get().get(objCClass: ...)` côté Swift :
 * chaque getter ici retourne un type concret, directement utilisable.
 */
object TummyDI {
    fun mealViewModel(): MealViewModel = KoinPlatform.getKoin().get()
    fun diaryViewModel(): DiaryViewModel = KoinPlatform.getKoin().get()
    fun settingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()
}
