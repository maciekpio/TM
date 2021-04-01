package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayLengthNode extends ExpressionNode
{
    public final ExpressionNode array_length;

    public ArrayLengthNode (Span span, Object array_length) {
        super(span);
        this.array_length = Util.cast(array_length, ExpressionNode.class);
    }

    @Override public String contents () {
        return "array_length " + array_length;
    }
}