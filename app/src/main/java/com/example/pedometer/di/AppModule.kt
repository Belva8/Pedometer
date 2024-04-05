package com.example.pedometer.DI

import android.app.NotificationManager
import android.content.Context
import android.hardware.SensorManager
import com.example.pedometer.room_db.PedometerDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// modul Dagger Hilt pod nazivom AppModule koji pruža različite dependencies koje se koriste u cijeloj apk
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    //Omogućuje instancu PedometerDB. Kvalifikator @ApplicationContext označava da je kontekst proslijeđen kao parametar kontekst aplikacije
    fun providePedometerDB(@ApplicationContext context: Context): PedometerDB {
        return PedometerDB.getInstance(context)
    }

    @Provides
    @Singleton
    //Omogućuje instancu NotificationManager, Dohvaća uslugu sustava pomoću konteksta aplikacije i prenosi ju  NotificationManager-u
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    //Omogućuje instancu SensorManagera. Slično metodi provideNotificationManager...
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

}







