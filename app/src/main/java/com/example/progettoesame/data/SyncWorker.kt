package com.example.progettoesame.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.progettoesame.data.repositories.SyncRepository

class SyncWorker(appContext: Context, workerParams: WorkerParameters, private val repository: SyncRepository)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.fullSync()
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error: ${e.message}")
            Result.retry()
        }
    }
}