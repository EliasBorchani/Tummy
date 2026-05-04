package com.tummy.data.symptoms.di

import com.tummy.data.symptoms.SymptomRepositoryImpl
import com.tummy.data.symptoms.db.SymptomsDatabase
import com.tummy.data.symptoms.db.buildSymptomsDatabase
import com.tummy.domain.symptoms.repository.SymptomRepository
import org.koin.dsl.module

val symptomsModule = module {
    single { buildSymptomsDatabase() }
    single { get<SymptomsDatabase>().symptomLogDao() }
    single<SymptomRepository> { SymptomRepositoryImpl(get()) }
}
