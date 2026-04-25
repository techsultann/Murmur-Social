package com.sultlab.murmur.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.context.GlobalContext

class PushTokenSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val token = inputData.getString(KEY_TOKEN)
            ?: return Result.failure()

        return try {

            val registrar: TokenRegistrar =
                GlobalContext.get().get()

            registrar.register(token)

            Result.success()

        } catch (e: Exception) {

            // network/server/transient issue
            Result.retry()
        }
    }

    companion object {
        const val KEY_TOKEN = "push_token"
    }
}