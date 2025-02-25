package com.example.toolbunch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun Calculator(navController: NavHostController= rememberNavController(),sharedViewModel: CalculatorViewModel) {
    val numericValue = sharedViewModel.marketValue
    var expression by remember { mutableStateOf("") }
    if (expression.isEmpty() && numericValue != 0.0) {
        expression = numericValue.toString()
    }
    var result by remember { mutableDoubleStateOf(numericValue) }
    var memory by remember { mutableDoubleStateOf(numericValue) } // Memory to store the numeric value

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the current expression
        Text(
            text = "Exp: $expression",
            modifier = Modifier.padding(8.dp).fillMaxWidth(0.9f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Left
        )

        // Display the result
        Text(
            text = "Result: ${result.toBigDecimal().setScale(2, java.math.RoundingMode.HALF_EVEN)}",
            modifier = Modifier.padding(8.dp).fillMaxWidth(0.9f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            textAlign = TextAlign.Left
        )

        // Input field for the expression
        BasicTextField(
            value = expression,
            onValueChange = { expression = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, textAlign = TextAlign.End)
        )

        // Grid of calculator buttons
        Column(modifier = Modifier.padding(top = 16.dp)) {
            val buttons = listOf(
                "7", "8", "9", "<", // Backspace button
                "4", "5", "6", "C",
                "1", "2", "3", "x", // Multiplication operator replaced with 'x'
                "+", "0", "-", "/",
                ".", "00", "^", "="
            )

            buttons.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { label ->
                        Button(
                            onClick = {
                                when (label) {
                                    "=" -> {
                                        result = Eval().evaluate(expression)
                                    }
                                    "C" -> {
                                        expression = memory.toString() // Restore memory value to expression
                                        result = 0.0 // Reset result
                                    }
                                    "<" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1) // Remove the last character
                                        }
                                    }
                                    else -> {
                                        // Handle number and operator input
                                        handleInput(label, expression) { newExpression ->
                                            expression = newExpression
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).padding(4.dp).height(60.dp)
                        ) {
                            Text(label, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            onClick = {
                sharedViewModel.marketValue = result
                navController.navigate("cFee")
            }) {
            Text("Go to CourtFeeCalculator")
        }
    }
}

// Function to handle input based on the current expression
fun handleInput(input: String, currentExpression: String, onUpdate: (String) -> Unit) {
    if (input == "." && currentExpression.isNotEmpty() && currentExpression.last().isDigit()) {
        // Allow decimal point only if the last character is a digit
        val lastTerm = currentExpression.splitOperators().lastOrNull() ?: ""
        if (!lastTerm.contains(".")) {
            onUpdate(currentExpression + input)
        }
    } else if (isOperator(input)) {
        // Prevent continuous operators
        if (currentExpression.isNotEmpty() && !isOperator(currentExpression.last().toString())) {
            onUpdate(currentExpression + input)
        }
    } else {
        // Append number
        onUpdate(currentExpression + input)
    }
}

// Function to check if a character is an operator
fun isOperator(char: String): Boolean {
    return char in listOf("+", "-", "x", "/", "^") // Updated to include 'x' for multiplication
}

// Function to split the expression into terms based on operators
fun String.splitOperators(): List<String> {
    return this.split("+", "-", "x", "/", "^").filter { it.isNotEmpty() } // Updated to include 'x'
}

