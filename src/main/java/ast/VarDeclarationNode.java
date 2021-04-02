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

        /*
          Find the type of the value used in the variable
          this.type = Util.cast(type, TypeNode.class);
         */
        Class typeClass = initializer.getClass();
        String typeName;
        if(typeClass.equals(IntLiteralNode.class)){
            typeName = "Integer";
        }else if (typeClass.equals(FloatLiteralNode.class)){
            typeName = "Float";
        }else if (typeClass.equals(StringLiteralNode.class)){
            typeName = "String";
        }else {
            typeName = "None";
            System.out.println("ERROR, typeName == None\n" +
                    "Context : " + VarDeclarationNode.class.toString());
        }

        SimpleTypeNode simpleTypeNode = new SimpleTypeNode(span, typeName);
        this.type = Util.cast(simpleTypeNode, TypeNode.class);

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
