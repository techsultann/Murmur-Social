import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.buildkonfig.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.googleServices)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += "-Xbinary=bundleId=com.sultlab.murmur"
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.data.store)
            implementation(libs.androidx.security.crypto)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.core.splash.screen)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.material3.adaptiveNavigation3)
            implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)
            implementation(libs.jetbrains.lifecycle.viewmodel)
            implementation(libs.kotlinx.serialization.core)

            //koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.test)
            implementation(libs.koin.core)

            //supabase
            implementation(project.dependencies.platform(libs.supabase))
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.functions)
            implementation(libs.supabase.realtime)

            //data-store
            implementation(libs.data.store)

            //ktor
            implementation(libs.ktor.client.core)

            implementation(libs.kotlinx.coroutines.core)

            //kermit
            implementation(libs.kermit)

            //firebase
            implementation(libs.firebase.messaging)

            //work manager
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.work.testing)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.sultlab.murmur"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sultlab.murmur"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

fun loadEnv(env: String): Properties {
    val file = rootProject.file("env/$env.properties")

    if (!file.exists()) {
        throw GradleException("Missing env file: env/$env.properties")
    }

    return Properties().apply {
        file.inputStream().use { load(it) }
    }
}

val allowedEnvs = listOf("staging", "prod")

val currentEnv = (project.findProperty("env") as String? ?: "staging").also {
    require(it in allowedEnvs) {
        "Invalid env: $it. Must be one of $allowedEnvs"
    }
}

println("🔧 Building with environment: $currentEnv")

val envProps = loadEnv(currentEnv)

fun Properties.require(key: String): String =
    getProperty(key) ?: error("$key missing for env=$currentEnv")

buildkonfig {
    packageName = "com.sultlab.murmur"

    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", envProps.require("SUPABASE_URL"))
        buildConfigField(STRING, "SUPABASE_PUBLISHABLE_KEY", envProps.require("SUPABASE_PUBLISHABLE_KEY"))
        buildConfigField(STRING, "ENV", currentEnv)
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
