package com.renote.renoteai.di

import com.renote.renoteai.database.source.pref.AccountPreference
import com.renote.renoteai.repository.DataRepository
import org.koin.dsl.module

val repositoryModule = module {

    single { AccountPreference(get()) }
    factory { DataRepository(get()) }

}