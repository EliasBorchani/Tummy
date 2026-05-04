package com.tummy.data.symptoms.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [SymptomLogEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(SymptomsDatabaseConstructor::class)
abstract class SymptomsDatabase : RoomDatabase() {
    abstract fun symptomLogDao(): SymptomLogDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object SymptomsDatabaseConstructor : RoomDatabaseConstructor<SymptomsDatabase> {
    override fun initialize(): SymptomsDatabase
}
