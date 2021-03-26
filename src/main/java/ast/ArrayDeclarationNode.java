package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.List;

public final class ArrayDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ExpressionNode> array_init;

    public ArrayDeclarationNode (Span span, Object name, Object array_init) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.array_init = Util.cast(array_init, List.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "var " + name;
    }

    @Override public String declaredThing () {
        return "variable";
    }
}