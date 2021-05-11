package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

public final class ParameterNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;

    public ParameterNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = Util.cast(new SimpleTypeNode(span, "NotYet"), TypeNode.class);
        UtilStatic.typesMap.put(this.name, "NotYet");
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

    @Override
    public String getType() {
        return type.contents();
    }
}
