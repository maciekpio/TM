import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.actions.StackPush;
import norswap.autumn.positions.LineMapString;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TM extends Grammar {

    //RESERVED KEYWORDS AND OPERATORS

    //public rule _fixed          = reserved("fixed");
    public rule _else           = reserved("else");
    public rule _if             = reserved("if");
    public rule _return         = reserved("return");
    public rule _while          = reserved("while");

    public rule _false          = reserved("false")  .as_val(false);
    public rule _true           = reserved("true")   .as_val(true);
    public rule _null           = reserved("null")   .as_val(null);

    // Lexical

    { ws = usual_whitespace; }

    public rule integer =
            choice('0', digit.at_least(1));

    public rule number =
            seq(opt('-'), integer)
            .push($ -> Integer.parseInt($.str()))
            .word();

    public rule string_char = choice(
            seq(set('"', '#').not(), range('\u0000', '\u001F').not(), any)
            , seq('#', set("#/bfnrt"))
            );

    public rule string_content =
            string_char.at_least(0)
                    .push($ -> $.str());

    public rule string =
            seq('"',string_content , '"')
                    .word();

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
            seq(LBRACE, pair.sep(0, COMMA), RBRACE)
                    .push($ -> Arrays.stream($.$)
                            .map(x -> (Object[]) x)
                            .collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));


    public rule array =
            seq(LBRACKET, value.sep(0, COMMA), RBRACKET)
                    .as_list(Object.class);

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

    //public rule iden = identifier(string);

    public rule id_start = cpred(Character::isJavaIdentifierStart);
    {           id_part  = cpred(c -> c != 0 && Character.isJavaIdentifierPart(c)); }

    /** Rule for parsing Identifiers, ensuring we do not match keywords, and memoized. */
    public rule iden = identifier(seq(id_start, id_part.at_least(0)))
            .push($ -> ($.str()))
            .memo(32);

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

    //OPERATIONS

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

    //PRIORITIES

    /**
    StackPush binary_push =
            $ -> BinaryExpression.mk($.$1(), $.$0(), $.$2());
     */

    public rule not_expr = right_expression()
            .right(integer);

    public rule P = left_expression()
            .operand(not_expr)
            .infix(DIVID, $ -> new Div($.$0(), $.$1()))
            .infix(TIMES, $ -> new Multi($.$0(), $.$1()));

    public rule S = left_expression()
            .operand(P)
            .infix(PLUS, $ -> new Add($.$0(), $.$1()))
            .infix(MINUS, $ -> new Sub($.$0(), $.$1()));

    public rule eq_expr = left_expression()
            .operand(S)
            .infix(EQUAL, $ -> new BinaryEqual($.$0(), $.$1()));

    public rule binary_and_expr = left_expression()
            .operand(eq_expr)
            .infix(AND, $ -> new BinaryAnd($.$0(), $.$1()));

    public rule binary_or_expr = left_expression()
            .operand(binary_and_expr)
            .infix(OR, $ -> new BinaryOr($.$0(), $.$1()));



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
