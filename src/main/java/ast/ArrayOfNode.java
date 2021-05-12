package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayOfNode extends ExpressionNode
{
    public final ExpressionNode initializer;
    public final ExpressionNode length;

    public ArrayOfNode(Span span, Object initializer, Object length) {
        super(span);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        this.length = Util.cast(length, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("arrayOf(%s:%s)", initializer.contents(), length.contents());
    }

    @Override
    public String getType() {
        return initializer.getType();
    }
}