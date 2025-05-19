package com.example.smartcarparking

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.smartcarparking.ui.theme.gray
import com.example.smartcarparking.ui.theme.orange
import kotlin.math.roundToInt

@Composable
fun SlideToBook(
    onSlideComplete: () -> Unit,
    text: String = "Slide to book",
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var sliderWidth by remember { mutableStateOf(0f) }
    val sliderHeight = 60.dp
    val thumbSize = 48.dp
    val density = LocalDensity.current

    // Calculate max offset (slider width - thumb size - padding)
    val maxOffset = remember {
        derivedStateOf {
            with(density) {
                sliderWidth - thumbSize.toPx() - 8.dp.toPx()
            }
        }
    }

    // Track if slider is being dragged
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(sliderHeight)
            .fillMaxWidth()
            .background(
                color = gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(percent = 50)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (offsetX >= maxOffset.value * 0.7f) {
                            onSlideComplete()
                        }
                        offsetX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxOffset.value)
                        change.consume()
                    }
                )
            }
            .onSizeChanged { sliderWidth = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        // Slider track text
        Text(
            text = "Slide to book",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (thumbSize + 16.dp).value.dp),
            textAlign = TextAlign.Center
        )

        // Slider thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(thumbSize)
                .background(
                    color = Color.Black,
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
        }

        // Animate the thumb back if not completed
        if (!isDragging && offsetX > 0f && offsetX < maxOffset.value * 0.7f) {
            LaunchedEffect(Unit) {
                animate(
                    initialValue = offsetX,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300)
                ) { value, _ ->
                    offsetX = value
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun showSlide() {
    SlideToBook(
        onSlideComplete = {
            // Handle slide completion (e.g., show a toast or log)
            println("Parking spot booked!")
        },
        modifier = Modifier
            .padding(16.dp) // Optional padding for better preview
    )
}
