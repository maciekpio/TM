package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class MapEntryNode extends ExpressionNode
{
    public final ExpressionNode key;
    public final ExpressionNode value;

    public MapEntryNode (Span span, Object key, Object value){
        super(span);
        this.key = Util.cast(key, ExpressionNode.class);
        this.value = Util.cast(value, ExpressionNode.class);
    }

    @Override
    public String contents() {
        return String.format("%s:%s", key.contents(), value.contents());
    }

    @Override
    public String getType() {
        return value.getType();
    }
}
