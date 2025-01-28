package org.codingambitions.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        CalculatorScreen()
    }
}

@Composable
fun CalculatorScreen() {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var result by remember { mutableStateOf("") }
    var lastResultUsed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Editable input display
        BasicTextField(
            value = input,
            onValueChange = {
                input = it
                lastResultUsed = false
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.End
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Result display
        Text(
            text = result,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Calculator buttons
        CalculatorButtons(
            onButtonClick = { button ->
                when (button) {
                    "=" -> {
                        result = calculateResult(input.text)
                        input = TextFieldValue(input.text) // Keep the expression in input
                        lastResultUsed = true
                    }
                    "C" -> {
                        input = TextFieldValue("")
                        result = ""
                        lastResultUsed = false
                    }
                    "\u232B" -> { // Backspace character
                        if (input.text.isNotEmpty()) input = TextFieldValue(input.text.dropLast(1))
                    }
                    "%" -> {
                        if (input.text.isNotEmpty()) {
                            val currentValue = input.text.toDoubleOrNull()
                            if (currentValue != null) {
                                input = TextFieldValue((currentValue / 100).toString())
                            }
                        }
                    }
                    else -> {
                        if (lastResultUsed && button !in listOf("+", "-", "*", "/")) {
                            input = TextFieldValue(button)
                        } else {
                            input = TextFieldValue(input.text + button)
                        }
                        lastResultUsed = false
                    }
                }
            }
        )
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {
    val buttons = listOf(
        listOf("C", "%", "\u232B", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        for (row in buttons) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (button in row) {
                    Button(
                        onClick = { onButtonClick(button) },
                        modifier = Modifier
                            .size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (button in listOf("+", "-", "*", "/", "=")) Color(0xFFBB86FC) else Color(0xFF1F1F1F),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = button,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun calculateResult(input: String): String {
    return try {
        val result = eval(input)
        if (result == result.toInt().toDouble()) {
            result.toInt().toString() // Remove decimal if result is an integer
        } else {
            result.toString()
        }
    } catch (e: Exception) {
        "Error"
    }
}

fun eval(expression: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expression.length) expression[pos].code else -1
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat.code) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expression.length) throw RuntimeException("Unexpected: ${expression[pos]}")
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                x = expression.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: ${ch.toChar()}")
            }

            return x
        }
    }.parse()
}
