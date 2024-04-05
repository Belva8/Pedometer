package com.example.pedometer.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pedometer.R
import com.example.pedometer.common.Constants.NOTIFICATION_CHANNEL
import com.example.pedometer.room_db.StepsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject




private const val TAG = "RealtimeStepCounterService"
private const val NOTIFY_ID = 457239

// Servis odgovoran za praćenje koraka u realnom vremenom

@AndroidEntryPoint
class RealtimeStepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var stepsRepository: StepsRepository
    @Inject
    lateinit var sensorManager: SensorManager // .Registrira senzor za pracenje koraka

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(NOTIFY_ID, createNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerSensor()
        return super.onStartCommand(intent, flags, startId)
    }
//Metoda kad je servis unisten. Unregisters sensor, zaustavlja sve
    override fun onDestroy() {
        unRegisterSensor()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun registerSensor() {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null)
            Log.d(TAG, "No Step Sensor detected on this device")
        else
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private fun unRegisterSensor() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) { //Metoda kad se promjeni broj u koracima. Update-a bazu podataka sa novim koracima.
        if (event!!.values[0] > Int.MAX_VALUE || event.values[0] == 0f) return

        event.values?.firstOrNull()?.let { steps ->
            val stepsToday = stepsRepository.updateStepsSinceBoot(steps.toLong())
//            todayStepsFlow.value = stepsToday
//            _todaySteps.tryEmit(stepsToday) ne radi
            Log.d(TAG, "Koraci od ponovnog pokretanja: $steps, Koraci danas: $stepsToday")
        }
    }
//Metoda kad se preciznost senzora promjeni.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, sensor?.name + " accuracy changed: " + accuracy)
    }

    private fun createNotification(context: Context): Notification { //Stvara obavijest koja dolazi kad aplikacija radi.
        val title = "Step Counter"
        val content = "Obtaining the realtime step count..."

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setOngoing(true)
            .build()
    }

}




