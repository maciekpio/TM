package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayGetNode extends ExpressionNode
{
    public final ExpressionNode index;
    public final ExpressionNode array;

    public ArrayGetNode(Span span, Object array, Object index) {
        super(span);
        this.index = Util.cast(index, ExpressionNode.class);
        this.array = Util.cast(array, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format(".get(%s)", index.contents());
    }

    @Override
    public String getType() {
        return "array.get";//TODO
    }
}