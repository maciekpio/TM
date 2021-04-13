package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;


import static ast.BinaryOperator.*;
import java.util.Objects;

public final class VarDeclarationNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;
    public final ExpressionNode initializer;

    public VarDeclarationNode (Span span, Object name, Object initializer) {
        super(span);

        if (name.getClass().equals(String.class)){
            this.name = Util.cast(name, String.class);
        }
        else {
            ReferenceNode referenceNode = (ReferenceNode) name;
            this.name = Util.cast(referenceNode.name, String.class);
        }

        this.initializer = Util.cast(initializer, ExpressionNode.class);

        String typeName = this.initializer.getType();
        SimpleTypeNode simpleTypeNode;
        if(typeName.equals("Int") || typeName.equals("Float") || typeName.equals("Bool") || typeName.equals("String") || typeName.equals("Type")){
            simpleTypeNode = new SimpleTypeNode(span, typeName);
        }
        else {
            simpleTypeNode = new SimpleTypeNode(span, "None");
        }
        this.type = Util.cast(simpleTypeNode, TypeNode.class);
        /*if(typeClass.equals(IntLiteralNode.class)){
            typeName = "Int";
        }else if (typeClass.equals(FloatLiteralNode.class)) {
            typeName = "Float";
        }else if (typeClass.equals(BooleanNode.class)){
                typeName = "Bool";
        }else if (typeClass.equals(StringLiteralNode.class)){
            typeName = "String";
        }else if (typeClass.isAssignableFrom(BinaryExpressionNode.class)){
            //BinaryExpressionNode binaryExpr = (BinaryExpressionNode) initializer;
            typeName = "Expression";
            System.out.println("TODO"); //TODO
        }
        else {
            typeName = "None";
            System.out.println("ERROR, typeName == None\n" +
                    "Context : " + VarDeclarationNode.class.toString());
        }*/
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
}
