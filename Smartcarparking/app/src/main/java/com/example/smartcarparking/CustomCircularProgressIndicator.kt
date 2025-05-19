package com.kapps.circularprogressindicatoryt

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcarparking.ui.theme.darkGray
import com.example.smartcarparking.ui.theme.gray
import com.example.smartcarparking.ui.theme.orange
import com.example.smartcarparking.ui.theme.white
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


fun formatTimeFromProgress(positionValue: Int): String {
   /* val totalSeconds = positionValue * 15
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
*/
    return "$positionValue min"
}


@Composable
fun CustomCircularProgressIndicator(
    modifier: Modifier = Modifier,
    initialValue:Int,
    primaryColor: Color,
    secondaryColor:Color,
    minValue:Int = 0,
    maxValue:Int = 20,
    circleRadius:Float,
    onPositionChange:(Int)->Unit
) {
    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var positionValue by remember {
        mutableStateOf(initialValue)
    }



    Box(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val newValue = calculateProgressFromTouch(change.position, circleCenter, circleRadius, minValue, maxValue)
                positionValue = newValue
                onPositionChange(newValue)
            }
        }
    ){
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ){
            val width = size.width
            val height = size.height
            val circleThickness = minOf(width, height) / 15f // Relative thickness
            circleCenter = Offset(x = width / 2f, y = height / 2f)
            val circleRadius = (minOf(width, height) - circleThickness) / 2f // Dynamic radius


            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        primaryColor.copy(0.45f),
                        secondaryColor.copy(0.15f)
                    )
                ),
                radius = circleRadius,
                center = circleCenter
            )


            drawCircle(
                style = Stroke(
                    width = circleThickness
                ),
                color = secondaryColor,
                radius = circleRadius,
                center = circleCenter
            )

            drawArc(
                color = primaryColor,
                startAngle = 90f,
                sweepAngle = (360f/maxValue) * positionValue.toFloat(),
                style = Stroke(
                    width = circleThickness,
                    cap = StrokeCap.Round
                ),
                useCenter = false,
                size = Size(
                    width = circleRadius * 2f,
                    height = circleRadius * 2f
                ),
                topLeft = Offset(
                    (width - circleRadius * 2f)/2f,
                    (height - circleRadius * 2f)/2f
                )

            )

            val outerRadius = circleRadius + circleThickness/2f
            val gap = 15f
            for (i in 0 .. (maxValue-minValue)){
                val color = if(i < positionValue-minValue) primaryColor else primaryColor.copy(alpha = 0.3f)
                val angleInDegrees = i*360f/(maxValue-minValue).toFloat()
                val angleInRad = angleInDegrees * PI / 180f + PI/2f

                val yGapAdjustment = cos(angleInDegrees * PI / 180f)*gap
                val xGapAdjustment = -sin(angleInDegrees * PI / 180f)*gap

                val start = Offset(
                    x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                    y = (outerRadius * sin(angleInRad) + circleCenter.y + yGapAdjustment).toFloat()
                )

                val end = Offset(
                    x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                    y = (outerRadius * sin(angleInRad) + circleThickness + circleCenter.y + yGapAdjustment).toFloat()
                )

                rotate(
                    angleInDegrees,
                    pivot = start
                ){
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = 1.dp.toPx()
                    )
                }

            }

            drawContext.canvas.nativeCanvas.apply {
                drawIntoCanvas {
                    drawText(
                        formatTimeFromProgress(positionValue),
                        circleCenter.x,
                        circleCenter.y + 45.dp.toPx()/3f,
                        Paint().apply {
                            textSize = 38.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = white.toArgb()
                            isFakeBoldText = true
                        }
                    )
                }
            }

        }
    }
}

fun calculateProgressFromTouch(touchOffset: Offset,
                               center: Offset,
                               radius: Float,
                               minValue: Int,
                               maxValue: Int
): Int {
    val dx = touchOffset.x - center.x
    val dy = touchOffset.y - center.y
    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    val normalizedAngle = ((angle + 360) % 360) // Normalize to 0-360
    val progress = ((normalizedAngle / 360f) * (maxValue - minValue)).toInt()
    return progress.coerceIn(minValue, maxValue)
}

/*
@Preview(showBackground = true)
@Composable
fun Preview() {
    CustomCircularProgressIndicator(
        modifier = Modifier
            .size(250.dp)
            .background(darkGray)
        ,
        initialValue = 0,
        primaryColor = orange,
        secondaryColor = gray,
        circleRadius = 230f,
        onPositionChange = {

        }
    )
}
 */


