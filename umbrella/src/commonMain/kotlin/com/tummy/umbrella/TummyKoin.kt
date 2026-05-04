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
 * Shared DI entry point. Called from composeApp (Android) and iosApp (Swift).
 *
 * Named `startTummyKoin` (not `initTummyKoin`) to avoid conflicting with Swift
 * initializers in SKIE interop.
 *
 * @param appDeclaration platform hook (androidContext, androidLogger, extra modules...).
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
 * ViewModels as pure Koin factories. On Android, composeApp re-declares the
 * same VMs with `viewModelOf(...)` to bind them to the ViewModelStoreOwner.
 */
private val viewModelsModule = module {
    factory { HomeViewModel(get(), get(), get()) }
    factory { params -> LogIngredientViewModel(params.get(), get(), get()) }
    factory { params -> LogSymptomViewModel(params.get(), get()) }
}

/**
 * Typed DI façade for Swift. Avoids the
 * `KoinPlatformTools.defaultContext().get().get(objCClass: ...)` dance on the
 * Swift side: each getter here returns a concrete type, directly usable.
 */
object TummyDI {
    fun homeViewModel(): HomeViewModel = KoinPlatform.getKoin().get()

    fun logIngredientViewModel(date: LocalDate): LogIngredientViewModel = KoinPlatform.getKoin().get { parametersOf(date) }

    fun logSymptomViewModel(date: LocalDate): LogSymptomViewModel = KoinPlatform.getKoin().get { parametersOf(date) }
}
