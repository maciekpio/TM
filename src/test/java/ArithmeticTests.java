import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

public class ArithmeticTests extends TestFixture {

    Arithmetic arithmeticParser = new Arithmetic();

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
