package com.example.pedometer


import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pedometer.presentation.permissions.CheckAndRequestPermissions
import com.example.pedometer.presentation.screens.HomeScreen
import com.example.pedometer.ui.theme.PedometerTheme
import com.example.pedometer.worker.StepsCounterWorker
import dagger.hilt.android.AndroidEntryPoint


// postavlja glavnu aktivnost apk-a
// obavlja provjere dopuštenja i inicijalizira pozadinske zadatke .

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val listOfPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PedometerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckAndRequestPermissions(
                        permissions = listOfPermissions
                    ) {
                        HomeScreen()
                    }

                    StepsCounterWorker.periodicWorker(this)
                }
            }
        }
    }
}

