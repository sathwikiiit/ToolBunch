package com.example.toolbunch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigation.NavHostController

@Composable
fun LandSizeCalculator(navController: NavHostController) {
    var acres by remember { mutableStateOf("") }
    var guntas by remember { mutableStateOf("") }
    var totalGuntas by remember { mutableDoubleStateOf(0.0) }
    var marketValuePerAcre by remember { mutableStateOf("") }
    val previousTotalGuntas = remember { mutableStateListOf<Double>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Optional padding around the content
        contentAlignment = Alignment.Center // Center the content
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Land Size Calculator",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = acres,
                    onValueChange = { acres = it },
                    label = { Text("Ac.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle( color = Color.Black, fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = guntas,
                    onValueChange = { guntas = it },
                    label = { Text("Gts.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle( color = Color.Black, fontSize = 16.sp)
                )
            }

            Row {
                Button(onClick = {
                    val acresValue = acres.toDoubleOrNull() ?: 0.0
                    val guntasValue = guntas.toDoubleOrNull() ?: 0.0
                    val totalGuntasValue = acresValue * 40 + guntasValue
                    totalGuntas += totalGuntasValue
                    previousTotalGuntas.add(totalGuntasValue)
                    acres = ""
                    guntas = ""
                }) {
                    Text("Add")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (previousTotalGuntas.isNotEmpty()) {
                        val lastEntry =
                            previousTotalGuntas.removeAt(previousTotalGuntas.lastIndex)
                        totalGuntas -= lastEntry
                    }
                }) {
                    Text("Undo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculate total acres and guntas from totalGuntas
            val totalAcres = (totalGuntas / 40).toInt()
            val remainingGuntas = (totalGuntas % 40)

            Text(
                text = "Total: $totalAcres Ac. - ${"%.2f".format(remainingGuntas)} Gts.",
                style = TextStyle(fontSize = 20.sp, color = Color.Blue)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = marketValuePerAcre,
                onValueChange = { marketValuePerAcre = it },
                label = { Text("Market Value/Acre (INR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle( color = Color.Black, fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            var totalMarketValue=0.0
            // Calculate and display total market value if market value is provided
            if (marketValuePerAcre.isNotEmpty()) {
                val marketValue = marketValuePerAcre.toDoubleOrNull() ?: 0.0
                totalMarketValue = totalGuntas * marketValue/40

                Text(
                    text = "Total Market Value: ₹${"%,.2f".format(totalMarketValue)}",
                    style = TextStyle(fontSize = 20.sp, color = Color.Green)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("marketValue", totalMarketValue)
                    navController.navigate("second")
                }) {
                Text("Go to CourtFeeCalculator")
            }


            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Previous Entries:")
            previousTotalGuntas.takeLast(4).forEach { entry ->
                val entryAcres = (entry / 40).toInt()
                val entryGuntas = (entry % 40)
                Text(text = "$entryAcres Ac. - ${"%.2f".format(entryGuntas)} Gts.")
            }
        }
    }
}
