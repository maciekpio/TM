import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class JSON extends Grammar
{
    // Lexical
    
    { ws = usual_whitespace; }

    public rule integer =
        choice('0', digit.at_least(1));

    public rule fractional = choice(
            seq('.', digit.at_least(1)),
            seq('/', integer)
    );

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(opt('-'), integer, fractional.opt(), exponent.opt()).word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq('\\', set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq('"', string_char.at_least(0), '"').word();

    public rule LBRACE   = word("{");
    public rule RBRACE   = word("}");
    public rule LBRACKET = word("[");
    public rule RBRACKET = word("]");
    public rule LPAREN   = word("(");
    public rule RPAREN   = word(")");
    public rule COLON    = word(":");
    public rule COMMA    = word(",");

    // Syntactic

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true")  .as_val(true),
        word("false") .as_val(false),
        word("null")  .as_val(null)));

    public rule pair =
        seq(string, COLON, value);

    public rule object =
        seq(LBRACE, pair.sep(0, COMMA), RBRACE);

    public rule array =
        seq(LBRACKET, value.sep(0, COMMA), RBRACKET);

    public rule root = seq(ws, value);

    @Override public rule root() {
        return root;
    }

    public void parse (String input) {
        ParseResult result = Autumn.parse(root, input, ParseOptions.get());
        if (result.fullMatch) {
            System.out.println(result.toString());
        } else {
            // debugging
            System.out.println(result.toString(new LineMapString(input), false, "<input>"));
            // for users
            System.out.println(result.userErrorString(new LineMapString(input), "<input>"));
        }
    }
}

