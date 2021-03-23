import ast.*;
import norswap.autumn.Grammar;

public final class ArithmeticGrammar extends Grammar {
    // ==== LEXICAL ===========================================================

    public rule line_comment =
            seq("##", seq(not("\n"), any).at_least(0));

    public rule ws_item = choice(
            set(" \t\n\r;"),
            line_comment);

    {
        ws = ws_item.at_least(0);
        id_part = choice(alphanum, '_');
    }

    public rule TIMES = word("*");
    public rule DIVID = word("/");
    public rule MODULO = word("%");
    public rule PLUS = word("+");
    public rule MINUS = word("-");
    public rule SEMICOLON = word(";");
    public rule EQUAL = word("==");
    public rule AS = word("=");
    public rule DIFF = word("!=");
    public rule LANGLE_EQUAL = word("<=");
    public rule RANGLE_EQUAL = word(">=");
    public rule LANGLE = word("<");
    public rule RANGLE = word(">");
    public rule AND = word("&&");
    public rule OR = word("||");
    public rule LPAREN = word("(");
    public rule RPAREN = word(")");

    public rule _let = reserved("let");
    public rule _if = reserved("if");
    public rule _else = reserved("else");
    public rule _true = reserved("true").push($ -> new BooleanNode($.span(), true));
    public rule _false = reserved("false").push($ -> new BooleanNode($.span(), false));

    public rule fractional =
            seq('.', digit.at_least(1));

    public rule integer =
            this.number.push($ -> new IntLiteralNode($.span(), Long.parseLong($.str())))
                    .word();

    public rule number =
            seq(opt('-'), integer, fractional.opt())
                    .push($ -> Double.parseDouble($.str()))
                    .word();

    public rule string_char = choice(
            seq(set('"', '\\').not(), any),
            seq('\\', set("\\nrt")));

    public rule string_content =
            string_char.at_least(0)
                    .push($ -> $.str());

    public rule string =
            seq('"', string_content, '"')
                    .push($ -> new StringLiteralNode($.span(), $.$[0]))
                    .word();

    public rule identifier =
            identifier(seq(choice(alpha, '_'), id_part.at_least(0)))
                    .push($ -> $.str());

    // ==== SYNTACTIC =========================================================

    public rule reference =
            identifier.push($ -> new ReferenceNode($.span(), $.$[0]));

    public rule basic_expression = choice(
            reference,
            integer,
            string);

    public rule mult_op = choice(
            TIMES.as_val(BinaryOperator.MULTIPLY),
            DIVID.as_val(BinaryOperator.DIVIDE),
            MODULO.as_val(BinaryOperator.REMAINDER));

    public rule add_op = choice(
            PLUS.as_val(BinaryOperator.ADD),
            MINUS.as_val(BinaryOperator.SUBTRACT));

    public rule cmp_op = choice(
            EQUAL.as_val(BinaryOperator.EQUALITY),
            DIFF.as_val(BinaryOperator.NOT_EQUALS),
            LANGLE_EQUAL.as_val(BinaryOperator.LOWER_EQUAL),
            RANGLE_EQUAL.as_val(BinaryOperator.GREATER_EQUAL),
            LANGLE.as_val(BinaryOperator.LOWER),
            RANGLE.as_val(BinaryOperator.GREATER));

    public rule mult_expr = left_expression()
            .operand(basic_expression)
            .infix(mult_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule add_expr = left_expression()
            .operand(mult_expr)
            .infix(add_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule order_expr = left_expression()
            .operand(choice(add_expr, _true, _false))
            .infix(cmp_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule and_expression = left_expression()
            .operand(order_expr)
            .infix(AND.as_val(BinaryOperator.AND),
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule or_expression = left_expression()
            .operand(and_expression)
            .infix(OR.as_val(BinaryOperator.OR),
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule assignment_expression = right_expression()
            .operand(or_expression)
            .infix(AS,
                    $ -> new AssignmentNode($.span(), $.$[0], $.$[1]));

    public rule expression = seq(assignment_expression);

    public rule paren_expression = lazy(() ->
            seq(LPAREN, this.expression, RPAREN)
                    .push($ -> new ParenthesizedNode($.span(), $.$[0])));

    public rule expression_stmt =
            expression.filter($ -> {
                if (!($.$[0] instanceof AssignmentNode))
                    return false;
                $.push(new ExpressionStatementNode($.span(), $.$[0]));
                return true;
            });

    public rule statement = lazy(() -> choice(
            this.var_decl,
            this.if_stmt,
            this.expression_stmt,
            this.expression));

    public rule var_decl =
            seq(_let, identifier, AS, expression)
                    .push($ -> new VarDeclarationNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule if_stmt =
            seq(_if, paren_expression, statement, seq(_else, statement).or_push_null())
                    .push($ -> new IfNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule root =
            seq(ws, statement.at_least(1))
                    .as_list(StatementNode.class)
                    .push($ -> new RootNode($.span(), $.$[0]));

    @Override
    public rule root() {
        return root;
    }
}
