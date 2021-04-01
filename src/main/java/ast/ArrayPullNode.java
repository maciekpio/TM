package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayPullNode extends ExpressionNode
{
    public final ExpressionNode array;
    public final ExpressionNode index;

    public ArrayPullNode(Span span, Object array, Object index) {
        super(span);
        this.array = Util.cast(array, ExpressionNode.class);
        this.index = Util.cast(index, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("%s[%s]", array.contents(), index.contents());
    }
}