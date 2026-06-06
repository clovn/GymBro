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
            MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPKIT_API_KEY)
            MapKitFactory.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
