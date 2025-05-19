package com.example.smartcarparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

class LottieTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LottieAnimationPreview()
        }
    }
}

@Composable
fun LottieAnimationPreview() {
    // Load the animation from raw resource
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.car_animation))

    // State to control animation progress
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever // Loops the animation
    )

    // Display the Lottie animation
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewLottieAnimation() {
    LottieAnimationPreview()
}
*/