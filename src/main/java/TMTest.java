import norswap.autumn.TestFixture;
import org.testng.annotations.Test;
public class TMTest extends TestFixture {
    TM arithmeticParser = new TM();

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