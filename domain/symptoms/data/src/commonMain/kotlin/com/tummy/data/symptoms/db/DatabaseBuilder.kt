package com.tummy.data.symptoms.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

expect fun symptomsDatabaseBuilder(): RoomDatabase.Builder<SymptomsDatabase>

fun buildSymptomsDatabase(): SymptomsDatabase {
    return symptomsDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()
}
