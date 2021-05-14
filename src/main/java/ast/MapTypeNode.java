package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class MapTypeNode extends TypeNode
{
    public final TypeNode componentType;

    public MapTypeNode (Span span, Object componentType) {
        super(span);
        this.componentType = Util.cast(componentType, TypeNode.class);
    }

    @Override public String contents() {
        return componentType.contents() + "{}";
    }
}