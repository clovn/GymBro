package ru.itis.gymbro

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.itis.gymbro.di.appModule

class GymBroApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Dependency Injection (Koin)
        startKoin {
            androidLogger()
            androidContext(this@GymBroApplication)
            modules(appModule)
        }

        // 2. Initialize Yandex MapKit Factory safely
        try {
            // Placeholder/dummy API key for building successfully.
            // Replace with your real Yandex MapKit API key when ready.
            MapKitFactory.setApiKey("00000000-0000-0000-0000-000000000000")
            MapKitFactory.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
