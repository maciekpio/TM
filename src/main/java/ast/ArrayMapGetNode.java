package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayMapGetNode extends ExpressionNode
{
    public final ExpressionNode index_key;
    public final ExpressionNode array_map;

    public ArrayMapGetNode(Span span, Object array_map, Object index_key) {
        super(span);
        this.index_key = Util.cast(index_key, ExpressionNode.class);
        this.array_map = Util.cast(array_map, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("%s.get(%s)", array_map.contents(), index_key.contents());
    }

    @Override
    public String getType() {
        return array_map.getType();
    }
}