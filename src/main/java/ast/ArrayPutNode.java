package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayPutNode extends ExpressionNode
{
    public final ExpressionNode objectPut;
    public final ExpressionNode index;
    public final ExpressionNode array;

    public ArrayPutNode(Span span, Object array, Object objectPut, Object index) {
        super(span);
        this.objectPut = Util.cast(objectPut, ExpressionNode.class);
        this.index = Util.cast(index, ExpressionNode.class);
        this.array = Util.cast(array, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format(".put %s at %s", objectPut.contents(), index.contents());
    }

    @Override
    public String getType() {
        return "array.put";//TODO
    }
}