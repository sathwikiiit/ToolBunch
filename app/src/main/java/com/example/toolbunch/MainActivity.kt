package com.example.toolbunch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.toolbunch.ui.theme.ToolBunchTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolBunchTheme {
                NavigationGraph()
            }
        }
    }
    @Composable
    fun NavigationGraph() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "grid") {
            composable("grid") { GridScreen(navController) }
            composable("first") { LandSizeCalculator(navController) }
            composable("second") {
                val marketValue = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<Double>("marketValue") ?: 0.0
                Log.println(Log.INFO,"MainActivity","Log $marketValue")

                Greeting(
                    numericValue = marketValue
                )
            }

        }
    }

    @Composable
    fun GridScreen(navController: NavHostController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                item {
                    CalculatorCard(
                        title = "Land Size Calculator",
                        onClick = { navController.navigate("first") }
                    )
                }
                item {
                    CalculatorCard(
                        title = "Court Fee Calculator",
                        onClick = { navController.navigate("second") }
                    )
                }
            }
        }
    }
    @Composable
    fun CalculatorCard(title: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(color = Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, color = Color.White, style = MaterialTheme.typography.titleMedium, textAlign = Center)
            }
        }
    }

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
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = guntas,
                        onValueChange = { guntas = it },
                        label = { Text("Gts.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
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

    @Composable
    fun Greeting(numericValue:Double=0.0) {
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

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ToolBunchTheme {
            NavigationGraph()
        }
    }
}