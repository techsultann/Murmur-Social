package com.sultlab.murmur

import com.sultlab.murmur.di.appModule
import com.sultlab.murmur.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration) {
    startKoin {
        appDeclaration.invoke(this)
        modules(appModule, platformModule)
    }
}

fun initKoin() {
    initKoin { }
}
