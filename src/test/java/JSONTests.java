import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

public class JSONTests extends TestFixture {

    JSON jsonParser = new JSON();

    @Test
    public void testInteger() {
        this.rule = jsonParser.integer;
        success("0");
        success("1");
        success("12");
        failure("012");
        failure("10 10");
        failure("0 0");
    }

    @Test
    public void testFractional() {
        this.rule = jsonParser.fractional;
        success(".1");
        success("/2");
        failure("0.1");
        failure("1.1");
        failure("1/2");
        failure("0");
        failure("1\\2");
        failure("1/2.1");
    }

    @Test
    public void testArray() {
        this.rule = jsonParser.array;
        success("[]");
        success("[\"A\"]");
        success("[5/2]");
        success("[\"A\", 5/2]");
    }

    @Test
    public void testRoot() {
        this.rule = jsonParser.root;
        success("{}");
        success("{\"key\": 0, \"other_key\": \"value\"}");
    }
}
