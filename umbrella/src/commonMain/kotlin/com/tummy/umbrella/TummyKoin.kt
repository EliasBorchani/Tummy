package com.tummy.umbrella

import com.tummy.data.ingredients.di.ingredientsModule
import com.tummy.data.symptoms.di.symptomsModule
import com.tummy.features.home.HomeViewModel
import com.tummy.features.log.ingredient.LogIngredientViewModel
import com.tummy.features.log.symptom.LogSymptomViewModel
import com.tummy.utilities.network.NetworkConfig
import com.tummy.utilities.network.createHttpClient
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
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
        ingredientsModule,
        symptomsModule,
        viewModelsModule,
    )
    modules(platformModules)
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
    factory { HomeViewModel(get(), get(), get()) }
    factory { params -> LogIngredientViewModel(params.get(), get(), get()) }
    factory { params -> LogSymptomViewModel(params.get(), get()) }
}

/**
 * Façade DI typée pour Swift. Évite d'avoir à passer par
 * `KoinPlatformTools.defaultContext().get().get(objCClass: ...)` côté Swift :
 * chaque getter ici retourne un type concret, directement utilisable.
 */
object TummyDI {
    fun homeViewModel(): HomeViewModel = KoinPlatform.getKoin().get()

    fun logIngredientViewModel(date: LocalDate): LogIngredientViewModel =
        KoinPlatform.getKoin().get { parametersOf(date) }

    fun logSymptomViewModel(date: LocalDate): LogSymptomViewModel =
        KoinPlatform.getKoin().get { parametersOf(date) }
}
