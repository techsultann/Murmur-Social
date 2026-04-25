package com.sultlab.murmur

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.sultlab.murmur.service.TokenRegistrar
import com.sultlab.murmur.ui.AppViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val tokenRegistrar: TokenRegistrar by inject()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch {
                tokenRegistrar.fetchAndRegister()
            }
        }
    }
    private val appViewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        splashScreen.setKeepOnScreenCondition {
            !appViewModel.uiState.value.isReady
        }

        setContent {
            App(viewModel = appViewModel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) {
                lifecycleScope.launch {
                    tokenRegistrar.fetchAndRegister()
                }
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            lifecycleScope.launch {
                tokenRegistrar.fetchAndRegister()
            }
        }
    }
}
