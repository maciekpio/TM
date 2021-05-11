package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

public final class AttributeDeclarationNode extends DeclarationNode
{
    public final String name;
    public final ExpressionNode initializer;
    public TypeNode type;
    public String structName;

    public AttributeDeclarationNode(Span span, Object name, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        this.type = UtilStatic.whichTypeNodeIs(span, this.initializer);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return String.format("attr (%s) %s", this.getType(), name);
    }

    @Override public String declaredThing () {
        return "attribute";
    }

    @Override
    public String getType() {
        return this.type.contents();
    }
}