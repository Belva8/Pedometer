package com.example.pedometer.presentation.screens


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pedometer.R
import com.example.pedometer.common.DateUtil
import com.example.pedometer.domain.viewModel.ShareViewModel
import com.example.pedometer.presentation.screens.components.AnimatedCircularIndicator
import com.example.pedometer.presentation.screens.components.CustomDialog
import com.example.pedometer.presentation.screens.components.CustomText

import com.example.pedometer.service.RealtimeStepCounterService
import com.example.pedometer.ui.theme.Dimensions
import com.example.pedometer.ui.theme.TextDefaultColor
import kotlinx.coroutines.delay

private const val TAG = "PedometerScreen" //const. string koji se korisit kao TAG za logging

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PedometerScreen(
    shareViewModel: ShareViewModel = hiltViewModel()
) {
//    Text(text = "Pedometer Screen")

    val context = LocalContext.current

    IgnoreBatteryOptimizations()

    val shareUiState by shareViewModel.uiState.collectAsState()
    val stepsToday = shareUiState.stepsToday

    var timeOfClock by remember { mutableStateOf("") }
    var dateOfClock by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        while (true) {
            timeOfClock = DateUtil.timeOfClock()
            dateOfClock = DateUtil.dateOfClock()
            delay(500L)
        }
    }

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {},
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimensions.Seven))
                Text(
                    text = timeOfClock,
                    style = TextStyle(fontSize = 28.sp, color = MaterialTheme.colors.TextDefaultColor)
                )
                Text(
                    text = dateOfClock,
                    style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.TextDefaultColor)
                )
                Spacer(modifier = Modifier.height(Dimensions.Four))
                AnimatedCircularIndicator(stepsToday.toInt(), 10000)
                Spacer(modifier = Modifier.height(Dimensions.Seven))
                Row {
                    val stride = 65
                    val weight = 73
                    val distance = (stepsToday * stride / 100) / 1000f

                    CustomText(
                        text = "%.1f".format(distance * weight) + " kcal",
                        leadingIcon = painterResource(id = R.drawable.baseline_local_fire_department_24)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Two))
                    CustomText(
                        text = "%.2f".format(distance) + " km",
                        leadingIcon = painterResource(id = R.drawable.baseline_moving_24)
                    )
                }
            }
        },
        floatingActionButton = {},
        scaffoldState = scaffoldState
    )

    ComposableLifeCycle { _, event ->
        val intent = Intent(context, RealtimeStepCounterService::class.java)

        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                Log.d(TAG, "PedometerScreen: ON_RESUME")
                context.startForegroundService(intent)
            }
            Lifecycle.Event.ON_PAUSE -> {
                Log.d(TAG, "PedometerScreen: ON_PAUSE")
                context.stopService(intent)
            }
            else -> {}
        }
    }
}


//prati   lifecyle (ON_RESUME, ON_PAUSE) zaslona i pokretanje/zaustavljanje RealtimeStepCounterService po tome
@Composable
fun ComposableLifeCycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}



//Traži od korisnika da zanemari optimizacije baterije ako apk nije na popisu dopuštenih,
// zbog kontinuiranog rada  brojača koraka.
@SuppressLint("ObsoleteSdkInt", "BatteryLife")
@Composable
fun IgnoreBatteryOptimizations() {
    val activity = LocalContext.current as Activity

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

    val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packName = activity.packageName
    val isWhiteListing = pm.isIgnoringBatteryOptimizations(packName)

    if (isWhiteListing.not()) {
        CustomDialog(
            title = stringResource(id = R.string.ignore_dialog_title),
            text = stringResource(id = R.string.ignore_dialog_text),
            confirm = stringResource(id = R.string.ignore_dialog_confirm)
        ) {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).also {
                it.data = Uri.parse("package:$packName")
                activity.startActivity(it)
            }
        }
    }
}


