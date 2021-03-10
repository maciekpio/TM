import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

public class ArithmeticTests extends TestFixture {

    Arithmetic arithmeticParser = new Arithmetic();

    @Test
    public void testIden(){
        this.rule = arithmeticParser.iden;
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
        this.rule = arithmeticParser.integer;
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
        this.rule = arithmeticParser.number;
        success("123");
        success("2147483647"); //2^31
        success("0");
        success("-123");

        failure("2147483648"); //2^31 + 1
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
        this.rule = arithmeticParser.string_content;
        success("this is a test text");
        success("123");
        success("if(true){return false;}");
        success("");
    }

    @Test
    public void testString(){
        this.rule = arithmeticParser.string;
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
        this.rule = arithmeticParser.string_char;

        success("'a'"); //Need to be fixed

        failure("\"this is a test text\"");
        failure("\"123\"");
        failure("\"this is a number 123\"");
        failure("\"this is not a fct {if (true){return false;}\"");
        failure("\"\""); //"" TODO
        failure("\"this is a test text");
        failure("this is a test text\"");
        failure("this is a test text");
        failure("123");
        failure("if(true){return false;}");
    }

    @Test
    public void testReturn(){
        this.rule = arithmeticParser.return_state;
        success("return (5);");
        success("return ();");
        success("return (\"je\");");
        failure("return;");
        failure("return ;");
        failure("return 5;");
    }



    @Test
    public void testSum() {
        this.rule = arithmeticParser.root;
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
