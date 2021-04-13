package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class StructDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<AttributeDeclarationNode> attributes;

    @SuppressWarnings("unchecked")
    public StructDeclarationNode (Span span, Object name, Object attributes) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.attributes = Util.cast(attributes, List.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return String.format("struct %s {%s}", name, attributes.toString());
    }

    @Override public String declaredThing () {
        return "struct";
    }
}
