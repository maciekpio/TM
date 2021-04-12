import ast.*;
import norswap.autumn.Grammar;

import static ast.UnaryOperator.NOT;

public final class TMGrammar extends Grammar {
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
    //public rule SEMICOLON = word(";");
    public rule EQUAL = word("==");
    public rule AS = word("=");
    public rule DIFF = word("!=");
    public rule LOWER_EQUAL = word("<=");
    public rule RANGLE_EQUAL = word(">=");
    public rule LOWER = word("<");
    public rule GREATER = word(">");
    public rule AND = word("&&");
    public rule OR = word("||");
    public rule BANGBANG = word("!!");
    public rule LPAREN = word("(");
    public rule RPAREN = word(")");
    public rule LBRACE = word("{");
    public rule RBRACE = word("}");
    public rule LBRACKET = word("[");
    public rule RBRACKET = word("]");
    public rule COMMA = word(",");
    public rule DOT = word(".");

    public rule _let = reserved("let");
    public rule _if = reserved("if");
    public rule _else = reserved("else");
    public rule _while = reserved("while");
    public rule _def = reserved("def");
    public rule _return = reserved("return");
    public rule _struct = reserved("struct");
    public rule _attr = reserved("attr");
    public rule _array = reserved("array");
    public rule _get = reserved("get");
    public rule _put = reserved("put");
    public rule _true = reserved("true").push($ -> new BooleanNode($.span(), true));
    public rule _false = reserved("false").push($ -> new BooleanNode($.span(), false));
    public rule _null = reserved("null").as_val(null);
    //public rule _print = reserved("print");
    //public rule _rprint = reserved("rprint");

    public rule number =
            seq(opt('-'), choice('0', digit.at_least(1)));

    public rule integer =
            number
                    .push($ -> new IntLiteralNode($.span(), Long.parseLong($.str())))
                    .word();

    public rule fractional =
            seq(number, '.', digit.at_least(1))
                    .push($ -> new FloatLiteralNode($.span(), Double.parseDouble($.str())))
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
            _true,
            _false,
            fractional,
            integer,
            string);

    public rule suffix_expression = left_expression()
            .left(basic_expression)
            .suffix(seq(DOT, identifier),
                    $ -> new AttributeAccessNode($.span(), $.$[0], $.$[1]))
            .suffix(seq(DOT, _get, lazy(() -> this.paren_expression)),
                    $ -> new ArrayPullNode($.span(), $.$[0], $.$[1]))
            .suffix(seq(DOT, _put, lazy(() -> this.paren_put_expression)),
                    $ -> new ArrayPushNode($.span(), $.$[0], $.$[1]))
            .suffix(lazy(() -> this.fct_call_args),
                    $ -> new FctCallNode($.span(), $.$[0], $.$[1]));

    public rule prefix_expression = right_expression()
            .operand(choice(suffix_expression))
            .prefix(BANGBANG.as_val(NOT),
                    $ -> new UnaryExpressionNode($.span(), $.$[0], $.$[1]));

    public rule mult_op = choice(
            TIMES.as_val(BinaryOperator.TIMES),
            DIVID.as_val(BinaryOperator.DIVID),
            MODULO.as_val(BinaryOperator.MODULO));

    public rule add_op = choice(
            PLUS.as_val(BinaryOperator.PLUS),
            MINUS.as_val(BinaryOperator.MINUS));

    public rule cmp_op = choice(
            EQUAL.as_val(BinaryOperator.EQUAL),
            DIFF.as_val(BinaryOperator.DIFF),
            LOWER_EQUAL.as_val(BinaryOperator.LOWER_EQUAL),
            RANGLE_EQUAL.as_val(BinaryOperator.GREATER_EQUAL),
            LOWER.as_val(BinaryOperator.LOWER),
            GREATER.as_val(BinaryOperator.GREATER));

    public rule mult_expr = left_expression()
            .operand(prefix_expression)
            .infix(mult_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule add_expr = left_expression()
            .operand(mult_expr)
            .infix(add_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule order_expr = left_expression()
            .operand(choice(add_expr, _true, _false))
            //.operand(choice(add_expr))
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

    public rule put_expressions = lazy(() ->
            seq(this.expression, COMMA, this.expression)
    );

    /**
     * array matrix[5]
     * matrix.put("this is an object", 0)
     */
    public rule paren_put_expression = lazy(() ->
            seq(LPAREN, this.put_expressions, RPAREN)
                    .push($ -> new PutParametersNode($.span(), $.$[0], $.$[1])));

    public rule expression_stmt =
            expression.filter($ -> {
                if (!($.$[0] instanceof AssignmentNode || $.$[0] instanceof FctCallNode))
                    return false;
                $.push(new ExpressionStatementNode($.span(), $.$[0]));
                return true;
            });

    public rule expression_choice = lazy(() -> choice(
            this.expression_stmt,
            this.expression
    ));

    /**
     * Here is the main way for the root
     */
    public rule statement = lazy(() -> choice(
            this.let_decl,
            this.array_decl,
            this.fct_decl,
            this.struct_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_choice
    ));

    public rule block_statements = lazy(() -> choice(
            this.let_decl,
            this.array_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_choice,
            this.return_stmt
    ));

    public rule fct_statements = lazy(() -> choice(
            this.let_decl,
            this.array_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_choice
    ));

    public rule brace_statement =
            seq(LBRACE, block_statements.at_least(0), RBRACE)
                    .as_list(StatementNode.class).push($ -> new BlockNode($.span(), $.$[0]));

    public rule let_decl =
            seq(_let, identifier, AS, expression)
                    .push($ -> new VarDeclarationNode($.span(), $.$[0], $.$[1]));

    public rule if_stmt =
            seq(_if, paren_expression, brace_statement, seq(_else, brace_statement).or_push_null())
                    .push($ -> new IfNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule while_stmt =
            seq(_while, paren_expression, brace_statement)
                    .push($ -> new WhileNode($.span(), $.$[0], $.$[1]));

    public rule return_stmt =
            seq(_return, LPAREN, expression.or_push_null(), RPAREN)
                    .push($ -> new ReturnNode($.span(), $.$[0]));

    public rule fct_block =
            fct_statements.at_least(0)
                    .as_list(StatementNode.class).push($ -> new BlockNode($.span(), $.$[0]));

    public rule expressions = lazy(() ->
            expression.sep(0, COMMA).as_list(ExpressionNode.class)
    );

    public rule fct_call_arg =
            seq(identifier)
                    .push($ -> new ParameterNode($.span(), $.$[0]));

    public rule fct_decl_args =
            fct_call_arg.sep(0, COMMA)
                    .as_list(ParameterNode.class);

    public rule fct_decl = lazy(() ->
            seq(_def, ws, identifier,
                    LPAREN, fct_decl_args, RPAREN,
                    LBRACE, fct_block, return_stmt, RBRACE)
                    .push($ -> new FctDeclarationNode($.span(), $.$[0], $.$[1], $.$[2], $.$[3]))
    );

    public rule fct_call_args = lazy(() ->
            seq(LPAREN, expressions.opt(), RPAREN));

    public rule struct_decl_attribute =
            seq(_attr, identifier)
                    .push($ -> new AttributeDeclarationNode($.span(), $.$[0])
    );

    /**
     * struct person {
     *     attr firstname;
     *     attr lastname;
     * }
     * person TW = new person;
     * person MP = new person;
     *
     * TW.firstname = Thibault;
     */

    public rule struct_decl = lazy(() ->
            seq(_struct, ws, identifier, ws.opt(), LBRACE, struct_decl_attribute.at_least(1).as_list(DeclarationNode.class), RBRACE)
                    .push($ -> new StructDeclarationNode($.span(), $.$[0], $.$[1]))
    );

    /**
     * TODO
     * Basically the same thing that "expressions" but it will be a counter in the future
     */

    /**
    public rule array_content_expressions = lazy(() ->
            expression.sep(0, COMMA).as_list(ArrayContentExpressionNode.class)
    );

    public rule array_decl_values_decl =
            seq(AS, LBRACKET, array_content_expressions, RBRACKET)
            .push($ -> new ArrayLiteralNode($.span(), $.$[0]));


    public rule array_decl =
            seq(_array, ws, identifier, LBRACKET, expression, RBRACKET, array_decl_values_decl.or_push_null())
                    .push($ -> new ArrayDeclarationNode($.span(), $.$[0], $.$[1]));
     */

    public rule array_length =
            seq(LBRACKET, expression, RBRACKET)
            .push($ -> new ArrayLengthNode($.span(), $.$[0]));

    public rule array_decl =
            seq(_array, ws, identifier, array_length)
                    .push($ -> new ArrayDeclarationNode($.span(), $.$[0], $.$[1]));

    public rule root =
            seq(ws, statement.at_least(1))
                    .as_list(StatementNode.class)
                    .push($ -> new RootNode($.span(), $.$[0]));

    @Override
    public rule root() {
        return root;
    }
}