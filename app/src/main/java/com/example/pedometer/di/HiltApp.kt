package com.example.pedometer.di




import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.pedometer.common.Constants.NOTIFICATION_CHANNEL
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


// služi kao ulazna točka za postavljanje injection dependency Hilt u apk.
// Inicijalizira potrebne komponente i pruža konf.  funkcioniranje WorkManager-a, uključujući definiranje kanala obavijesti ako ga verzija Androida uređaja podržava.
@HiltAndroidApp
class HiltApp : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    //Kad se apk pokreće. Inicijalizira  komponente za obavjesti
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(channel)
        }
    }

   // metoda koja  pruža konfiguraciju za WorkManager. Postavlja HiltWorkerFactory kao factory worker za WorkManager-a.
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
