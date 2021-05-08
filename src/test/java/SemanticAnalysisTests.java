import ast.RootNode;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import ast.SighNode;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;

/**
 * NOTE(norswap): These tests were derived from the {@link InterpreterTests} and don't test anything
 * more, but show how to idiomatically test semantic analysis. using {@link UraniumTestFixture}.
 */
public final class SemanticAnalysisTests extends UraniumTestFixture
{
    RootNode ast;

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
        ast = (RootNode) parse("let x = true; let y = x; y = false");
        System.out.println(ast.contents());
        successInput("let x=1");
        successInput("let x=2.0");
        successInput("let x = 0 ; x=x+1");
        successInput("let x=0 ; x=3");
        successInput("let x = \"0\"; x = \"S\"");
        successInput("let x = 2.0; let x = true;");
        successInput("let x = true; let y = !!x; y = true");
        successInput("let x = true; let x = 1");

        failureInput("let x = true; x = 1");
        failureInputWith("let x = 2; let y = true ; let z = x + y", "Trying to plus int with Bool");
        failureInputWith("let x = 2.0; x = true;", "Trying to assign a value to a non-compatible lvalue");
        failureInputWith("let x = true ; x=1", "Trying to assign a value to a non-compatible lvalue");
        failureInputWith("x = x + 1; let x = 2", "variable used before declaration: x");

        // implicit conversions
        //successInput("var x: Float = 1 ; x = 2");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testTest(){
        //ast = (RootNode) parse("let x = 1; let matrix[] = [1, x]; matrix = [2.0, 1.0]");
        //System.out.println(ast.contents());
        //successInput("let x = 1; let y = x+1; y = 2.0");
        //successInput("let x = 1; let matrix[] = [1, x]; matrix = [1, 2]");
        successInput("main () {print(\"main\")}");
    }

    @Test public void testArrayAccess() {
        successInput("let matrix = [1, 2];");
        successInput("let x = 1.0; let matrix = [1, x]; matrix = [2.0, 1.0]");
        failureInput("let matrix = [1, 1]; let x = 2; x=matrix");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testStructDecl() {
        //String letSomeVariables = "let anInt = 1; let aFloat = 2.0; let aBool = true; let aString = \"text\"; let matrix = [1.0, 2.0]; ";
        successInput("struct StructName {attr1 = 0.0}");
        successInput("struct StructName {attr1 = anInt}");
        successInput("struct StructName {attr1 = aFloat; attr2 = false; attr3 = aString; attr4 = anArray}");
        ast = (RootNode) parse("struct Struct_name {attr1 = aFloat; attr2 = false; attr3 = aString; attr4 = anArray}");
        System.out.println(ast.contents());
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testStructAccess() {
        String structPerson = "struct Position {x=0; y=0}; ";
        String structPeople = "struct People {age = anInt; size = aFloat; ofAge = true}; ";
        //successInput(structPeople + "let tibo = new People(21, 175.5, true); let myAge=tibo.age; let mySize=tibo.size; let myOfAge=tibo.ofAge;");
        successInput(structPeople + "let tibo = new People(); tibo.age = 22; let myAge = tibo.age;");
        //successInput("struct StructName {attr1 = anInt}");
        //successInput("struct StructName {attr1 = aFloat; attr2 = false; attr3 = aString; attr4 = anArray}");
        //ast = (RootNode) parse("struct StructName {attr1 = aFloat; attr2 = false; attr3 = aString; attr4 = anArray}");
        //System.out.println(ast.contents());
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testRootAndBlock () {
        successInput("print(\"a\")");
        successInput("print(\"a\" + 1)");
        successInput("print(\"a\") print(\"b\")");
        successInput("print(\"a\") print(\"b\")");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testCalls() {
        successInput(
                "def add (a, b) { return (a + b) } " +
                        "return add(4, 7)");

        successInput(
                "struct Point { var x: Int; var y: Int }" +
                        "return $Point(1, 2)");

        successInput("var str: String = null; return print(str + 1)");

        failureInputWith("return print(1)", "argument 0: expected String but got Int");
    }

    // ---------------------------------------------------------------------------------------------
}
