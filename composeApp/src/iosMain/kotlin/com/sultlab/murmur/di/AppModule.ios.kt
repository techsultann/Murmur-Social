package com.sultlab.murmur.di

import com.sultlab.murmur.data.local.AppDatabase
import com.sultlab.murmur.data.local.DeviceHashStore
import com.sultlab.murmur.data.local.LikesStore
import com.sultlab.murmur.data.local.getDatabaseBuilder
import com.sultlab.murmur.data.local.getRoomDatabase
import com.sultlab.murmur.service.IosTokenRegistrar
import com.sultlab.murmur.ui.AppPreferences
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DeviceHashStore() }
    single { AppPreferences() }
    single { LikesStore() }
    single { IosTokenRegistrar() }
    single<AppDatabase> { getRoomDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().postDao() }
    single { get<AppDatabase>().groupMessageDao() }
}
