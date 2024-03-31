package com.vinicius.calculadoracp1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vinicius.calculadoracp1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNumberButtons()

        setupOperationButtons()

        setupOtherButtons()

        setupEqualsButton()
    }

    private fun setupNumberButtons() {
        val numberButtons = listOf(
            binding.numberZero,
            binding.numberOne,
            binding.numberTwo,
            binding.numberTree,
            binding.numberFour,
            binding.numberFive,
            binding.numberSix,
            binding.numberSeven,
            binding.numberEight,
            binding.numberNine,
            binding.dot
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                addExpression(button.text.toString())
            }
        }
    }

    private fun setupOperationButtons() {
        val operationButtons = listOf(
            binding.plus,
            binding.subtraction,
            binding.multiplication,
            binding.division,
            binding.percentage
        )

        operationButtons.forEach { button ->
            button.setOnClickListener {
                replaceLastOperation(button.text.toString())
            }
        }
    }

    private fun setupOtherButtons() {
        binding.backspace.setOnClickListener {
            binding.txtOperation.text = binding.txtOperation.text.dropLast(1)
        }

        binding.clean.setOnClickListener {
            binding.txtOperation.text = ""
            binding.txtResult.text = ""
        }

        binding.invertSignal.setOnClickListener {
            val currentText = binding.txtOperation.text.toString()
            binding.txtOperation.text = if (currentText.startsWith('-')) {
                currentText.substring(1) // Remove o sinal negativo
            } else {
                "-$currentText" // Adiciona um sinal negativo
            }
        }
    }

    private fun setupEqualsButton() {
        binding.equals.setOnClickListener {
            showResult()
        }
    }

    private fun addExpression(value: String) {
        binding.txtOperation.append(value)
    }

    private fun replaceLastOperation(newOperator: String) {
        val currentText = binding.txtOperation.text.toString()

        if (currentText.isNotEmpty() && currentText.last().toString() in listOf("+", "-", "*", "/", "%")) {
            val updatedText = String.format(getString(R.string.operation_format), currentText.dropLast(1), newOperator)
            binding.txtOperation.text = updatedText
        } else {
            addExpression(newOperator)
        }
    }

    private fun showResult() {
        val expression = binding.txtOperation.text.toString()

        try {
            val result = evaluateExpression(expression)
            binding.txtResult.text = result.toString()
        } catch (e: Exception) {
            binding.txtResult.text = R.string.error_message.toString()
        }
    }

    private fun evaluateExpression(expression: String): Double {
        val tokens = tokenizeExpression(expression)
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        for (token in tokens) {
            if (token.isNumber()) {
                numbers.add(token.toDouble())
            } else if (token == "%") {
                val value = numbers.removeLast()
                val percent = value / 100.0
                numbers.add(percent)
            } else {
                while (operators.isNotEmpty() && hasPrecedence(token, operators.last())) {
                    val operator = operators.removeLast()
                    val right = numbers.removeLast()
                    val left = numbers.removeLast()
                    numbers.add(applyOperation(left, right, operator))
                }
                operators.add(token)
            }
        }

        while (operators.isNotEmpty()) {
            val operator = operators.removeLast()
            val right = numbers.removeLast()
            val left = numbers.removeLast()
            numbers.add(applyOperation(left, right, operator))
        }

        return numbers.first()
    }


    private fun tokenizeExpression(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = ""

        for (char in expression) {
            if (char.isDigit() || char == '.') {
                currentToken += char
            } else {
                if (currentToken.isNotEmpty()) {
                    tokens.add(currentToken)
                    currentToken = ""
                }
                if (char == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "%"))) {
                    currentToken += char
                } else {
                    tokens.add(char.toString())
                }
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken)
        }

        val groupedTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "%") {
                if (i > 0 && tokens[i - 1].isNumber()) {
                    groupedTokens.removeAt(groupedTokens.size - 1) // Remove o número anterior
                    val number = tokens[i - 1].toDouble()
                    groupedTokens.add("${calculatePercentage(number, 100.0)}")
                } else {
                    groupedTokens.add("0")
                }
            } else {
                groupedTokens.add(tokens[i])
            }
            i++
        }

        return groupedTokens.filter { it.isNotBlank() }
    }


    private fun String.isNumber(): Boolean {
        return matches("-?\\d+(\\.\\d+)?".toRegex())
    }

    private fun hasPrecedence(op1: String, op2: String): Boolean {
        return !(op1 == "+" || op1 == "-") || (op2 == "*" || op2 == "/")
    }

    private fun applyOperation(left: Double, right: Double, operator: String): Double {
        return when (operator) {
            "+" -> addValues(left, right)
            "-" -> subtractValues(left, right)
            "*" -> multiplyValues(left, right)
            "/" -> divideValues(left, right)
            "%" -> calculatePercentage(left, right)
            else -> throw IllegalArgumentException("Operador inválido: $operator")
        }
    }

    // ------------------ FUNÇÕES MATEMATICAS ------------------ //

    private fun addValues(firstValue: Double, secondValue: Double): Double {
        return firstValue + secondValue
    }

    private fun subtractValues(firstValue: Double, secondValue: Double): Double {
        return firstValue - secondValue
    }

    private fun multiplyValues(firstValue: Double, secondValue: Double): Double {
        return firstValue * secondValue
    }

    private fun divideValues(firstValue: Double, secondValue: Double): Double {
        if (secondValue == 0.0) {
            throw ArithmeticException("Divisão por zero")
        }
        return firstValue / secondValue
    }

    private fun calculatePercentage(value: Double, percentage: Double): Double {
        return value * (percentage / 100.0)
    }
}