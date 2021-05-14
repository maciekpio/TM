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
public final class SemanticTests extends UraniumTestFixture
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
        testHelpS("42");
        testHelpS("42.0");
        testHelpS("\"hello\"");
        testHelpS("true");
        testHelpS("false");
        testHelpS("!true");
        testHelpS("!false");
        testHelpS("!!true");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testNumericBinary() {
        testHelpS("1 + 2");
        testHelpS("2 - 1");
        testHelpS("2 * 3");
        testHelpS("2 / 3");
        testHelpS("3 / 2");
        testHelpS("2 % 3");
        testHelpS("3 % 2");

        testHelpS("1.0 + 2.0");
        testHelpS("2.0 - 1.0");
        testHelpS("2.0 * 3.0");
        testHelpS("2.0 / 3.0");
        testHelpS("3.0 / 2.0");
        testHelpS("2.0 % 3.0");
        testHelpS("3.0 % 2.0");

        testHelpS("1 + 2.0");
        testHelpS("2 - 1.0");
        testHelpS("2 * 3.0");
        testHelpS("2 / 3.0");
        testHelpS("3 / 2.0");
        testHelpS("2 % 3.0");
        testHelpS("3 % 2.0");

        testHelpS("1.0 + 2");
        testHelpS("2.0 - 1");
        testHelpS("2.0 * 3");
        testHelpS("2.0 / 3");
        testHelpS("3.0 / 2");
        testHelpS("2.0 % 3");
        testHelpS("3.0 % 2");

        testHelpF("2 + true", "Trying to plus int with Bool");
        testHelpF("true + 2", "Trying to plus Bool with Int");
    }

    // ---------------------------------------------------------------------------------------------

    public void testHelpS(String testString){
        successInput("main{"+testString+"}");
    }

    public void testHelpF(String testString,String mess){
        failureInputWith("main{"+testString+"}",mess);
    }

    @Test public void testOtherBinary() {

        testHelpS("true && false");
        testHelpS("false && true");
        testHelpS("true && true");
        testHelpS("true || false");
        testHelpS("false || true");
        testHelpS("false || false");

        testHelpF("false || 1",
                "Attempting to perform binary logic on non-boolean type: Int");
        testHelpF("2 || true",
                "Attempting to perform binary logic on non-boolean type: Int");

        testHelpS("1 + \"a\"");
        testHelpS("\"a\" + 1");
        testHelpS("\"a\" + true");

        testHelpS("1 == 1");
        testHelpS("1 == 2");
        testHelpS("1.0 == 1.0");
        testHelpS("1.0 == 2.0");
        testHelpS("true == true");
        testHelpS("false == false");
        testHelpS("true == false");
        testHelpS("1 == 1.0");

        testHelpF("true == 1", "Trying to compare incomparable types Bool and Int");
        testHelpF("2 == false", "Trying to compare incomparable types Int and Bool");

        testHelpS("\"hi\" == \"hi\"");

        testHelpS("1 != 1");
        testHelpS("1 != 2");
        testHelpS("1.0 != 1.0");
        testHelpS("1.0 != 2.0");
        testHelpS("true != true");
        testHelpS("false != false");
        testHelpS("true != false");
        testHelpS("1 != 1.0");

        testHelpF("true != 1", "Trying to compare incomparable types Bool and Int");
        testHelpF("2 != false", "Trying to compare incomparable types Int and Bool");

        testHelpS("\"hi\" != \"hi\"");
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
        successInput("parseInt(\"5\")");
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
        successInput("struct StructName {attr1 = aFloat; attr2 = false; attr3 = aString}");
        ast = (RootNode) parse("struct Struct_name {attr1 = aFloat; attr2 = false; attr3 = aString}");
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
                "def add (a, b) { return (a || b) }; main{add(4, 7)}"
        );

        /*successInput(
                "struct Point {x = anInt; y = anInt }" +
                        "main{(new Point(1, 2))}");

        failureInputWith("main{ print(1) }", "incompatible argument provided for argument 0: expected Int but got String");*/
    }

    // ---------------------------------------------------------------------------------------------
}
