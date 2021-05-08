package utils_static;

import ast.*;
import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.HashMap;

public class UtilStatic {

    public static HashMap<String, String> typesMap = new HashMap<>(){{
        put("print", "Void");
        put("rprint", "Void");
    }};

    public static TypeNode whichTypeIs(Span span, ExpressionNode thatExpression)
    {
        /*If "thatExpression" is the return () of a function, the type's function is void*/
        if (thatExpression==null) return new SimpleTypeNode(span, "Void");

        TypeNode type;
        boolean isAnArray = (thatExpression instanceof ArrayLiteralNode);
        SimpleTypeNode simpleTypeNode;

        simpleTypeNode = new SimpleTypeNode(span, thatExpression.getType());
        if(isAnArray){
            type = Util.cast(new ArrayTypeNode(span, simpleTypeNode), TypeNode.class);
        }
        else {
            type = Util.cast(simpleTypeNode, TypeNode.class);
        }
        return type;
    }
}
