import ast.*;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.Span;
import org.testng.annotations.Test;
import norswap.autumn.AutumnTestFixture;

public class GrammarTests extends AutumnTestFixture {

    TMGrammar TMParser = new TMGrammar();
    ParseResult ast;

    String structBasic = "struct struct_name {identifier1 = anInt}";
    String structWith3values = "struct struct_name {identifier1 = anInt identifier2 = aString identifierN = aFloat}";
    String structBackSlashN = "struct struct_name {identifier1 = anInt\n identifier2 = aString\n identifierN = aFloat}";
    String structWithSpaces = "struct    struct_name    {  identifier1   =   value1     identifier2   =   value2}";
    String structWithError1 = "struct struct_name {identifier1 = value1, identifier2 = value2}";
    String structWithError2 = "struct struct_name {if(true){identifier1 = value1} identifier2 = value2}";
    String structWithError3 = "struct {identifier1 = value1, identifier2 = value2}";
    String structWithError4 = "structstruct_name {identifier1 = value1; identifier2 = value2}";
    String structWithError5 = "struct struct_name (identifier1 = value1 identifier2 = value2)";
    String structWithError6 = "struct struct_name {identifier1 = value1 identifier2 = value2";
    String structWithError7 = "struct_name {identifier1 = value1 identifier2 = value2}";

    String fctBasicVoid = "def fct_name () {return ()}";
    String fctVoid = "def fct_name () {}";
    String fctWith1Arg = "def fct_name (arg1) {return (arg1)}";
    String fctComplex = "def fct_name (arg1, arg2, arg3) {if(arg1) {return (false);} return (true)}";
    String fctWithSpaces = "def   fct_name   (  )   {  return   (   )    }";
    String fctError1 = "def () {return ()}";
    String fctError2 = "def fct_name {return ()}";
    String fctError3 = "def fct_name () {return ()";
    String fctError4 = "def fct_name (if, while) return ()";
    String fctError5 = "def fct_name ()";

    @Test
    public void testIden(){
        this.rule = TMParser.identifier;
        success("anIdentifier");
        success("super"); //not in reserved
        success("String"); //In our language, String is not reserved
        success("CONSTANT");
        success("i");
        successExpect("anIdentifier", "anIdentifier");

        failure("(i)");
        failure("if");
        failure("an identifier");
        failure("\"thisIsString\"");
        failure("if(true){return false}");
    }

    @Test
    public void testInteger(){
        this.rule = TMParser.integer;
        success("123");
        success("2147483647");
        success("0");

        failure("123.5");
        failure("123 5");
        failure("this is a test text");
        failure("\"\"");
        failure("if");
        failure("if(true){return false}");
        failure("(123)");
        failure("");
    }

    @Test
    public void testNumber(){
        this.rule = TMParser.number;

        success("123");
        success("2147483647"); //2^31 so it should success
        success("0");
        success("-123");

        //failure("2147483648"); //2^31 + 1 so it is no longer an integer

        failure("123 5");
        failure("this is a test text");
        failure("\"\"");
        failure("if");
        failure("if(true){return false}");
        failure("(123)");
        failure("");
    }

    @Test
    public void testStringContent(){
        this.rule = TMParser.string_content;

        successExpect("this is a test text", "this is a test text");

        success("this is a test text");
        success("123");
        success("if(true){return false}");
        success("");

        failure("\"this is a string\"");
    }

    @Test
    public void testString(){
        this.rule = TMParser.string;
        success("\"this is a test text\"");
        success("\"123\"");
        success("\"this is a number 123\"");
        success("\"this is not a fct {if (true){return false}\"");
        success("\"\"");
        success("\"je\"");

        failure("\"this is a test text");
        failure("this is a test text\"");
        failure("this is a test text");
        failure("123");
        failure("if(true){return false}");
    }

    @Test
    public void testStringChar(){
        this.rule = TMParser.string_char;

        failure("\"this is a test text\"");
        failure("\"123\"");
        failure("\"this is a number 123\"");
        failure("\"this is not a fct {if (true){return false}\"");
        failure("\"\"");
        failure("\"this is a test text");
        failure("this is a test text\"");
        failure("this is a test text");
        failure("123");
        failure("if(true){return false}");
    }

    @Test
    public void testReturn(){
        this.rule = TMParser.return_stmt;
        success("return (5)");
        success("return ()");
        success("return ( ) ");
        success("return (\"this is a string\")");
        success("return (anIdentifier)");
        success("return()"); //the ws is optional

        failure("return");
        failure("return ");
        failure("return 5");
        failure("return (5 ");
        failure("return (if)");
    }

    @Test
    public void testStruct(){
        this.rule = TMParser.struct_decl;

        success(structBasic);
        success(structWith3values);
        success(structBackSlashN);
        success(structWithSpaces);

        failure(structWithError1);
        failure(structWithError2);
        failure(structWithError3);
        failure(structWithError4);
        failure(structWithError5);
        failure(structWithError6);
        failure(structWithError7);
        failure("if (struct) {return ()}");
        failure(" ");
    }

    @Test
    public void testLetDefinition(){
        this.rule = TMParser.let_decl;
        success("let anIdentifier = 123");
        success("let anIdentifier = \"this is a string\"");
        success("let anIdentifier = -5");
        success("let anIdentifier = a&&b");
        success("let anIdentifier = anOtherIden");

        failure("anIdentifier = 123");
        failure("let an, Identifier = 123, 124");//Our language doesn't allow that
        failure("let if = 123");
    }

    @Test
    public void testFctArgs(){
        this.rule=TMParser.fct_call_args;
        success("(anIdentifier)");
        success("(anIdentifier, anOther)");
        success("(anIden   ,   anOther)   ");

        failure("anIdentifier"); //It needs the comma at the first place
        failure(", anIden anOther");
        failure(", anIden, ");
        failure(" ");
        failure("");
        failure("if");
        failure("1 2");
        failure("a=b");
    }

    @Test
    public void testFctDefinition(){
        this.rule=TMParser.fct_decl;
        success(fctBasicVoid);
        success(fctWith1Arg);
        success(fctComplex);
        success(fctWithSpaces);
        success(fctVoid);

        failure(fctError1);
        failure(fctError2);
        failure(fctError3);
        failure(fctError4);
        failure(fctError5);
    }

    @Test
    public void testValue(){
        this.rule=TMParser.expression;
        success("anIdentifier");
        success("\"this is a string\"");
        success("123");
        success("true");
        success("false");
        failure(" ");
        failure("");
        failure("if");
    }

    @Test
    public void testStatement(){
        this.rule=TMParser.statement;
        success("print(a)");
        success("if (a&&b) {return ()}");
    }

    @Test
    public void testExpr(){
        this.rule=TMParser.expression;
        success("anIdentifier");
        success("123");
        success("\"this is a string\"");
        success("aFunction(arg1, arg2)");
        success("a+b");

        failure("()");
    }

    @Test
    public void testFctCallExpr(){
        this.rule=TMParser.suffix_expression;
        success("aFunction()");
        success("aFunction(arg1)");
        success("aFunction(arg1, arg2, arg3)");
        success("aFunction(a&&b)");
        success("print(a)");
    }

    @Test
    public void testLineComment(){
        this.rule=TMParser.line_comment;
        success("##this is a comment");
        success("##");
        success("#####");
        success("##\"\"");
        success("## return (true)");

        failure("\n");
        failure("#hello");
        failure("#hello#");
        failure("#*");
        failure("*#");
    }

    @Test
    public void testSpaceChar(){
        this.rule=TMParser.usual_whitespace;
        success(" ");
        success("\n");
        success("  ");

        failure("5");
        failure("#*");
        failure("*#");
        failure("##je m'appele");
        failure("##");

    }

    @Test
    public void testExpression() {
        this.rule = TMParser.expression;
        successExpect("2*3", new BinaryExpressionNode(null, new IntLiteralNode(null,2), BinaryOperator.TIMES, new IntLiteralNode(null,3)));
        successExpect("2/3", new BinaryExpressionNode(null,new IntLiteralNode(null,2),BinaryOperator.DIVID, new IntLiteralNode(null,3)));
        successExpect("2-3", new BinaryExpressionNode(null,new IntLiteralNode(null,2),BinaryOperator.MINUS, new IntLiteralNode(null,3)));
        successExpect("1+2*3", new BinaryExpressionNode(null,new IntLiteralNode(null,1),BinaryOperator.PLUS, new BinaryExpressionNode(null,new IntLiteralNode(null,2),BinaryOperator.TIMES, new IntLiteralNode(null,3))));
        successExpect("1*2+3", new BinaryExpressionNode(null, new BinaryExpressionNode(null,new IntLiteralNode(null,1),BinaryOperator.TIMES, new IntLiteralNode(null,2)) ,BinaryOperator.PLUS, new IntLiteralNode(null,3)));
        successExpect("1==2", new BinaryExpressionNode(null,new IntLiteralNode(null,1), BinaryOperator.EQUAL, new IntLiteralNode(null,2)));
        successExpect("true&&false", new BinaryExpressionNode( null,new BooleanLiteralNode(null,true),BinaryOperator.AND, new BooleanLiteralNode(null,false)));
        successExpect("true||false", new BinaryExpressionNode(null,new BooleanLiteralNode(null,true), BinaryOperator.OR,new BooleanLiteralNode(null,false)));
    }

    String ifTest1 = "if (i%15 == 0){\n" +
            "print(\"FizzBuzz\")" +
            "} else {if (i % 3 == 0){\n" +
            "print(\"Fizz\")}" +
            "}";

    @Test
    public void testIf() {
        this.rule = TMParser.if_stmt;
        // This is a simple test to see if it parses
        success("if (true) {return (1)} \n else {return (2)}");
        success(ifTest1);
    }

    @Test
    public void testTrue() {
        this.rule = TMParser.and_expression;
        success("true && false");
    }

    @Test
    public void testLetDecl(){
        this.rule = TMParser.let_decl;
        ast = successExpect("let x = 1", new LetDeclarationNode(new Span(0,0), "x", new IntLiteralNode(null, 1)));
        System.out.println(ast.valueStack);
    }

    @Test
    public void testReference(){
        this.rule = TMParser.root;
        ast = success("let x = 1; let matrix= [1, x];");
        System.out.println(ast.valueStack);
    }

    @Test
    public void testPut(){
        this.rule = TMParser.root;
        ast = success("x.put(0: 3)");
        System.out.println(ast.valueStack);
    }
}
