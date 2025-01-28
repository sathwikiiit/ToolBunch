package com.example.toolbunch

import kotlin.math.pow

class Eval {

    fun evaluate(expression: String): Double {
        var terms = mutableListOf<String>()
        var operators = mutableListOf<Char>()

        // Tokenization with error checks
        var currentTerm = ""
        expression.forEach { char ->
            if (char.isDigit() || char == '.') {
                currentTerm += char
            } else if (char != ' ') {
                if (currentTerm.isNotEmpty()) {
                    terms.add(currentTerm)
                    currentTerm = ""
                }
                if (char == '+' || char == '-' || char == 'x' || char == '/' || char == '^') {
                    operators.add(char)
                } else {
                    throw IllegalArgumentException("Invalid operator: $char")
                }
            }
        }
        if (currentTerm.isNotEmpty()) {
            terms.add(currentTerm)
        }

        // Basic input validation
        if (terms.size != operators.size + 1) {
            throw IllegalArgumentException("Invalid expression: Mismatch between terms and operators")
        }

        // Evaluation with error checks
        while (operators.isNotEmpty()) {
            val precedenceMap = mapOf('+' to 1, '-' to 1, 'x' to 2, '/' to 2, '^' to 3)
            val highestPrecedence = operators.withIndex().maxByOrNull { precedenceMap[it.value]!! }!!
            val operatorIndex = highestPrecedence.index

            val operand1 = try {
                terms[operatorIndex].toDouble()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid operand: ${terms[operatorIndex]}")
            }

            val operand2 = try {
                terms[operatorIndex + 1].toDouble()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid operand: ${terms[operatorIndex + 1]}")
            }

            val result = when (operators[operatorIndex]) {
                '+' -> operand1 + operand2
                '-' -> operand1 - operand2
                'x' -> operand1 * operand2
                '/' -> {
                    if (operand2 == 0.0) {
                        throw IllegalArgumentException("Division by zero")
                    }
                    operand1 / operand2
                }
                '^' -> operand1.pow(operand2)
                else -> throw IllegalArgumentException("Invalid operator: ${operators[operatorIndex]}")
            }

            terms[operatorIndex] = result.toString()
            terms.removeAt(operatorIndex + 1)
            operators.removeAt(operatorIndex)
        }

        return terms[0].toDouble()
    }
}