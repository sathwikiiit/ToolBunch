package com.example.toolbunch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil


@Composable
fun CourtFeeCalculator(numericValue:Double=0.0) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var formattedValue by remember { mutableStateOf(currencyFormatter.format(numericValue)) }
    var courtFee by remember { mutableStateOf(currencyFormatter.format(0.0)) } // State variable for court fee


    // Use Box to center the content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Optional padding around the content
        contentAlignment = Alignment.Center // Center the content
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
        ) {
            OutlinedTextField(
                value = formattedValue,
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.9f),
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                onValueChange = { newValue: String ->
                    // Parse the input value, handling potential formatting characters
                    val parsedValue = newValue.replace("[^\\d.]".toRegex(), "") // Remove non-numeric characters
                    val numericValue = parsedValue.toDoubleOrNull() ?: 0.0

                    // Update the state and format the value
                    formattedValue = currencyFormatter.format(numericValue)
                },
                label = { Text("Enter Suit Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val numericValue = formattedValue.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    courtFee = currencyFormatter.format(calculateCourtFee(numericValue))
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Submit")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Court Fee: $courtFee",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp), // Add padding around the text
                style = TextStyle(
                    color = Color.Blue, // Change color to blue
                    fontSize = 20.sp, // Increase font size
                    fontWeight = FontWeight.Bold // Make the text bold
                )
            )
        }
    }
}
private fun calculateCourtFee(x: Double): Double {
    return when {
        x == 0.0 -> {
            0.0
        }

        x > 0 && x <= 100 -> {
            ceil(x / 5) * 0.6
        }

        x > 100 && x <= 1000 -> {
            calculateCourtFee(100.00) + ceil((x - 100) / 10) * 1.1
        }

        x > 1000 && x <= 10000 -> {
            calculateCourtFee(1000.00) + ceil((x - 1000) / 100) * 7.50
        }

        x > 10000 && x <= 20000 -> {
            calculateCourtFee(10000.00) + ceil((x - 10000) / 500) * 30
        }

        x > 20000 && x <= 30000 -> {
            calculateCourtFee(20000.00) + ceil((x - 20000) / 1000) * 40
        }

        x > 30000 && x <= 50000 -> {
            calculateCourtFee(30000.00) + ceil((x - 30000) / 2000) * 60
        }

        x > 50000 && x <= 100000 -> {
            calculateCourtFee(50000.00) + ceil((x - 50000) / 4000) * 80
        }

        else -> {
            calculateCourtFee(100000.00) + ceil((x - 100000) / 10000) * 100
        }
    }
}

