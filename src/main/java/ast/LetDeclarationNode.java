package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

import static utils_static.UtilStatic.*;

public final class LetDeclarationNode extends DeclarationNode
{
    public final String name;
    public TypeNode type;
    public final ExpressionNode initializer;
    public final Boolean pinned;

    public LetDeclarationNode(Span span, Object name, Object initializer, Boolean pinned) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        this.type = UtilStatic.whichTypeIs(span, this.initializer);
        surePut(this.name, this.type.contents());
        this.pinned = pinned;
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return String.format("let (%s) %s = %s", type.contents(), name, initializer.contents());
    }

    @Override public String declaredThing () {
        return "variable";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LetDeclarationNode that = (LetDeclarationNode) o;
        return name.equals(that.name) &&
                type.equals(that.type) &&
                initializer.equals(that.initializer);
    }

    public void setType(String strType){
        SimpleTypeNode simpleTypeNode = new SimpleTypeNode(this.type.span, strType);
        this.type = Util.cast(simpleTypeNode, TypeNode.class);
    }

    @Override
    public String getType() {
        return type.contents();
    }
}
