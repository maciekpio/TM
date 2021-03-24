package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class VarDeclarationNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;
    public final ExpressionNode initializer;

    public VarDeclarationNode (Span span, Object name, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        System.out.println("VarDeclarationNode");
        System.out.println(initializer.getClass().toString());
        /*
          Find the type of the value used in the variable
          this.type = Util.cast(type, TypeNode.class);
         */
        this.type = Util.cast(initializer.getClass(), TypeNode.class);
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
