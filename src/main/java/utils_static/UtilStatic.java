package utils_static;

import ast.*;
import com.sun.jdi.BooleanType;
import interpreter.PassthroughException;
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
    }};

    public static HashMap<String, String> structValuesMap = new HashMap<>();

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
        boolean isAnArray = (isInstanceOf(thatExpression, ArrayLiteralNode.class, ArrayOfNode.class));

        SimpleTypeNode simpleTypeNode = new SimpleTypeNode(span, thatExpression.getType());
        if(isAnArray){
            type = Util.cast(new ArrayTypeNode(span, simpleTypeNode), TypeNode.class);
        }
        else {
            type = Util.cast(simpleTypeNode, TypeNode.class);
        }
        return type;
    }

    public static Type whichTypeIs(Object literal)
    {
        String str = literal.getClass().toString();
        System.out.println("whichTypeIs : " + str);
        switch (literal.getClass().toString()){
            case "class java.lang.Long": return IntType.INSTANCE;
            case "class java.lang.Double": return FloatType.INSTANCE;
            case "class java.lang.String": return StringType.INSTANCE;
            case "class java.lang.Boolean": return BoolType.INSTANCE;
            //case "class [Ljava.lang.Object": return new ArrayType(NotYetType.INSTANCE);
        }
        throw new PassthroughException(new Throwable("whichTypeIs function only supports Long, Double, String and Boolean types"));
    }

    /**Works with numbers*/
    public static boolean isArithmetic (BinaryOperator op) {
        return op == PLUS || op == TIMES || op == MINUS || op == DIVID || op == MODULO;
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
