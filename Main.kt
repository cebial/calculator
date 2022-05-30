package calculator

import java.math.BigInteger

object Calculator {
    private fun isNum(string: String) = string.toBigIntegerOrNull() != null
    private fun isVarName(name: String) = name.matches(Regex("[a-zA-Z]+"))

    // return a list with numbers and single character operations only
    private fun formatExpression(s: String): String =
        s.replace("--", "+").replace(Regex("\\++"), "+").replace("+-", "-")

    // all supported operations as functions
    private val applyOperator = mapOf(
        "+" to { a: BigInteger, b: BigInteger -> a + b },
        "-" to { a: BigInteger, b: BigInteger -> a - b },
        "*" to { a: BigInteger, b: BigInteger -> a * b },
        "/" to { a: BigInteger, b: BigInteger -> a / b },
    )

    // a map to store all variables entered by the user
    private val vars = mutableMapOf<String, String>()

    private fun performCalculus(s: String) {
        // check if this is a valid expression (parentheses aren't handled here)
        if (!s.matches(Regex("\\(*[+-]?[0-9a-zA-Z]+(\\s([+-]*|[*/])\\s\\(*[+-]?[0-9a-zA-Z]+\\)*)*"))) {
            println("Invalid Expression"); return
        }

        // create the postfix expression (or exit here if parentheses are unbalanced)
        val postfix = convertInfixToPostfix(s)
        if (postfix.size == 0 ) {
            println("Invalid Expression"); return
        }

        // perform the calculus using the postfix expression
        val stack = mutableListOf<String>()
        for (op in postfix) {
            when {
                isNum(op) -> stack += op
                isVarName(op) -> stack += vars[op]!!
                else -> {
                    val a = stack.removeLast().toBigInteger()
                    val b = stack.removeLast().toBigInteger()
                    stack.add(applyOperator[op]!!(b, a).toString())
                }
            }
        }

        // last item on the stack is the end result
        println(stack[0])
    }

    private fun performAssignment(s: String) {
        val parts = s.split(Regex("\\s*=\\s*"))

        when {
            !isVarName(parts[0]) -> println("Invalid identifier")
            (!isNum(parts[1]) && !isVarName(parts[1])) || parts.size > 2  -> println("Invalid assignment")
            !isNum(parts[1]) && parts[1] !in vars -> println("Unknown variable")
            isNum(parts[1]) -> vars[parts[0]] = parts[1]
            else -> vars[parts[0]] = vars[parts[1]]!!
        }
    }

    private fun hasPrecedence(op: String, op2: String) = (op in "*/" && op2 !in "*/") || (op in "+-" && op2 == "(")

    private fun convertInfixToPostfix(s: String) : MutableList<String> {
        val stack = mutableListOf<String>()
        val postfix = mutableListOf<String>()

        val infix = formatExpression(s).replace(Regex("\\s*([+*/()]|-(?!\\d))\\s*")," $1 ")
            .trim().split(Regex("\\s+"))

        scanner@ for (op in infix) {
            when {
                // if the scanned character is an operand, append it to the postfix string
                isNum(op) || isVarName(op) -> postfix += op

                // left parentheses are always pushed onto the stack
                op == "(" -> stack += op

                // right parenthesis encountered? pop an operator of the stack and copy to the output, repeat this
                // until the top of the stack is a left parenthesis, then discard both parentheses
                op == ")" -> {
                    while (stack.size > 0) {
                        val op2 = stack.removeLast()
                        if (op2 == "(") continue@scanner
                        postfix += op2
                    }

                    // if stack is empty here, parentheses were unbalanced
                    if (stack.size == 0) { postfix.clear(); break@scanner }
                }

                // if a) precedence of the scanned operator is greater than the precedence order of the operator
                // on the stack, or b) the stack is empty or c) the stack contains a ‘(‘ , push it on the stack.
                stack.isEmpty() || stack.last() == "(" || hasPrecedence(op, stack.last()) -> stack += op

                // a) pop all the operators from the stack which are greater than or equal to in precedence than that of
                // the scanned operator. Then b) push the scanned operator to the stack. If we find parentheses while
                // popping, then stop and push the scanned operator on the stack.
                else -> {
                    while (stack.size > 0) {
                        if (hasPrecedence(op, stack.last())) break
                        val op2 = stack.removeLast()
                        if (op2 == "(") break
                        postfix += op2
                    }
                    stack += op
                }
            }
        }

        // at the end of the expression, pop the stack and add all operators to the result.
        while (stack.size > 0) {
            val op2 = stack.removeLast()
            // if we encounter a left parenthesis here, parentheses were unbalanced
            if (op2 == "(") { postfix.clear(); break }
            postfix += op2
        }

        return postfix
    }

    fun processInput(s: String) {
        when {
            s == "/help" -> println("I will do your math homework.")
            s[0] == '/' -> println("Unknown command")
            '=' in s -> performAssignment(s)
            s in vars -> println(vars[s])
            isVarName(s) -> println("Unknown variable")
            else -> performCalculus(s)
        }
    }
}

fun main() {
    while (true) {
        when (val s = readln()) {
            "" -> continue
            "/exit" -> break
            else -> Calculator.processInput(s.trim())
        }
    }
    println("Bye!")
}
