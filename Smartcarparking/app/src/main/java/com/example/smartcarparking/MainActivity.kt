package com.example.smartcarparking

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.smartcarparking.ui.theme.SmartCarParkingTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kapps.circularprogressindicatoryt.CustomCircularProgressIndicator

import androidx.compose.ui.text.font.FontWeight
import com.example.smartcarparking.ui.theme.white
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import com.airbnb.lottie.compose.*
import com.example.smartcarparking.ui.theme.backgroundColor
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.rememberShimmer


    class MainActivity : ComponentActivity() {
        private lateinit var database: FirebaseDatabase

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            // Initialize Firebase
            database = Firebase.database
            val parkingRef = database.getReference("parking_spots")

            setContent {
                var coinAmount by remember { mutableStateOf(150) }
                var showAddCoinsDialog by remember { mutableStateOf(false) }

                SmartCarParkingTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = { AppTopBar(
                            coinAmount = coinAmount,
                            onRefreshClick = {
                                // Reset booking status to false for all spots
                                resetBookingStatus(parkingRef)
                            },
                            onAddCoinsClick = { showAddCoinsDialog = true }
                        ) }
                    ) { innerPadding ->
                        ParkingSpots(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(top = 42.dp),
                            parkingRef = parkingRef,
                            coinAmount = coinAmount,
                            onCoinAmountChange = { newAmount ->
                                coinAmount = newAmount
                            }
                        )
                        if (showAddCoinsDialog) {
                            AddCoinsDialog(
                                onDismissRequest = { showAddCoinsDialog = false },
                                onCoinsAdded = { amount ->
                                    coinAmount += amount
                                }
                            )
                        }
                    }
                }
            }
        }



    private fun resetBookingStatus(parkingRef: com.google.firebase.database.DatabaseReference) {
        parkingRef.child("A1").child("booked").setValue(false)
        parkingRef.child("A2").child("booked").setValue(false)
        parkingRef.child("A3").child("booked").setValue(false)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    coinAmount: Int,
    onRefreshClick: () -> Unit,
    onAddCoinsClick: () -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    val isVisible by remember {
        derivedStateOf {
            lastClickTime != 0L && System.currentTimeMillis() - lastClickTime < 1000
        }
    }

    LaunchedEffect(lastClickTime) {
        if (lastClickTime != 0L) {
            delay(1000)
            lastClickTime = 0L
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        color = Color(0xFF0E0E0E),
        shape = RoundedCornerShape(
            bottomStart = 42.dp,
            bottomEnd = 42.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Title text
            Text(
                "Smart Car Parking",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 75.dp)
            )

            // Refresh button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 35.dp, top = 75.dp)
                    .clickable {
                        lastClickTime = System.currentTimeMillis()
                        onRefreshClick()
                    }
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Coin display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 15.dp, bottom = 40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = "Coins",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFFEDA),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .padding(horizontal = 25.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$coinAmount",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Add the plus button
                IconButton(
                    onClick = onAddCoinsClick,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add coins",
                        tint = Color(0xFFFFBB00)
                    )
                }
            }
        }
    }
}

@Composable
fun ParkingSpots(
    modifier: Modifier = Modifier,
    parkingRef: com.google.firebase.database.DatabaseReference,
    coinAmount: Int,
    onCoinAmountChange: (Int) -> Unit
) {
    // States to track booking status from Firebase
    val (a1Status, setA1Status) = remember { mutableStateOf(false) }
    val (a2Status, setA2Status) = remember { mutableStateOf(false) }
    val (a3Status, setA3Status) = remember { mutableStateOf(false) }

    val (a1Occupied, setA1Occupied) = remember { mutableStateOf(false) }
    val (a2Occupied, setA2Occupied) = remember { mutableStateOf(false) }
    val (a3Occupied, setA3Occupied) = remember { mutableStateOf(false) }

    val openDialog = remember { mutableStateOf<String?>(null) }

    val timers = remember { mutableStateMapOf<String, Long>() }

    // Add shimmer state
    var showShimmer by remember { mutableStateOf(true) }
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)


    // Timer coroutine
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Update every second
            timers.forEach { (spotName, remainingTime) ->
                if (remainingTime > 0) {
                    timers[spotName] = remainingTime - 1
                } else {
                    // When timer reaches 0, update Firebase
                    parkingRef.child(spotName).child("booked").setValue(false)
                    timers.remove(spotName)
                }
            }
        }
    }

    // Hide shimmer after 1 second
    LaunchedEffect(Unit) {
        delay(1000)
        showShimmer = false
    }


    // Listen to Firebase changes
    LaunchedEffect(Unit) {
        parkingRef.child("A1").child("booked").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA1Status(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        parkingRef.child("A1").child("occupied").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA1Occupied(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        parkingRef.child("A2").child("booked").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA2Status(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })


        parkingRef.child("A2").child("occupied").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA2Occupied(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        parkingRef.child("A3").child("booked").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA3Status(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        parkingRef.child("A3").child("occupied").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setA3Occupied(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Set background color here
    ) {
        // Changed from Column to LazyColumn
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            // Shimmer effect items
            if (showShimmer) {
                items(3) { index ->
                    ShimmerParkingCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    if (index < 2) Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Actual parking spot items
                item {
                    ParkingSpotCard(
                        spotName = "A1",
                        isBooked = a1Status,
                        isOccupied = a1Occupied,
                        remainingTime = timers["A1"] ?: 0,
                        onBookClick = { openDialog.value = "A1" }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ParkingSpotCard(
                        spotName = "A2",
                        isBooked = a2Status,
                        isOccupied = a2Occupied,
                        remainingTime = timers["A2"] ?: 0,
                        onBookClick = { openDialog.value = "A2" }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ParkingSpotCard(
                        spotName = "A3",
                        isBooked = a3Status,
                        isOccupied = a3Occupied,
                        remainingTime = timers["A3"] ?: 0,
                        onBookClick = { openDialog.value = "A3" }
                    )
                }
            }
        }

        // Dialogs (unchanged)
        when (openDialog.value) {
            "A1" -> ShowDialog(
                onDismissRequest = { openDialog.value = null },
                parkingRef = parkingRef,
                spotName = "A1",
                currentStatus = a1Status,
                coinAmount = coinAmount,
                onCoinAmountChange = onCoinAmountChange,
                onTimerSet = { sliderValue ->
                    val minutes = sliderValue.coerceAtLeast(1)
                    timers["A1"] = TimeUnit.MINUTES.toSeconds(minutes.toLong())
                }
            )

            "A2" -> ShowDialog(
                onDismissRequest = { openDialog.value = null },
                parkingRef = parkingRef,
                spotName = "A2",
                currentStatus = a2Status,
                coinAmount = coinAmount,
                onCoinAmountChange = onCoinAmountChange,
                onTimerSet = { sliderValue ->
                    val minutes = sliderValue.coerceAtLeast(1)
                    timers["A2"] = TimeUnit.MINUTES.toSeconds(minutes.toLong())
                }
            )

            "A3" -> ShowDialog(
                onDismissRequest = { openDialog.value = null },
                parkingRef = parkingRef,
                spotName = "A3",
                currentStatus = a3Status,
                coinAmount = coinAmount,
                onCoinAmountChange = onCoinAmountChange,
                onTimerSet = { sliderValue ->
                    val minutes = sliderValue.coerceAtLeast(1)
                    timers["A3"] = TimeUnit.MINUTES.toSeconds(minutes.toLong())
                }
            )
        }
    }
}


@Composable
fun ShimmerParkingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(180.dp) // Match your actual card height
            .shimmer(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        border = BorderStroke(1.dp, Color(0xFFADD8E6))
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ParkingSpotCard(
    spotName: String,
    isBooked: Boolean,
    isOccupied: Boolean,
    remainingTime: Long,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)


    // Lottie animation composition
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.car_animation)
    )

    // Animation progress (0f to 1f)
    var animationProgress by remember { mutableStateOf(0f) }

    // Animation states
    var showCar by remember { mutableStateOf(isBooked || isOccupied) }
    val carOffsetX by animateDpAsState(
        targetValue = when {
            !showCar -> (250).dp // Start off-screen left
            isBooked || isOccupied -> 0.dp // Center when booked/occupied
            else -> (-300).dp // Move off-screen right when unbooked
        },
        animationSpec = tween(durationMillis = 1100)
    )

    // Handle animation when status changes
    LaunchedEffect(isBooked, isOccupied) {
        if (isBooked || isOccupied) {
            // Car entering animation
            showCar = true
            animationProgress = 0f
            animate(0f, 1f, animationSpec = tween(1000)) { value, _ ->
                animationProgress = value
            }
        } else {
            // Car exiting animation
            animate(1f, 0f, animationSpec = tween(1000)) { value, _ ->
                animationProgress = value
            }
            //delay(100) // Wait for animation to complete
            showCar = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEDA)),
        border = BorderStroke(1.dp, Color(0xFFADD8E6))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Timer at the top (only shown when booked)
            if (isBooked) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(bottom = 8.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Spot name
                Text(
                    text = spotName,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Animated Lottie car
                if (showCar) {
                    LottieAnimation(
                        composition = composition,
                        progress = { animationProgress },
                        modifier = Modifier
                            .size(180.dp)
                            .offset(x = carOffsetX)
                    )
                }

                // BOOK button (only shown when not booked and not occupied)
                if (!isBooked && !isOccupied && !showCar) {
                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0E0E0E),
                            contentColor = Color(0xFFFFBB00)
                        ),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("BOOK")
                    }
                }
            }
        }
    }
}


@Composable
fun ShowDialog(
    onDismissRequest: () -> Unit,
    parkingRef: com.google.firebase.database.DatabaseReference,
    spotName: String,
    currentStatus: Boolean,
    coinAmount: Int,
    onCoinAmountChange: (Int) -> Unit,
    onTimerSet: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(0) }


    val amount = remember { derivedStateOf { selectedMinutes * 10 } } // 10 Rs per minute

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(white),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CustomCircularProgressIndicator(
                        modifier = Modifier
                            .size(225.dp)
                            .background(white),
                        initialValue = 0,
                        primaryColor = Color.Black,
                        secondaryColor = Color.DarkGray,
                        circleRadius = 80f,
                        onPositionChange = { minutes ->
                            selectedMinutes = minutes
                        }
                    )
                }

                // Amount display section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Booking Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Duration:",
                            color = Color.Gray
                        )
                        Text(
                            text = "$selectedMinutes min",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rate:",
                            color = Color.Gray
                        )
                        Text(
                            text = "10 Rs/min",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Total Amount:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Text(
                            text = "${amount.value} Rs",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFBB00),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Your Coins:",
                            color = Color.Gray
                        )
                        Text(
                            text = "$coinAmount Rs",
                            color = if (coinAmount >= amount.value) Color.Green else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 22.dp)
                ) {
                    SlideToBook(
                        onSlideComplete = {
                            if (coinAmount >= amount.value && selectedMinutes > 0 ) {
                                // Deduct coins only if sufficient balance
                                onCoinAmountChange(coinAmount - amount.value)
                                parkingRef.child(spotName).child("booked").setValue(!currentStatus)
                                onTimerSet(selectedMinutes)
                                onDismissRequest()
                            }
                        }
                    )
                }
            }
        }
    }
}