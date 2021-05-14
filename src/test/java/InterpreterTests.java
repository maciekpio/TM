import ast.SighNode;
import interpreter.PassthroughException;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.Grammar.rule;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import interpreter.TMInterpreter;
import interpreter.Null;
import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.IO;
import norswap.utils.TestFixture;
import norswap.utils.Util;
import norswap.utils.data.wrappers.Pair;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

public final class InterpreterTests extends TestFixture {

    // TODO peeling

    // ---------------------------------------------------------------------------------------------

    private final TMGrammar grammar = new TMGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    // ---------------------------------------------------------------------------------------------

    private Grammar.rule rule;

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, null);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn, String expectedOutput) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (rule rule, String input, Object expectedReturn, String expectedOutput) {
        // TODO
        // (1) write proper parsing tests
        // (2) write some kind of automated runner, and use it here

        autumnFixture.rule = rule;
        ParseResult parseResult = autumnFixture.success(input);
        SighNode root = Util.cast(parseResult.topValue(), SighNode.class);

        Reactor reactor = new Reactor();
        Walker<SighNode> walker = TMSemantic.createWalker(reactor);
        TMInterpreter interpreter = new TMInterpreter(reactor);
        walker.walk(root);
        reactor.run();
        Set<SemanticError> errors = reactor.errors();
        //List<SemanticError> errors = reactor.allErrors();

        if (!errors.isEmpty()) {
            LineMapString map = new LineMapString("<test>", input);
            String report = reactor.reportErrors(it ->
                it.toString() + " (" + ((SighNode) it).span.startString(map) + ")");
            //            String tree = AttributeTreeFormatter.format(root, reactor,
            //                    new ReflectiveFieldWalker<>(SighNode.class, PRE_VISIT, POST_VISIT));
            //            System.err.println(tree);
            throw new AssertionError(report);
        }

        Pair<String, Object> result = IO.captureStdout(() -> interpreter.interpret(root));
        assertEquals(result.b, expectedReturn);
        if (expectedOutput != null) assertEquals(result.a, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn, String expectedOutput) {
        rule = grammar.root;
        check("main{" + input + "}", expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn) {
        rule = grammar.root;
        check("main{" + input + "}", expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkThrows (String input, Class<? extends Throwable> expected) {
        assertThrows(expected, () -> check(input, null));
    }

    // ---------------------------------------------------------------------------------------------

    private final HashMap<String, Object> point12 = new HashMap<>(){{
        put("x", 1L);
        put("y", 2L);
    }};

    private final HashMap<String, Object> point00 = new HashMap<>(){{
        put("x", 0L);
        put("y", 0L);
    }};

    private final HashMap<String, Object> point1525 = new HashMap<>(){{
        put("x", 1.5d);
        put("y", 2.5d);
    }};


    @Test
    public void testInContext () {
        rule = grammar.root;
        check("struct P{x=aFloat; y=aFloat}\n" +
                "struct P3D{x=aFloat; y=aFloat; z=aFloat}" +
                "\n" +
                "def abs(n){\n" +
                "    if(n>=0){return (n)}\n" +
                "    return (0-n);\n" +
                "}\n" +
                "\n" +
                "def getManDiff(p1, p2) {\n" +
                "   let dX = abs((p1.x - p2.x))\n" +
                "   let dY = abs((p1.y - p2.y))\n" +
                "   return (dX+dY)\n" +
                "}\n" +
                "\n" +
                "main{getManDiff(new P(), new P3D(1.5, 2.5, 0.0))}", 4d);
        check("struct P2D{\n" +
                "    x=aFloat\n" +
                "    y=aFloat\n" +
                "}\n" +
                "\n" +
                "struct P3D{\n" +
                "    x=aFloat\n" +
                "    y=aFloat\n" +
                "    z=aFloat\n" +
                "}\n" +
                "\n" +
                "def abs(n){\n" +
                "    if(n>=0){\n" +
                "        return (n)\n" +
                "    }\n" +
                "    return (0-n)\n" +
                "}\n" +
                "\n" +
                "def getManDiff(p1, p2) {\n" +
                "   let dX = abs((p1.x - p2.x))\n" +
                "   let dY = abs((p1.y - p2.y))\n" +
                "   return (dX+dY)\n" +
                "}\n" +
                "\n" +
                "main{\n" +
                "    getManDiff(new P2D(), new P3D(1.5, 2.5, -2.0))\n" +
                "}", 4d);
        check("def abs(n){\n" +
                "    if(n>=0){return (n)}\n" +
                "    return (0-n);\n" +
                "}\n" +
                "\n" +
                "def getManDiff(p2) {\n" +
                "   let p1 = [0, 0]" +
                "   let dX = abs((p1.get(0) - p2.get(0)))\n" +
                "   let dY = abs((p1.get(1) - p2.get(1)))\n" +
                "   return (dX+dY)\n" +
                "}\n" +
                "let a2 = [1.5, 2.5]"+
                "main{" +
                "   getManDiff(a2)" +
                "}", 4d);
        check("def transferTo(a1, a2) {\n" +
                "   a1.put(0: a2.get(0)) " +
                "   a1.put(1: a2.get(1)) " +
                "}\n" +
                "let arrayTarget = [aFloat, aFloat] " +
                "let arrayFrom = [1.5, 2.5] " +
                "transferTo(arrayTarget, arrayFrom) "+
                "main{" +
                "   print(arrayTarget + aString)" +
                "}", null, "[1.5, 2.5]\n");
        check("def createTab(len){" +
                "   return (arrayOf(len:len))" +
                "}" +
                "main{" +
                "   print(createTab(3))" +
                "}", null, "[3, 3, 3]\n");
        check("def abs(n){\n" +
                "    if(n>=0){return (n)}\n" +
                "    return (0-n);\n" +
                "}\n" +
                "\n" +
                "def getManDiff(p2) {\n" +
                "   let p1 = {\"x\":aFloat, \"y\":aFloat}" +//{"x":0.0, "y":0.0}
                "   let dX = abs((p1.get(\"x\") - p2.get(\"x\")))\n" +
                "   let dY = abs((p1.get(\"y\") - p2.get(\"y\")))\n" +
                "   return (dX+dY)\n" +
                "}\n" +
                "let a2 = {\"x\":1.5, \"y\":2.5}"+
                "main{" +
                "   getManDiff(a2)" +
                "}", 4d);
        check("def transferTo(a1, a2) {\n" +
                "   a1.put(\"x\": a2.get(\"x\")) " +
                "   a1.put(\"y\": a2.get(\"y\")) " +
                "}\n" +
                "let x = \"x\"; let y = \"y\"" +
                "let arrayTarget = {x:aFloat, y:aFloat} " +
                "let arrayFrom = {x:1.5, y:2.5} " +
                "transferTo(arrayTarget, arrayFrom) " +
                "main{" +
                "   arrayTarget" +
                "}", point1525);
        check("def transferWith(a1, a2) {\n" +
                "   a1.put(\"x\": a2.get(\"x\")) " +
                "   a1.put(\"y\": a2.get(\"y\")) " +
                "}\n" +
                "let x = \"x\"; let y = \"y\"" +
                "let arrayTarget = {x:aFloat, y:aFloat} " +
                "let arrayFrom = {x:1.5, y:2.5} " +
                "transferWith(arrayTarget, arrayFrom) " +
                "main{" +
                "   arrayTarget" +
                "}", point1525);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testBuiltIn(){
        rule=grammar.root;
        check("main{ print(\"hello\")}", null, "hello\n");
        check("main{ rprint(\"Arithmetic error\")}", null, "Error: Arithmetic error\n");
        check("main{ parseInt(\"5\")}",  5,null);
        checkThrows("main{ parseInt(\"text\")}",  NumberFormatException.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        checkExpr("42", 42L);
        checkExpr("42.0", 42.0d);
        checkExpr("\"hello\"", "hello");
        checkExpr("(42)", 42L);
        checkExpr("[1, 2, 3]", new Object[]{1L, 2L, 3L});
        checkExpr("true", true);
        checkExpr("false", false);
        checkExpr("!false", true);
        checkExpr("!true", false);
        checkExpr("!!true", true);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testNumericBinary () {
        checkExpr("1 + 2", 3L);
        checkExpr("2 - 1", 1L);
        checkExpr("2 * 3", 6L);
        checkExpr("2 / 3", 0L);
        checkExpr("3 / 2", 1L);
        checkExpr("2 % 3", 2L);
        checkExpr("3 % 2", 1L);

        checkExpr("1.0 + 2.0", 3.0d);
        checkExpr("2.0 - 1.0", 1.0d);
        checkExpr("2.0 * 3.0", 6.0d);
        checkExpr("2.0 / 3.0", 2d / 3d);
        checkExpr("3.0 / 2.0", 3d / 2d);
        checkExpr("2.0 % 3.0", 2.0d);
        checkExpr("3.0 % 2.0", 1.0d);

        checkExpr("1 + 2.0", 3.0d);
        checkExpr("2 - 1.0", 1.0d);
        checkExpr("2 * 3.0", 6.0d);
        checkExpr("2 / 3.0", 2d / 3d);
        checkExpr("3 / 2.0", 3d / 2d);
        checkExpr("2 % 3.0", 2.0d);
        checkExpr("3 % 2.0", 1.0d);

        checkExpr("1.0 + 2", 3.0d);
        checkExpr("2.0 - 1", 1.0d);
        checkExpr("2.0 * 3", 6.0d);
        checkExpr("2.0 / 3", 2d / 3d);
        checkExpr("3.0 / 2", 3d / 2d);
        checkExpr("2.0 % 3", 2.0d);
        checkExpr("3.0 % 2", 1.0d);

        checkExpr("2**3",8L);
        checkExpr("0.5**2",0.25d);
        checkExpr("2**0.5",1.4142135623730951d);
        checkExpr("2.5**0.5",1.5811388300841898d);


        checkExpr("2 * (4-1) * 4.0 / 6 % (2+1)", 1.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testOtherBinary () {
        checkExpr("true  && true",  true);
        checkExpr("true  || true",  true);
        checkExpr("true  || false", true);
        checkExpr("false || true",  true);
        checkExpr("false && true",  false);
        checkExpr("true  && false", false);
        checkExpr("false && false", false);
        checkExpr("false || false", false);

        checkExpr("1 + \"a\"", "1a");
        checkExpr("\"a\" + 1", "a1");
        checkExpr("\"a\" + true", "atrue");

        checkExpr("1 == 1", true);
        checkExpr("1 == 2", false);
        checkExpr("1.0 == 1.0", true);
        checkExpr("1.0 == 2.0", false);
        checkExpr("true == true", true);
        checkExpr("false == false", true);
        checkExpr("true == false", false);
        checkExpr("1 == 1.0", true);
        checkExpr("[1] == [1]", true);
        checkExpr("[1, 2, 3] == [1, 2, 3]", true);
        checkExpr("arrayOf(0:3) == arrayOf(0:3)", true);

        checkExpr("1 != 1", false);
        checkExpr("1 != 2", true);
        checkExpr("1.0 != 1.0", false);
        checkExpr("1.0 != 2.0", true);
        checkExpr("true != true", false);
        checkExpr("false != false", false);
        checkExpr("true != false", true);
        checkExpr("1 != 1.0", false);

        checkExpr("\"hi\" != \"hi2\"", true);
        checkExpr("[1.0] != [1]", true);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testVarDecl () {
        rule = grammar.root;
        check("let x = 1; main{x}", 1L);
        check("let x = 2.0; main{x}", 2d);

        check("let x = 0; main{x = 3}", 3L);
        check("let x = \"0\"; main{x = \"S\"}", "S");

        // implicit conversions
        check("let x = 1.2; x = 2; main{x}", 2.0d);
        checkThrows("pinned x = anInt; x = 1", AssertionError.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testRootAndBlock () {
        rule = grammar.root;
        check("main{}", null);
        check("main{1}", 1L);
        check("main{1}; main{2}", 1L);

        check("print(\"a\")", null, "a\n");
        check("print(\"a\" + 1)", null, "a1\n");
        check("print(\"a\"); print(\"b\")", null, "a\nb\n");

        check("print(\"a\"); print(\"b\")", null, "a\nb\n");

        check(
            "let x = 1;" +
                   "print(\"\" + x);" +
                   "let x = 2;" +
                   "print(\"\" + x)",
            null, "1\n2\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testCalls () {
        rule = grammar.root;

        check(
                "def add (a, b) { return (a + b) } " +
                        "main{add(7, 3)}",
                10L);

        check(
            "def add (a, b) { return (a + b) } " +
                "main{add(7, 3)}",
            10L);

        check(
                "def equals (a, b) { return (a == b) } " +
                        "main{equals(\"text\", \"text\")}",
                true);

        check("struct Point {x = anInt; y = anInt }" +
                "def setX (x) { return (new Point(x, 2)) }" +
                "main{setX(1)}", point12);

        check(
            "struct Point {x = anInt; y = anInt }" +
                "main{new Point(1, 2)}",
                point12);

        check("let str = \"null\"; main{print(str + 1)}", null, "null1\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testArrayStructAccess () {
        checkExpr("[1].get(0)", 1L);
        checkExpr("[1.0].get(0)", 1d);
        checkExpr("[1, 2].get(1)", 2L);
        checkExpr("[1].length", 1L);
        checkExpr("[1, 2].length", 2L);

        check("let x = [0]; x.put(0: 3); main{x.get(0)}", 3L);
        checkThrows("let x = [0]; x.put(1: 3); main{x.get(1)}",
            ArrayIndexOutOfBoundsException.class);

        check(
            "struct P {x = anInt; y = anInt}" +
                "main {new P(1, 2).y}",
            2L);

        check(
            "struct P {x = anInt; y = anInt}" +
                "let p = new P(1, 2);" +
                "p.y = 42;" +
                "main{p.y}",
            42L);

        check("struct P{x=anInt; y=anInt};" +
                        " main{new P()}",
                point00);

        check("let tab = arrayOf(false:2)" +
                "main{tab == [false, false]}", true);
        check("let tab = arrayOf(false:2)" +
                "main{tab.length}", 2L);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIfWhile () {
        rule = grammar.root;
        check("if (true) {return (1)} else {return (2)}", 1L);
        check("if (false) {return (1)} else {let x = 2; return (x)}", 2L);
        check("if (false) {return (1)} else{ if (true) {return (2)} else {return (3)}} ", 2L);
        check("if (false) {return (1)} else{ if (false) {return (2)} else {return (3)}} ", 3L);

        check("let i = 0; while (i < 3) { print(\"\" + i); i = i + 1 } ", null, "0\n1\n2\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testTypeAsValues () {
        rule = grammar.root;
        check("struct S{attr = anInt} ; main{(\"\"+ S)}", "S");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnconditionalReturn()
    {
        rule = grammar.root;
        check("def f() { if(true) {return (1)} else {return (2)} }; main{f()}", 1L);
    }

    // ---------------------------------------------------------------------------------------------

    // NOTE(tm): INCREDIBLY complete, cover all features of the world.
}
