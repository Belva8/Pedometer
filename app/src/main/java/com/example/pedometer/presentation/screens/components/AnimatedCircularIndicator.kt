package com.example.pedometer.presentation.screens.components

import android.icu.text.DecimalFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pedometer.ui.theme.*


//krug za progress

@Composable
fun AnimatedCircularIndicator(
    currentValue: Int,
    maxValue: Int,
    backgroundColor: Color = LightPastel,
    indicatorColor: Color = DarkPastel
) {
    val strokeCircle = with(LocalDensity.current) {
        Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
    }

    val strokeArc = with(LocalDensity.current) {
        Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
    }

    Box(contentAlignment = Alignment.Center) {
        ShowText(
            currentValue = currentValue,
            maxValue = maxValue
        )

        val targetValue = currentValue / maxValue.toFloat()

        Canvas(
            Modifier
                .progressSemantics(currentValue / maxValue.toFloat())
                .size(240.dp)
        ) {
            val startAngle = 270f    // početak , 12 sati
            val sweep: Float = targetValue * 360f
            val diameterOffset = strokeCircle.width / 2 - 6  //-6 udaljenost izmedu 2 kruga

            drawCircle(
                color = backgroundColor,
                style = strokeCircle,
                radius = size.minDimension / 2.0f - diameterOffset
            )

            drawCircularIndicator(
                startAngle, sweep, strokeArc
            )
        }
    }
}

@Composable
private fun ShowText(
    currentValue: Int,
    maxValue: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = DecimalFormat("#,###").format(currentValue),
            style = TextStyle(
                color = MaterialTheme.colors.TextDefaultColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "/" + DecimalFormat("#,###").format(maxValue),
            style = TextStyle(
                color = MaterialTheme.colors.TextHintColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    stroke: Stroke
) {
    //Za crtanje ovog kruga potreban nam je pravokutnik s rubovima koji se poravnavaju sa središtem crte.
    //  moramo sklonit polovicu širine poteza od ukupnog promjera za obje strane.
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset

    drawArc(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFDD835),
                Color(0xFFFF8A65),
                Color(0xFFBB86FC),
                Color(0xFF1D41C5)
            )
        ),

        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke
    )
}

@Preview(showBackground = true)
@Composable
fun CircularIndicatorPreview() {
    PedometerTheme {
        AnimatedCircularIndicator(currentValue = 2000, maxValue = 3000)
    }
}




