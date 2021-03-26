import ast.*;
import norswap.autumn.ParseResult;
import org.testng.annotations.Test;
import norswap.autumn.AutumnTestFixture;

public class GrammarTests extends AutumnTestFixture {

    ArithmeticGrammar arithmeticParser = new ArithmeticGrammar();

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
    public void testRoot() {
        this.rule = arithmeticParser.root;
        success("42");
    }

    @Test
    public void testIf() {
        this.rule = arithmeticParser.if_stmt;
        // This is a simple test to see if it parses
        success("if (true) 1 else 2");
    }

    @Test
    public void testTrue() {
        this.rule = arithmeticParser.and_expression;
        success("true && false");
    }
}
