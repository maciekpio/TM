import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

public class TMTests extends TestFixture {

    TM TMParser = new TM();

    String structBasic = "struct struct_name {identifier1 = value1;}";
    String structWith3values = "struct struct_name {identifier1 = value1; identifier2 = value2; identifierN = valueN;}";
    String structBackSlashN = "struct struct_name {identifier1 = value1;\n identifier2 = value2;\n identifierN = valueN;}";
    String structWithSpaces = "struct    struct_name    {  identifier1   =   value1  ;   identifier2   =   value2 ; }";
    String structWithError1 = "struct struct_name {identifier1 = value1, identifier2 = value2;}";
    String structWithError2 = "struct struct_name {if(true){identifier1 = value1;} identifier2 = value2;}";
    String structWithError3 = "struct {identifier1 = value1, identifier2 = value2;}";
    String structWithError4 = "structstruct_name {identifier1 = value1; identifier2 = value2;}";
    String structWithError5 = "struct struct_name (identifier1 = value1; identifier2 = value2;)";
    String structWithError6 = "struct struct_name {identifier1 = value1; identifier2 = value2;";
    String structWithError7 = "struct_name {identifier1 = value1; identifier2 = value2;}";

    String fctBasicVoid = "def fct_name () {return ();}";
    String fctWith1Arg = "def fct_name (arg1) {return (arg1);}";
    String fctComplex = "def fct_name (arg1, arg2, arg3) {if(arg1) {return (false);} return (true);}";
    String fctWithSpaces = "def   fct_name   (  )   {  return   (   )  ;  }";
    String fctError1 = "def () {return ();}";
    String fctError2 = "def fct_name {return ();}";
    String fctError3 = "def fct_name () {return ();";
    String fctError4 = "def fct_name (if, while) return ();";
    String fctError5 = "def fct_name () {}";
    String fctError6 = "def fct_name ();";

    @Test
    public void testIden(){
        this.rule = TMParser.iden;
        success("anIdentifier");
        success("super"); //not in reserved
        success("String"); //In our language, String is not reserved
        success("CONSTANT");
        success("i");

        failure("(i)");
        failure("if");
        failure("an identifier");
        failure("\"thisIsString\"");
        failure("if(true){return false;}");
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
        failure("if(true){return false;}");
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

        failure("2147483648"); //2^31 + 1 so it is no longer an integer
        failure("123.5");
        failure("123 5");
        failure("this is a test text");
        failure("\"\"");
        failure("if");
        failure("if(true){return false;}");
        failure("(123)");
        failure("");
    }

    @Test
    public void testStringContent(){
        this.rule = TMParser.string_content;
        success("this is a test text");
        success("123");
        success("if(true){return false;}");
        success("");
    }

    @Test
    public void testString(){
        this.rule = TMParser.string;
        success("\"this is a test text\"");
        success("\"123\"");
        success("\"this is a number 123\"");
        success("\"this is not a fct {if (true){return false;}\"");
        success("\"\"");
        success("\"je\"");

        failure("\"this is a test text");
        failure("this is a test text\"");
        failure("this is a test text");
        failure("123");
        failure("if(true){return false;}");
    }

    @Test
    public void testStringChar(){
        this.rule = TMParser.string_char;

        success("'a'"); //Need to be fixed

        failure("\"this is a test text\"");
        failure("\"123\"");
        failure("\"this is a number 123\"");
        failure("\"this is not a fct {if (true){return false;}\"");
        failure("\"\"");
        failure("\"this is a test text");
        failure("this is a test text\"");
        failure("this is a test text");
        failure("123");
        failure("if(true){return false;}");
    }

    @Test
    public void testReturn(){
        this.rule = TMParser.return_state;
        success("return (5);");
        success("return ();");
        success("return ( ) ;");
        success("return (\"this is a string\");");
        success("return (anIdentifier);");
        success("return();"); //the ws is optional

        failure("return;");
        failure("return ;");
        failure("return 5;");
        failure("return (5 ;");
        failure("return (if);");
        failure("return (5)");
    }

    @Test
    public void testStruct(){


        this.rule = TMParser.struct_definition;
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
        failure("if (struct) {return ();}");
        failure(" ");
    }

    @Test
    public void testLetDefinition(){
        this.rule = TMParser.let_def_state;
        success("let anIdentifier = 123;");
        success("let anIdentifier = \"this is a string\";");
        success("let anIdentifier = -5;");
        success("let anIdentifier = (a)&&(b);");
        success("let anIdentifier = anOtherIden;");
        success("let anIdentifier = [0, 1, 2, 3];");

        failure("anIdentifier = 123;");
        failure("let anIdentifier = 123");
        failure("let an, Identifier = 123, 124;");//Our language doesn't allow that
        failure("let if = 123;");
    }

    @Test
    public void testFctArgs(){
        this.rule=TMParser.fct_args;
        success(", anIdentifier");
        success(", anIdentifier, anOther");
        success("   ,    anIden   ,   anOther   ");

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
        this.rule=TMParser.fct_definition;
        success(fctBasicVoid);
        success(fctWith1Arg);
        success(fctComplex);
        success(fctWithSpaces);

        failure(fctError1);
        failure(fctError2);
        failure(fctError3);
        failure(fctError4);
        failure(fctError5);
        failure(fctError6);
    }

    @Test
    public void testValue(){
        this.rule=TMParser.value;
        success("anIdentifier");
        success("\"this is a string\"");
        success("123");
        success(structBasic);
        success("true");
        success("false");
        success("null");
        failure(" ");
        failure("");
        failure("if");
        failure("1 2");
        failure("a=b");
    }

    @Test
    public void testStatement(){
        this.rule=TMParser.statement;
        success("if (a&&b) {return ();}");
    }

    //#TODO
    @Test
    public void testIfState(){
        this.rule=TMParser.if_state;
        success("if ((boolean)==(true)) {return ();}");
    }

    //#TODO
    @Test
    public void testWhileState(){
        this.rule=TMParser.while_state;
    }

    //#TODO
    @Test
    public void testState(){
        this.rule=TMParser.statement;
    }

    //#TODO
    @Test
    public void testEntireUnaryExpr(){
        this.rule=TMParser.entire_unary_expr;
        success("-5");
        success("!!5");
        success("!!True");
        success("-a");
        success("--5");
        success("!! 5");
        success("- 5");

        failure("!5");
        failure("!!");
        failure("-");
    }

    //#TODO
    @Test
    public void testUnaryExpr(){
        this.rule=TMParser.unary_expr;
        success("-");
        success("!!");

        failure("! !");
        failure("--");
        failure("-!");
        failure("!-");
        failure("?");
        failure("!!!");
    }

    //#TODO
    @Test
    public void testNotExpr(){
        this.rule=TMParser.not_expr;
    }

    //#TODO
    @Test
    public void testP(){
        this.rule=TMParser.P;
    }

    //#TODO
    @Test
    public void testS(){
        this.rule=TMParser.S;
    }

    //#TODO
    @Test
    public void testEqExpr(){
        this.rule=TMParser.eq_expr;
    }

    //#TODO
    @Test
    public void testBinaryAndExpr(){
        this.rule=TMParser.binary_and_expr;
        success("&&");

        failure("& &");
        failure("&");
        failure("\"&&\"");
    }

    //#TODO
    @Test
    public void testBinaryOrExpr(){
        this.rule=TMParser.binary_or_expr;
        success("||");

        failure("| |");
        failure("|");
        failure("\"||\"");
    }

    @Test
    public void testExpr(){
        this.rule=TMParser.expr;
        success("anIdentifier");
        success("123");
        success("\"this is a string\"");
        success("null");
        success("(anExpression)");
        success("aFunction(arg1, arg2)");//TODO il faut change les priorites du choice de expr
        //success("(a)+(b)"); //TODO voit d'abord '(a)' comme une compound_expr au lieu de voir le tout comme une entire_binary_expr
        success("!!aBoolean");

        failure("()");
    }

    @Test
    public void testFctCallExpr(){
        this.rule=TMParser.fct_call_expr;
        success("aFunction()");
        success("aFunction(arg1)");
        success("aFunction(arg1, arg2, arg3)");
        success("aFunction((a)&&(b))");
    }

    @Test
    public void testEntireBinaryExpr(){
        this.rule=TMParser.entire_binary_expr;
        success("(a)==(b)");
        success("(a)&&(b)");
        success("(a)||(b)");
        success("(a)!=(b)");
        success("(a)*(b)");
        success("(a)/(b)");
        success("(a)+(b)");
        success("(2)-(5)");
        success("(\"string1\")+(\"string2\")"); //It can be used as a concat
        success("(aBoolean)==(true)");

        failure("(a)=(b)");
        failure("(a+b)");
        failure("a+b");
        failure("(if)==(while)");
    }

    @Test
    public void testNotLine(){
        this.rule=TMParser.not_line;
        success("5");
        success(" ");
        success("\r");

        failure("\n");
    }

    @Test
    public void testLineComment(){
        this.rule=TMParser.line_comment;
        success("##this is a comment");
        success("##");
        success("#####");
        success("##\"\"");
        success("## return (true);");

        failure("\n");
        failure("#hello");
        failure("#hello#");
        failure("#*");
        failure("*#");
    }

    @Test
    public void testMultiComment(){
        this.rule=TMParser.multi_comment;
        success("#* yes *#");
        success("#**#");
        success("#* *#");
        success("#*5*#");

        failure("##");
        failure("#");
        failure("#*#");
        failure("#*");
        failure("#yes#");
    }

    @Test
    public void testSpaceChar(){
        this.rule=TMParser.space_char;
        success(" ");
        success("\n");

        /*TODO voir pq ca fail avec 2 espaces => perso je pense qu'on est pas obligé de tester cette rule
        ** Ici ca teste seulement l'espace en lui-meme donc normal
        **/
        failure("  ");
        failure("5");
        failure("#*");
        failure("*#");
        failure("##je m'appele");
        failure("##");

    }

    @Test
    public void testArrayDefinition(){
        this.rule=TMParser.array_definition;
        success("array matrix[5]");
        success("array m[a]");

        failure("array [5]");
        failure("array m[\"je\"]");
        failure("array matrix[]");
        failure("array[5]");
        failure("arraymatrix[5]");
    }

    @Test
    public void testSum() {
        this.rule = TMParser.root;
//        success("1");
        success("1+1;");
        success("1 + 1;");
        success("1 - 1;");
        success("1 + 1 + 1;");
        success("1 - 1 + 1;");
        success("1 + 1 - 1;");
        // Exemple TP
        success("1 + 1 + 1000");
        success("1 + 90 * 85");
        success("1 - 1");
        success("1 / 2+ 1");
        success("-1 + 1");
        success("1 + 1 + 1000; 1 + 90 * 85; 1 - 1; 1 / 2+ 1; -1 + 1");
        // Failure
        failure("1 1");
        failure("1 +-+- 1");
    }
}
