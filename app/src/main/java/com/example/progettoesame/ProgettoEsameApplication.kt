package com.example.progettoesame

import android.app.Application
import com.example.progettoesame.data.SyncManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class ProgettoEsameApplication : Application() {
    private val syncManager: SyncManager by inject()
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ProgettoEsameApplication)
            modules(appModule)
            workManagerFactory()
        }
        syncManager.schedulePeriodicSync()
        syncManager.schedulePeriodicHardDelete()
    }
}