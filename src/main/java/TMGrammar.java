import ast.*;
import norswap.autumn.Grammar;
import norswap.autumn.actions.ActionContext;

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

    /*Useful signs*/
    public rule TIMES        = word("*");
    public rule POW          = word("**");
    public rule DIVID        = word("/");
    public rule MODULO       = word("%");
    public rule PLUS         = word("+");
    public rule MINUS        = word("-");
    public rule EQUAL        = word("==");
    public rule AS           = word("=");
    public rule DIFF         = word("!=");
    public rule LOWER_EQUAL  = word("<=");
    public rule RANGLE_EQUAL = word(">=");
    public rule LOWER        = word("<");
    public rule GREATER      = word(">");
    public rule AND          = word("&&");
    public rule OR           = word("||");
    public rule BANG         = word("!");
    public rule LPAREN       = word("(");
    public rule RPAREN       = word(")");
    public rule LBRACE       = word("{");
    public rule RBRACE       = word("}");
    public rule LBRACKET     = word("[");
    public rule RBRACKET     = word("]");
    public rule COMMA        = word(",");
    public rule DOT          = word(".");
    public rule COLON        = word(":");

    /*Reserved words*/
    public rule _let         = reserved("let");
    public rule _if          = reserved("if");
    public rule _else        = reserved("else");
    public rule _while       = reserved("while");
    public rule _def         = reserved("def");
    public rule _new         = reserved("new");
    public rule _return      = reserved("return");
    public rule _struct      = reserved("struct");
    public rule _main        = reserved("main");
    public rule _get         = reserved("get");
    public rule _put         = reserved("put");
    public rule _arrayOf     = reserved("arrayOf");

    /*Reserved constants*/
    public rule _true        = reserved("true").push($ -> new BooleanLiteralNode($.span(), true));
    public rule _false       = reserved("false").push($ -> new BooleanLiteralNode($.span(), false));
    public rule _anInt       = reserved("anInt").push($ -> new IntLiteralNode($.span(), 0));
    public rule _aFloat      = reserved("aFloat").push($ -> new FloatLiteralNode($.span(), 0.0));
    public rule _aString     = reserved("aString").push($ -> new StringLiteralNode($.span(), ""));
    /*public rule _anArray     = reserved("anArray").push($ -> new ArrayLiteralNode($.span(),
            new ArrayList<ExpressionNode>(){{ add(0, new IntLiteralNode($.span(), 0)); }}));//[0]*/

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
                    .push(ActionContext::str);

    public rule string =
            seq('"', string_content, '"')
                    .push($ -> new StringLiteralNode($.span(), $.$[0]))
                    .word();

    public rule identifier =
            identifier(seq(choice(alpha, '_'), id_part.at_least(0)))
                    .push(ActionContext::str);

    // ==== SYNTACTIC =========================================================

    public rule reference =
            identifier.push($ -> new ReferenceNode($.span(), $.$[0]));

    public rule constructor =
            seq(_new, reference)
                    .push($ -> new ConstructorNode($.span(), $.$[0]));

    public rule paren_expression = lazy(() ->
            seq(LPAREN, this.expression, RPAREN)
                    .push($ -> new ParenthesizedNode($.span(), $.$[0])));

    public rule expressions = lazy(() ->
            this.expression.sep(0, COMMA).as_list(ExpressionNode.class)
    );

    public rule array_expressions = lazy(() ->
            this.expression.sep(1, COMMA).as_list(ExpressionNode.class)
    );

    public rule array_literal =
            seq(LBRACKET, array_expressions, RBRACKET)
                    .push($ -> new ArrayLiteralNode($.span(), $.$[0]));

    public rule array_of = lazy(() ->
            seq(_arrayOf, LPAREN, this.expression, COLON, this.expression, RPAREN)
                    .push($ -> new ArrayOfNode($.span(), $.$[0], $.$[1])));

    public rule array_choice =
            choice(array_of, array_literal);

    public rule basic_expression = choice(
            constructor,
            reference,
            _true,
            _false,
            _anInt,
            _aFloat,
            _aString,
            array_choice,
            //_null,
            fractional,
            integer,
            string,
            paren_expression,
            array_literal);

    public rule suffix_expression = left_expression()
            .left(basic_expression)
            .suffix(seq(DOT, identifier),
                    $ -> new AttributeAccessNode($.span(), $.$[0], $.$[1]))
            .suffix(seq(DOT, _get, LPAREN, lazy(() -> this.expression), RPAREN),
                    $ -> new ArrayGetNode($.span(), $.$[0], $.$[1]))
            .suffix(seq(DOT, _put, LPAREN, lazy(() -> this.expression).sep_exact(2, COLON), RPAREN),
                    $ -> new ArrayPutNode($.span(), $.$[0], $.$[1], $.$[2]))
            .suffix(lazy(() -> this.fct_call_args),
                    $ -> new FctCallNode($.span(), $.$[0], $.$[1]));

    public rule prefix_expression = right_expression()
            .operand(choice(suffix_expression))
            .prefix(BANG.as_val(UnaryOperator.NOT),
                    $ -> new UnaryExpressionNode($.span(), $.$[0], $.$[1]));

    public rule mult_op = choice(
            TIMES.as_val(BinaryOperator.TIMES),
            DIVID.as_val(BinaryOperator.DIVID),
            MODULO.as_val(BinaryOperator.MODULO));

    public rule pow_op = POW.as_val(BinaryOperator.POW);

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

    public rule pow_expr = left_expression()
            .operand(prefix_expression)
            .infix(pow_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule mult_expr = left_expression()
            .operand(pow_expr)
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

    /*
    public rule paren_put_expression = lazy(() ->
            seq(LPAREN, this.expression, COMMA, this.expression, RPAREN));
    */

    public rule expression_stmt =
            expression.filter($ -> {
                if (!($.$[0] instanceof AssignmentNode || $.$[0] instanceof FctCallNode || $.$[0] instanceof ArrayPutNode)){
                    System.out.println($.$[0] + " filter false");
                    return false;
                }
                $.push(new ExpressionStatementNode($.span(), $.$[0]));
                System.out.println($.$[0] + " filter true");
                return true;
            });

    /*
     * Here is the main way for the root
     */
    public rule statement = lazy(() -> choice(
            this.main_stmt,
            this.let_decl,
            this.fct_decl,
            this.struct_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_stmt
    ));

    public rule block_statements = lazy(() -> choice(
            this.let_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_stmt,
            this.return_stmt
    ));

    /*public rule fct_statements = lazy(() -> choice(
            this.let_decl,
            this.if_stmt,
            this.while_stmt,
            this.expression_stmt
    ));*/

    public rule brace_statement =
            seq(LBRACE, block_statements.at_least(0), RBRACE)
                    .as_list(StatementNode.class).push($ -> new BlockNode($.span(), $.$[0]));

    public rule let_decl = lazy(() ->
            seq(_let, identifier, AS, choice(expression, paren_expression))
                    .push($ -> new LetDeclarationNode($.span(), $.$[0], $.$[1]))
    );

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
            block_statements.at_least(0)
                    .as_list(StatementNode.class).push($ -> new BlockNode($.span(), $.$[0]));

    public rule arg_type =
            seq(COLON, reference);

    public rule fct_decl_arg =
            seq(identifier, arg_type.or_push_null())
                    .push($ -> new ParameterNode($.span(), $.$[0], $.$[1]));

    public rule fct_decl_args_list =
            fct_decl_arg.sep(0, COMMA)
                    .as_list(ParameterNode.class);

    public rule fct_decl = lazy(() ->
            seq(_def, ws, identifier,
                    LPAREN, fct_decl_args_list, RPAREN,
                    LBRACE, fct_block, RBRACE)
                    .push($ -> new FctDeclarationNode($.span(), $.$[0], $.$[1], $.$[2]))
    );

    /*public rule fct_decl = lazy(() ->
            seq(_def, ws, identifier,
                    LPAREN, fct_decl_args_list, RPAREN,
                    LBRACE, fct_block, return_stmt, RBRACE)
                    .push($ -> new FctDeclarationNode($.span(), $.$[0], $.$[1], $.$[2], $.$[3]))
    );

    public rule main_stmt = lazy(() ->
            seq(_main,
                    LPAREN, fct_decl_args_list, RPAREN,
                    LBRACE, fct_block, RBRACE)
                    .push($ -> new FctDeclarationNode($.span(), $.$[0], $.$[1], $.$[2], new ReturnNode($.span(), null)))
    );*/

    public rule main_stmt =
            seq(_main, LBRACE, expression.or_push_null(), RBRACE)
                    .push($ -> new ReturnNode($.span(), $.$[0]));

    public rule fct_call_args = lazy(() ->
            seq(LPAREN, expressions, RPAREN));

    public rule struct_decl_attribute =
            seq(identifier, AS, expression)
                    .push($ -> new AttributeDeclarationNode($.span(), $.$[0], $.$[1])
    );

    public rule struct_decl = lazy(() ->
            seq(_struct, ws, identifier, ws.opt(), LBRACE, struct_decl_attribute.at_least(1).as_list(DeclarationNode.class), RBRACE)
                    .push($ -> new StructDeclarationNode($.span(), $.$[0], $.$[1]))
    );

    public rule root =
            seq(ws, statement.at_least(1))
                    .as_list(StatementNode.class)
                    .push($ -> new RootNode($.span(), $.$[0]));

    @Override
    public rule root() {
        return root;
    }
}