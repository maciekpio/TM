import ast.StringLiteralNode;
import org.testng.annotations.Test;
import norswap.autumn.AutumnTestFixture;

public class GrammarTests extends AutumnTestFixture {

    TMGrammar arithmeticParser = new TMGrammar();

    @Test
    public void testSum() {
        this.rule = arithmeticParser.add_expr;
        success("42 + 42");
    }

    @Test
    public void testInteger() {
        this.rule = arithmeticParser.integer;
        // This is a simple test to see if it parses
        success("42");
    }

    @Test
    public void testExpression() {
        this.rule = arithmeticParser.expression_choice;
        // This is a simple test to see if it parses
        success("42 + 42");
    }

    @Test
    public void testRoot() {
        this.rule = arithmeticParser.root;
        success("let a = 42");
        success("array matrix[2]");
        success("matrix.get(1)");
        success("matrix.put(1, 12)");
    }

    String ifTest1 = "if (i%15 == 0){\n" +
            "print(\"FizzBuzz\")" +
            "} else {if (i % 3 == 0){\n" +
            "print(\"Fizz\")}" +
            "}";

    @Test
    public void testIf() {
        this.rule = arithmeticParser.if_stmt;
        // This is a simple test to see if it parses
        success("if (true) {return (1)} \n else {return (2)}");
        success(ifTest1);
    }

    @Test
    public void testTrue() {
        this.rule = arithmeticParser.and_expression;
        success("true && false");
    }

    String fib = "def fib(a, b, N){" +
            "if (N==0){" +
            "return ()" +
            "}" +
            " print(a)" +
            " return (fib(b, a+b, N-1))" +
            "}";

    String fizzbuzz =
            "def fizzbuzz(args) {\n" +
                "let i = 1\n" +
                "while (i <= 100) {\n" +
                    "if (i%15 == 0){\n" +
                        "print(\"FizzBuzz\")" +
                    "} else {if (i % 3 == 0){\n" +
                        "print(\"Fizz\")" +
                    "}\n" +
                    "else{ if (i % 5 == 0){\n" +
                        "print(\"Buzz\")" +
                    "}\n" +
                    "else {\n" +
                        "print(i)" +
                    "}}}\n" +
                    "i = i + 1\n" +
                "}\n" +
            "return ()}";

    String prime =
                    "def isPrime(number) {\n" +
                    "    if (number <= 1) {return (false)}\n" +
                    "    prime = true\n" +
                    "    i = 2\n" +
                    "    while (i < number && prime) {\n" +
                    "        if (number%i == 0) {prime = false}\n" +
                    "    i = i + 1\n" +
                    "    }\n" +
                    "    return (prime)\n" +
                    "}\n" +
                    "\n" +
                    "def main (args) {\n" +
                    "    N = Integer.parseInt(args.get(0))\n" +
                    "    current = 2\n" +
                    "    count = 0\n" +
                    "    while (count < N) {\n" +
                    "        if (isPrime(current)) {\n" +
                    "        print(current)\n" +
                    "        count = count + 1\n" +
                    "        }\n" +
                    "    current = current + 1\n" +
                    "    }\n" +
                    "return ()}";

    String sort =
                    "def swap(a, i, j) {\n" +
                    "    tmp = a.get(i)\n" +
                    "    a.put(a.get(j), i)\n" +
                    "    a.put(tmp, j)\n" +
                    "    return ()\n" +
                    "}\n" +
                    "\n" +
                    "def sort(numbers) {\n" +
                    "    i = 0\n" +
                    "    while (i < length(numbers)) {\n" +
                    "        j = i+1\n" +
                    "        while (j < numbers.length) {\n" +
                    "            if (numbers.get(i) > numbers.get(j)){\n" +
                    "                swap(numbers, i, j)\n" +
                    "                j = j + 1\n" +
                    "            }\n" +
                    "        i = i + 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "    return ()\n" +
                    "}\n" +
                    "\n" +
                    "def main(args) {\n" +
                    "    array numbers [length(args)]\n" +
                    "    i = 0\n" +
                    "    while (i < args.length) {\n" +
                    "        numbers.put(Integer.parseInt(args.get(i)), i)\n" +
                    "        i = i + 1\n" +
                    "    }\n" +
                    "    sort(numbers)\n" +
                    "    i = 0\n" +
                    "    while (i < numbers.length) {\n" +
                    "        print(numbers.get(i))\n" +
                    "        i = i + 1\n" +
                    "    }\n" +
                    "    return ()\n" +
                    "}";

    @Test
    public void testFib() {
        this.rule = arithmeticParser.root;
        success(fib);
        success(fizzbuzz);
        success(prime);
        success(sort);
    }
}
