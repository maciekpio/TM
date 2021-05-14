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
        put("args", "String");
    }};

    public static HashMap<String, Boolean> pinnedMap = new HashMap<>();

    /**
     * Ensure that the put function does not erase the wrong key in {@typesMap}
     * If the key is already, then NotYet is put instead.
     * And so the value will be checked during the interpreter phase and NOT during the semantic.
     * @param key as the key in the put function.
     * @param value as the value in the put function.
     */
    public static void surePut(String key, String value){
        if (value.equals("NotYet")){
            typesMap.put(key, "NotYet");
            return;
        }
        String getValue = typesMap.get(key);
        if (getValue != null && !getValue.equals(value))
            typesMap.put(key, "NotYet");
        else
            typesMap.put(key, value);
    }

    /**
     * Used in context to fill typesMap with the type of different nodes during the AST construction.
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

    /**
     * Used in context to know the real type of an {@link NotYetType} during the interpreter phase.
     * @param expr is any expression and the result of a get(expr) by the interpreter.
     * @return a {@link Type} that represents the type of the expression.
     */
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

    /**
     * Only use by whichTypeIs(Object expr) to know the component type of the array/map.
     * Also check any type anomalies.
     * @param expressions
     * @return
     */
    public static Type whichArrayTypeIs(Object[] expressions){
        Type firstType = whichTypeIs(expressions[0]);
        for (Object expr : expressions){
            Type exprType = whichTypeIs(expr);
            if (!exprType.equals(firstType)){
                if (firstType.name().equals("Int") && exprType.name().equals("Float")){
                    firstType = FloatType.INSTANCE;
                } else {
                    throw new PassthroughException(new Throwable(String.format("An array/map can not have incompatibles types as %s and %s", firstType.toString(), exprType.toString())));
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

    /**Works with numbers xor booleans*/
    public static boolean isEquality (BinaryOperator op) {
        return op == EQUAL || op == DIFF;
    }

    /**@return true if {@param o} is one of the instances of all the class types in {@param types}.*/
    public static boolean isInstanceOf (Object o, @SuppressWarnings("rawtypes") Class... types){
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
