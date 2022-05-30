package calculator

object Calculator {
    private fun isNum(string: String) = string.toIntOrNull() != null
    private fun isVarName(name: String) = name.matches(Regex("[a-zA-Z]+"))

    // return a list with numbers and single character operations only
    private fun formatExpression(s: String): String =
        s.replace("--", "+").replace(Regex("\\++"), "+").replace("+-", "-")

    // all supported operations as functions
    private val applyOperator = mapOf(
        "+" to { a: Int, b: Int -> a + b },
        "-" to { a: Int, b: Int -> a - b },
        "*" to { a: Int, b: Int -> a * b },
        "/" to { a: Int, b: Int -> a / b },
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
        val stack = mutableListOf<Int>()
        for (op in postfix) {
            when {
                isNum(op) -> stack += op.toInt()
                isVarName(op) -> stack += vars[op]!!.toInt()
                else -> {
                    val a = stack.removeLast()
                    val b = stack.removeLast()
                    stack.add(applyOperator[op]!!(b, a))
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
            // if the scanned character is an operand, append it to Postfix string
            if (isNum(op) || isVarName(op)) {
                postfix += op
                continue
            }

            // left parentheses are always pushed onto a stack
            if (op == "(") {
                stack += op
                continue
            }

            // when a right parenthesis is encountered, the symbol on the top of the stack is popped off the stack and
            // copied to the output. This process repeats until the top of the stack is a left parenthesis. When that
            // occurs, both parentheses are discarded.
            if (op == ")") {
                while (stack.size > 0) {
                    val op2 = stack.removeLast()
                    if (op2 == "(") continue@scanner
                    postfix += op2
                }

                // if stack is empty here, parentheses were unbalanced
                if (stack.size == 0) {
                    postfix.clear()
                    break
                }
            }

            // a) if the precedence order of the scanned operator is greater than the precedence order of the operator
            // on the stack, or b) the stack is empty or c) the stack contains a ‘(‘ , push it on stack.
            if (stack.isEmpty() || stack.last() == "(" || hasPrecedence(op, stack.last())) {
                stack += op
                continue
            }

            // a) pop all the operators from the stack which are greater than or equal to in precedence than that of the
            // scanned operator. After doing that b) push the scanned operator to the stack. (If you encounter
            // parentheses while popping, then stop there and push the scanned operator in the stack.)
            while (stack.size > 0) {
                if (hasPrecedence(op, stack.last())) break
                val op2 = stack.removeLast()
                if (op2 == "(") break
                postfix += op2
            }
            stack += op
        }

        // at the end of the expression, pop the stack and add all operators to the result.
        while (stack.size > 0) {
            val op2 = stack.removeLast()
            // if we encounter a left parenthesis here, parentheses were unbalanced
            if (op2 == "(") {
                postfix.clear()
                break
            }
            postfix += op2
        }

        return postfix
    }

    fun processInput(s: String) {
        if (s[0] == '/') { println("Unknown command"); return }

        when {
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
//        when (val s = "((1 + 2) + 3) + 4") {
            "" -> continue
            "/exit" -> break
            "/help" -> println("I will do your math homework.")
            else -> Calculator.processInput(s.trim())
//            else -> { Calculator.processInput(s.trim()); return }
        }
    }
    println("Bye!")
}
