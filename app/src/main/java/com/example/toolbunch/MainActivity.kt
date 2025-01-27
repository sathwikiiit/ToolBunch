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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.toolbunch.ui.theme.ToolBunchTheme

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

                CourtFeeCalculator(
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

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ToolBunchTheme {
            LandSizeCalculator(rememberNavController())
        }
    }
}