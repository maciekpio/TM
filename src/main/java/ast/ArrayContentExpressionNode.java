package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayContentExpressionNode extends ExpressionNode
{
    public final ExpressionNode array_content;

    public ArrayContentExpressionNode(Span span, Object array_content) {
        super(span);
        this.array_content = Util.cast(array_content, ExpressionNode.class);
    }

    @Override public String contents () {
        return "array_content " + array_content;
    }
}