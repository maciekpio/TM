import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class TM extends Grammar {

    // Lexical

    { ws = usual_whitespace; }

    public rule integer =
            choice('0', digit.at_least(1));

    public rule number =
            seq(opt('-'), integer).word();

    public rule string_char = choice(
            seq(set('"', '#').not(), range('\u0000', '\u001F').not(), any)
            , seq('#', set("#/bfnrt"))
            );
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
    public rule SEMICOL  = word(";");
    public rule EQUAL    = word("=");
    public rule IF       = word("if");
    public rule WHILE    = word("while");
    public rule RETURN   = word("return");
    public rule LET      = word("let");

    // Syntactic

    public rule value = lazy(() -> choice(
            string,
            number,
            this.object,
            this.array,
            word("true")  .as_val(true),
            word("false") .as_val(false),
            word("null")  .as_val(null)));

    public rule statement = lazy(() ->
            choice(
                    this.compound_state,
                    this.while_state,
                    this.if_state,
                    this.return_state,
                    this.let_def,
                    this.expr_state
            ));

    public rule pair =
            seq(string, COLON, value);

    public rule object =
            seq(LBRACE, pair.sep(0, COMMA), RBRACE);

    public rule array =
            seq(LBRACKET, value.sep(0, COMMA), RBRACKET);

    public rule compound_state =
            seq(LBRACE, statement, RBRACE);

    public rule expr_state =
            seq(expr, SEMICOL);

    public rule expr;

    public rule while_state =
            seq(WHILE, LPAREN, expr, RPAREN, statement);

    public rule if_state =
            seq(IF, LPAREN, expr, RPAREN, statement);

    public rule return_state =
            seq(RETURN, LPAREN, value.opt(), RPAREN, SEMICOL);

    public rule let_def =
            seq(LET, identifier, EQUAL, value, SEMICOL);

    public rule identifier;



    public rule root = seq(ws, value);

    @Override public rule root() {
        return root;
    }

    public void parse (String inputName, String input) {
        ParseResult result = Autumn.parse(root, input, ParseOptions.get());
        if (result.fullMatch) {
            System.out.println(result.toString());
        } else {
            // debugging
            System.out.println(result.toString(new LineMapString(inputName, input), false));
            // for users
            System.out.println(result.userErrorString(new LineMapString(inputName, input)));
        }
    }

    public static void main (String[] args) {
        // failing parse example
        new TM().parse("<test>", "{ \"test\" : // }");
    }
}
