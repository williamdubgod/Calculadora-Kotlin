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

        // Configuração dos botões numéricos
        setupNumberButtons()

        // Configuração dos botões de operação
        setupOperationButtons()

        // Configuração dos outros botões (backspace, limpar, inverter sinal)
        setupOtherButtons()

        // Configuração do botão de igual
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

        // Adiciona um listener para cada botão numérico
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

        // Adiciona um listener para cada botão de operação
        operationButtons.forEach { button ->
            button.setOnClickListener {
                replaceLastOperation(button.text.toString())
            }
        }
    }

    private fun setupOtherButtons() {
        // Configuração do botão de backspace
        binding.backspace.setOnClickListener {
            binding.txtOperation.text = binding.txtOperation.text.dropLast(1)
        }

        // Configuração do botão de limpar
        binding.clean.setOnClickListener {
            binding.txtOperation.text = ""
            binding.txtResult.text = ""
        }

        // Configuração do botão de inverter sinal
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
        // Configuração do botão de igual
        binding.equals.setOnClickListener {
            showResult()
        }
    }

    private fun addExpression(value: String) {
        // Adiciona o valor do botão ao texto da operação
        binding.txtOperation.append(value)
    }

    private fun replaceLastOperation(newOperator: String) {
        val currentText = binding.txtOperation.text.toString()

        // Verifica se o último caractere no texto da operação é um operador
        if (currentText.isNotEmpty() && currentText.last().toString() in listOf("+", "-", "*", "/", "%")) {
            // Se sim, substitui o último operador pelo novo
            binding.txtOperation.text = currentText.dropLast(1) + newOperator
        } else {
            // Se não, apenas adiciona o novo operador
            addExpression(newOperator)
        }
    }

    private fun showResult() {
        // Exibe o resultado da expressão inserida
        val expression = binding.txtOperation.text.toString()

        try {
            val result = evaluateExpression(expression)
            binding.txtResult.text = result.toString()
        } catch (e: Exception) {
            binding.txtResult.text = "Erro"
        }
    }

    private fun evaluateExpression(expression: String): Double {
        // Avalia a expressão matemática e retorna o resultado
        val tokens = tokenizeExpression(expression)
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<String>()

        for (token in tokens) {
            if (token.isNumber()) {
                numbers.add(token.toDouble())
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
        // Divide a expressão em tokens
        val regex = "(?<=[-+*/%])|(?=[-+*/%])".toRegex()
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
                    // Se o '-' for o primeiro caractere ou se for precedido por um operador
                    currentToken += char
                } else {
                    tokens.add(char.toString())
                }
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken)
        }

        return tokens.filter { it.isNotBlank() }
    }

    private fun String.isNumber(): Boolean {
        // Verifica se uma string representa um número
        return matches("-?\\d+(\\.\\d+)?".toRegex())
    }

    private fun hasPrecedence(op1: String, op2: String): Boolean {
        // Verifica se o operador 1 tem precedência sobre o operador 2
        return !(op1 == "+" || op1 == "-") || (op2 == "*" || op2 == "/")
    }

    private fun applyOperation(left: Double, right: Double, operator: String): Double {
        // Aplica a operação matemática especificada
        return when (operator) {
            "+" -> addValues(left, right)
            "-" -> subtractValues(left, right)
            "*" -> multiplyValues(left, right)
            "/" -> divideValues(left, right)
            "%" -> calculatePercentage(left, right)
            else -> throw IllegalArgumentException("Operador inválido: $operator")
        }
    }

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