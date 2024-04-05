package com.example.pedometer.worker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pedometer.R
import com.example.pedometer.common.Constants.NOTIFICATION_CHANNEL
import com.example.pedometer.room_db.StepsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



// Klasa odgovorna za pozadinski rad i procese i update-a bazu podataka
@HiltWorker
class StepsCounterWorker @AssistedInject constructor
    (@Assisted appContext: Context,
     @Assisted params: WorkerParameters
)
    :
    CoroutineWorker(appContext, params) //uzima appContext i params kao parametre, (od WorkManager-a)

{

    @Inject
    lateinit var stepsRepository: StepsRepository


    //Metoda  WorkManager-a da izvršava pozadinske procese.
    // Dohvaća senzor koraka, registrira listener tza praćenje  promjene  broja koraka i update-a bazu podataka sa novim podacima koraka.
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())

        val sensorManager = applicationContext
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        return if (stepCounterSensor !== null) {
            val success = getStepsCount(sensorManager, stepCounterSensor)
            if (success) Result.success() else Result.retry()

        } else {
            Result.failure()
        }
    }
//Suspend funkcija koja registrira listener za pracenje promjene broja koraka pomoću senzora.
// Suspenda dok su podaci o broju koraka dobivene ,onda upddate-a bazu podataka i nastavlja coroutine sa success flag.
    private suspend fun  getStepsCount (
        sensorManager: SensorManager,
        stepCounterSensor: Sensor
    ) =  suspendCoroutine<Boolean> {

            continuation ->    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            sensorManager.unregisterListener(this)
            event?.values?.firstOrNull()?.let { steps ->
                val stepsToday = stepsRepository.updateStepsSinceBoot(steps.toLong())
                Log.d(TAG,"Step Count : $steps, steps today : $stepsToday")
                continuation.resume(true)
                return
            }

            continuation.resume(false)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, sensor?.name + " accuracy changed : " + accuracy)
        }

    }



        sensorManager.registerListener(
            listener,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }



 //pruža inforamcije o  foreground notification da budu display-ane dok worker radi.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title= applicationContext.getString(R.string.step_counter)
        val content = applicationContext.getString(R.string.obtaining_latest_step)

        val notification = NotificationCompat.Builder(applicationContext,NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setOngoing(true)
            .build()


        return ForegroundInfo(WORKER_NOTIFY_ID,notification)

    }


//objekt za utility metode
    companion object {

        private const val TAG =  "StepsCounterWorker"
        private const val WORKER_NOTIFY_ID= 345267


    //Metoda za  periodic work koristeci WorkManager.
    // Kreira  PeriodicWorkRequest za StepsCounterWorker klasu da se pokreće svakih 15 minutes and postavlja  ih saposebnim tag-om
        fun periodicWorker(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<StepsCounterWorker>(
                15, TimeUnit.MINUTES
            )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("periodic_pedometer_worker",
                ExistingPeriodicWorkPolicy.REPLACE,workRequest
            )
        }


    }


}