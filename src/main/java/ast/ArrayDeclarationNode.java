package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import static utils_static.UtilStatic.typesMap;

//TODO no more supported --> use VarDeclarationNode instead
public final class ArrayDeclarationNode extends DeclarationNode
{
    public final String name;
    public final ArrayLiteralNode initializer;
    public TypeNode type;

    public ArrayDeclarationNode (Span span, Object name, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.initializer = Util.cast(initializer, ArrayLiteralNode.class);
        SimpleTypeNode initSimpleType = new SimpleTypeNode(span, this.initializer.getType());
        this.type = new ArrayTypeNode(span, initSimpleType);

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