package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayMapPutNode extends ExpressionNode
{
    public final ExpressionNode object_valuePut;
    public final ExpressionNode index_key;
    public final ExpressionNode array_map;

    public ArrayMapPutNode(Span span, Object array_map, Object index_key, Object object_valuePut) {
        super(span);
        this.object_valuePut = Util.cast(object_valuePut, ExpressionNode.class);
        this.index_key = Util.cast(index_key, ExpressionNode.class);
        this.array_map = Util.cast(array_map, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format(".put %s at %s", object_valuePut.contents(), index_key.contents());
    }

    @Override
    public String getType() {
        return "Bool";
    }
}