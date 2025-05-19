package com.example.smartcarparking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddCoinsDialog(
    onDismissRequest: () -> Unit,
    onCoinsAdded: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color( 0xFFFFFEDA)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Coins",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            showError = false
                        },
                        label = { Text("Amount (Rs)") },
                        isError = showError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (showError) {
                        Text(
                            text = "Please enter a valid amount",
                            color = Color.Red,
                            modifier = Modifier.padding(top = 4.dp)
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
                            val coinsToAdd = amount.toIntOrNull()
                            if (coinsToAdd != null && coinsToAdd > 0) {
                                onCoinsAdded(coinsToAdd)
                                onDismissRequest()
                            } else {
                                showError = true
                            }
                        },
                        text = "Slide to pay"
                    )
                }
            }
        }
    }
}