package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ParameterNode extends DeclarationNode
{
    public final String name;

    public ParameterNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return name;
    }

    @Override public String declaredThing () {
        return "parameter";
    }
}
