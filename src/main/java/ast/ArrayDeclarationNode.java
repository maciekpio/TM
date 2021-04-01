package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayDeclarationNode extends DeclarationNode
{
    public final String name;
    public final ArrayLengthNode array_length;

    public ArrayDeclarationNode (Span span, Object name, Object array_length) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.array_length = Util.cast(array_length, ArrayLengthNode.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "array " + name;
    }

    @Override public String declaredThing () {
        return "array";
    }
}