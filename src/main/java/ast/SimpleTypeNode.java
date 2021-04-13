package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.Objects;

public final class SimpleTypeNode extends TypeNode
{
    public final String name;

    public SimpleTypeNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override public String contents () {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTypeNode that = (SimpleTypeNode) o;
        return this.name.equals(that.name);
    }
}
