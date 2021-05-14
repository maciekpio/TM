package utils_static;

import ast.*;
import interpreter.PassthroughException;
import interpreter.Void;
import norswap.autumn.positions.Span;
import norswap.utils.Util;
import types.*;

import java.util.HashMap;

import static ast.BinaryOperator.*;

public class UtilStatic {

    /**
     * @typesMap is a preview Map of the types used during the construction of the AST.
     */
    public static HashMap<String, String> typesMap = new HashMap<>(){{
        put("print", "Void");
        put("rprint", "Void");
        put("parseInt", "Int");
    }};

    public static void surePut(String key, String value){
        String getValue = typesMap.get(key);
        if (getValue != null && !getValue.equals(value))
            typesMap.put(key, "NotYet");
        else
            typesMap.put(key, value);
    }

    /**
     * Used in context to fill typesMap with the type of different nodes.
     * @param thatSpan is the span of that expression.
     * @param thatExpression is any expression.
     * @return a {@link TypeNode} that represents the type of the expression.
     */
    public static TypeNode whichTypeIs(Span thatSpan, ExpressionNode thatExpression)
    {
        Span span = new Span(thatSpan.start, thatSpan.start);
        /*If "thatExpression" is the return () of a function, the type's function is void*/
        if (thatExpression==null) return new SimpleTypeNode(span, "Void");

        TypeNode type;
        SimpleTypeNode simpleTypeNode = new SimpleTypeNode(span, thatExpression.getType());

        if(isInstanceOf(thatExpression, ArrayLiteralNode.class, ArrayOfNode.class))
            type = Util.cast(new ArrayTypeNode(span, simpleTypeNode), TypeNode.class);
        else if(thatExpression instanceof MapLiteralNode)
            type = Util.cast(new MapTypeNode(span, simpleTypeNode), TypeNode.class);
        else
            type = Util.cast(simpleTypeNode, TypeNode.class);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static Type whichTypeIs(Object expr)
    {
        if(expr instanceof Long)      return    IntType.INSTANCE;
        if(expr instanceof Integer)   return    IntType.INSTANCE;
        if(expr instanceof Double)    return  FloatType.INSTANCE;
        if(expr instanceof Boolean)   return   BoolType.INSTANCE;
        if(expr instanceof String)    return StringType.INSTANCE;
        if(expr instanceof Void)      return   VoidType.INSTANCE;
        if(expr instanceof Object[])  return new ArrayType(whichArrayTypeIs((Object[]) expr));
        if(expr instanceof HashMap)   return new MapType(whichArrayTypeIs(((HashMap<String, Object>) expr).values().toArray()));

        throw new PassthroughException(new Throwable("whichTypeIs function only supports Long, Double, String and Boolean types"));
    }

    public static Type whichArrayTypeIs(Object[] literals){
        Type firstType = whichTypeIs(literals[0]);
        for (Object literal : literals){
            Type litType = whichTypeIs(literal);
            if (!litType.equals(firstType)){
                if (firstType.name().equals("Int") && litType.name().equals("Float")){
                    firstType = FloatType.INSTANCE;
                } else {
                    throw new PassthroughException(new Throwable(String.format("An array/map can not have incompatibles types as %s and %s", firstType.toString(), litType.toString())));
                }
            }
        }
        return firstType;
    }

    /**Works with numbers*/
    public static boolean isArithmetic (BinaryOperator op) {
        return op == PLUS || op == TIMES || op == MINUS || op == DIVID || op == MODULO
                || op == POW;
    }

    /**Works with numbers*/
    public static boolean isComparison (BinaryOperator op) {
        return op == GREATER || op == GREATER_EQUAL || op == LOWER || op == LOWER_EQUAL;
    }

    /**Works with booleans*/
    public static boolean isLogic (BinaryOperator op) {
        return op == OR || op == AND;
    }

    /**Works with numbers and booleans*/
    public static boolean isEquality (BinaryOperator op) {
        return op == EQUAL || op == DIFF;
    }

    /**@return true if {@param o} is one of the instances of all the class types in {@param types}.*/
    public static boolean isInstanceOf (Object o, Class... types){
        boolean b;
        for (Object type : types){
            b = (o.getClass() == type);
            if(b) return true;
        }
        return false;
    }

    /**@return true if the object is numeric*/
    public static boolean isNumber (Object o){
        return (isInstanceOf(o, IntType.class, FloatType.class, Long.class, Double.class));
    }

    /**@return true if at least one these object is not yet typed*/
    public static boolean atLeastOneNYT(Object... types){
        boolean b;
        for (Object type : types){
            b = (type instanceof NotYetType);
            if(b) return true;
        }
        return false;
    }
}
