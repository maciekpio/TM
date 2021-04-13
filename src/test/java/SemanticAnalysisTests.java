import norswap.autumn.AutumnTestFixture;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import ast.SighNode;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;

/**
 * NOTE(norswap): These tests were derived from the {@link TMInterpreterTests} and don't test anything
 * more, but show how to idiomatically test semantic analysis. using {@link UraniumTestFixture}.
 */
public final class SemanticAnalysisTests extends UraniumTestFixture
{
    // ---------------------------------------------------------------------------------------------

    private final TMGrammar grammar = new TMGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.rule = grammar.root();
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    private String input;

    @Override protected Object parse (String input) {
        this.input = input;
        return autumnFixture.success(input).topValue();
    }

    @Override protected String astNodeToString (Object ast) {
        LineMapString map = new LineMapString("<test>", input);
        return ast.toString() + " (" + ((SighNode) ast).span.startString(map) + ")";
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void configureSemanticAnalysis (Reactor reactor, Object ast) {
        Walker<SighNode> walker = TMSemantic.createWalker(reactor);
        walker.walk(((SighNode) ast));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testLiteralsAndUnary() {
        successInput("42");
        successInput("42.0");
        successInput("\"hello\"");
        successInput("true");
        successInput("false");
        successInput("!!true");
        successInput("!!false");
        successInput("!!!!true");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testNumericBinary() {
        successInput("1 + 2");
        successInput("2 - 1");
        successInput("2 * 3");
        successInput("2 / 3");
        successInput("3 / 2");
        successInput("2 % 3");
        successInput("3 % 2");

        successInput("1.0 + 2.0");
        successInput("2.0 - 1.0");
        successInput("2.0 * 3.0");
        successInput("2.0 / 3.0");
        successInput("3.0 / 2.0");
        successInput("2.0 % 3.0");
        successInput("3.0 % 2.0");

        successInput("1 + 2.0");
        successInput("2 - 1.0");
        successInput("2 * 3.0");
        successInput("2 / 3.0");
        successInput("3 / 2.0");
        successInput("2 % 3.0");
        successInput("3 % 2.0");

        successInput("1.0 + 2");
        successInput("2.0 - 1");
        successInput("2.0 * 3");
        successInput("2.0 / 3");
        successInput("3.0 / 2");
        successInput("2.0 % 3");
        successInput("3.0 % 2");

        failureInputWith("2 + true", "Trying to plus int with Bool");
        failureInputWith("true + 2", "Trying to plus Bool with Int");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testOtherBinary() {
        successInput("true && false");
        successInput("false && true");
        successInput("true && true");
        successInput("true || false");
        successInput("false || true");
        successInput("false || false");

        failureInputWith("false || 1",
                "Attempting to perform binary logic on non-boolean type: Int");
        failureInputWith("2 || true",
                "Attempting to perform binary logic on non-boolean type: Int");

        successInput("1 + \"a\"");
        successInput("\"a\" + 1");
        successInput("\"a\" + true");

        successInput("1 == 1");
        successInput("1 == 2");
        successInput("1.0 == 1.0");
        successInput("1.0 == 2.0");
        successInput("true == true");
        successInput("false == false");
        successInput("true == false");
        successInput("1 == 1.0");

        failureInputWith("true == 1", "Trying to compare incomparable types Bool and Int");
        failureInputWith("2 == false", "Trying to compare incomparable types Int and Bool");

        successInput("\"hi\" == \"hi\"");

        successInput("1 != 1");
        successInput("1 != 2");
        successInput("1.0 != 1.0");
        successInput("1.0 != 2.0");
        successInput("true != true");
        successInput("false != false");
        successInput("true != false");
        successInput("1 != 1.0");

        failureInputWith("true != 1", "Trying to compare incomparable types Bool and Int");
        failureInputWith("2 != false", "Trying to compare incomparable types Int and Bool");

        successInput("\"hi\" != \"hi\"");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testLetDecl() {
        successInput("let x=1");
        successInput("let x=2.0");
        successInput("let x = 0 ; x+1");

        successInput("let x=0 ; x=3");
        successInput("let x = \"0\"; x = \"S\"");
        System.out.println("HERE");
        successInput("let x = 2.0; let x = true;");
        successInput("let x = true; let y = !!x; y = true");

        failureInputWith("let x = 2.0; x = true;", "Trying to assign a value to a non-compatible lvalue");
        failureInputWith("let x = true ; x=1", "Trying to assign a value to a non-compatible lvalue");
        failureInputWith("x + 1; let x = 2", "variable used before declaration: x");

        // implicit conversions
        //successInput("var x: Float = 1 ; x = 2");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testArrayStructAccess() {
        successInput("array matrix [10] ; matrix.put(1, 0)");

        successInput("return [1.0][0]");
        successInput("return [1, 2][1]");

        failureInputWith("return [1][true]", "Indexing an array using a non-Int-valued expression");

        // TODO make this legal?
        // successInput("[].length", 0L);

        successInput("return [1].length");
        successInput("return [1, 2].length");

        successInput("var array: Int[] = null; return array[0]");
        successInput("var array: Int[] = null; return array.length");

        successInput("var x: Int[] = [0, 1]; x[0] = 3; return x[0]");
        successInput("var x: Int[] = []; x[0] = 3; return x[0]");
        successInput("var x: Int[] = null; x[0] = 3");

        successInput(
                "struct P { var x: Int; var y: Int }" +
                        "return $P(1, 2).y");

        successInput(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = null;" +
                        "return p.y");

        successInput(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = $P(1, 2);" +
                        "p.y = 42;" +
                        "return p.y");

        successInput(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = null;" +
                        "p.y = 42");

        failureInputWith(
                "struct P { var x: Int; var y: Int }" +
                        "return $P(1, true)",
                "argument 1: expected Int but got Bool");

        failureInputWith(
                "struct P { var x: Int; var y: Int }" +
                        "return $P(1, 2).z",
                "Trying to access missing field z on struct P");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testRootAndBlock () {
        successInput("print(\"a\")");
        successInput("print(\"a\" + 1)");
        successInput("print(\"a\") print(\"b\")");
        successInput("print(\"a\") print(\"b\")");
    }

    // ---------------------------------------------------------------------------------------------
}
