# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/mac/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For Ktor
-keepattributes Signature
-keepattributes *Annotation*
-keep class io.ktor.** { *; }
-dontwarn io.ktor.util.debug.IntellijIdeaDebugDetector
-dontwarn java.lang.management.**

# For Kotlin Serialization
-keepclassmembernames class * {
    @kotlinx.serialization.SerialName <fields>;
}

# For Koin
-keep class org.koin.** { *; }

# For Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# For Compose
-keep class androidx.compose.runtime.** { *; }
