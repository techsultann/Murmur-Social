package com.sultlab.murmur

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform