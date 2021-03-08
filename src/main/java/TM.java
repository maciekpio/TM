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


    public rule IF       = word("if");
    public rule WHILE    = word("while");
    public rule RETURN   = word("return");
    public rule LET      = word("let");

    public rule AS       = word("=");
    public rule PLUS     = word("+");
    public rule MINUS    = word("-");
    public rule TIMES    = word("*");
    public rule DIVID    = word("/");
    public rule OR       = word("||");
    public rule AND      = word("&&");
    public rule EQUAL    = word("==");
    public rule DIFF     = word("!=");
    public rule NOT      = word("!!");

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

    public rule statement = lazy(() ->
            choice(
                    this.compound_state,
                    this.while_state,
                    this.if_state,
                    this.return_state,
                    this.let_def,
                    this.expr_state
            ));

    public rule expr = lazy(() ->
            choice(
                    this.iden,
                    this.compound_expr,
                    this.literal_expr,
                    this.fct_call_expr,
                    this.total_binary_expr,
                    this.total_unary_expr
            )
    );

    //STATEMENTS

    public rule compound_state =
            seq(LBRACE, statement, RBRACE);

    public rule expr_state =
            seq(expr, SEMICOL);

    public rule while_state =
            seq(WHILE, LPAREN, expr, RPAREN, statement);

    public rule if_state =
            seq(IF, LPAREN, expr, RPAREN, statement);

    public rule return_state =
            seq(RETURN, LPAREN, value.opt(), RPAREN, SEMICOL);

    //EXPRESSIONS

    public rule compound_expr =
            seq(LPAREN, expr, RPAREN);

    public rule iden = identifier(string);

    public rule literal_expr = lazy(() ->
            choice(
                    this.iden,
                    this.string,
                    this.number
            )
    );

    public rule fct_call_expr =
            seq(expr, LPAREN, seq(expr, seq(COMMA, expr).opt()).opt(), RPAREN);

    //OPERATIONS EXPRESSIONS

    public rule total_binary_expr =
            seq(expr, this.binary_expr, expr);

    public rule total_unary_expr =
            seq(this.unary_expr, expr);

    public rule binary_expr = lazy (() ->
            choice(
                    AS,
                    PLUS,
                    MINUS,
                    OR,
                    AND,
                    EQUAL,
                    DIFF
            )
    );

    public rule unary_expr = lazy(() ->
            choice(
                    MINUS,
                    NOT
            )
    );

    public rule operation = lazy(() -> choice(
            seq(ws.opt(), this.multiplication),
            seq(ws.opt(), this.division),
            seq(ws.opt(), this.addition),
            seq(ws.opt(), this.subtraction)
    ));

    public rule addition = lazy(() -> seq(number, ws.opt(), PLUS, ws.opt(),
            choice(this.operation, seq(number, ws.opt(), SEMICOL.opt(), this.operation.opt()))));

    public rule subtraction = lazy(() -> seq(number, ws.opt(), MINUS, ws.opt(),
            choice(this.operation, seq(number, ws.opt(), SEMICOL.opt(), this.operation.opt()))));

    public rule multiplication = lazy(() -> seq(number, ws.opt(), TIMES, ws.opt(),
            choice(this.operation, seq(number, ws.opt(), SEMICOL.opt(), this.operation.opt()))));

    public rule division = lazy(() -> seq(number, ws.opt(), DIVID, ws.opt(),
            choice(this.operation, seq(number, ws.opt(), SEMICOL.opt(), this.operation.opt()))));

    //VARIABLES DEFINITION

    public rule let_def =
            seq(LET, iden, AS, value, SEMICOL);



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
