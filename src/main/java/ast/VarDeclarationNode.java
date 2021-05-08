package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import static utils_static.UtilStatic.*;

public final class VarDeclarationNode extends DeclarationNode
{
    public final String name;
    public TypeNode type;
    public final ExpressionNode initializer;

    public VarDeclarationNode (Span span, Object name, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        this.type = whichTypeIs(span, this.initializer);
        typesMap.put(this.name, this.type.contents());
        System.out.printf("The current map is %s%n", typesMap.toString());
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
        VarDeclarationNode that = (VarDeclarationNode) o;
        return name.equals(that.name) &&
                type.equals(that.type) &&
                initializer.equals(that.initializer);
    }

    public void setType(String strType){
        System.out.printf("The type \"%s\" was changed to \"%s\"%n", type.contents(), strType);
        SimpleTypeNode simpleTypeNode = new SimpleTypeNode(this.type.span, strType);
        this.type = Util.cast(simpleTypeNode, TypeNode.class);
    }

    @Override
    public String getType() {
        return type.contents();
    }
}
