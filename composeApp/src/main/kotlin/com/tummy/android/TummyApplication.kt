package com.tummy.android

import android.app.Application
import com.tummy.android.di.androidAppModule
import com.tummy.android.di.androidDataModules
import com.tummy.data.ingredients.db.IngredientsAndroidDatabaseContext
import com.tummy.data.meal.db.AndroidDatabaseContext
import com.tummy.data.symptoms.db.SymptomsAndroidDatabaseContext
import com.tummy.umbrella.startTummyKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TummyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        AndroidDatabaseContext.applicationContext = this
        IngredientsAndroidDatabaseContext.applicationContext = this
        SymptomsAndroidDatabaseContext.applicationContext = this
        startTummyKoin {
            androidLogger()
            androidContext(this@TummyApplication)
            modules(androidAppModule)
            modules(androidDataModules)
        }
    }
}
