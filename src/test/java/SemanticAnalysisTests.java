import norswap.autumn.AutumnTestFixture;
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
        /*successInput("!!true");
        successInput("!!false");
        successInput("!!!!true");*/
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

        failureInput("2 + true");
        failureInput("true + 2");
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
