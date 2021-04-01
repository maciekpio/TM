package interpreter;

import ast.*;
import scopes.DeclarationKind;
import scopes.RootScope;
import scopes.Scope;
import scopes.SyntheticDeclarationNode;
import types.FloatType;
import types.IntType;
import types.StringType;
import types.Type;
import norswap.uranium.Reactor;
import norswap.utils.Util;
import norswap.utils.exceptions.NoStackException;
import norswap.utils.visitors.ValuedVisitor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.coIterate;
import static norswap.utils.Vanilla.map;

/**
 * Implements a simple but inefficient interpreter for Sigh.
 *
 * <p>Runtime value representation:
 * <ul>
 *     <li>{@code Int}, {@code Float}, {@code Bool}: {@link Long}, {@link Double}, {@link Boolean}</li>
 *     <li>{@code String}: {@link String}</li>
 *     <li>{@code null}: {@link Null#INSTANCE}</li>
 *     <li>Arrays: {@code Object[]}</li>
 *     <li>Structs: {@code HashMap<String, Object>}</li>
 *     <li>Functions: the corresponding {@link DeclarationNode} ({@link FctDeclarationNode} or
 *     {@link SyntheticDeclarationNode}), excepted structure constructors, which are
 *     represented by {@link Constructor}</li>
 *     <li>Types: the corresponding {@link StructDeclarationNode}</li>
 * </ul>
 */
public final class Interpreter
{
    // ---------------------------------------------------------------------------------------------

    private final ValuedVisitor<SighNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;
    private Frame frame = null;

    // ---------------------------------------------------------------------------------------------

    public Interpreter (Reactor reactor) {
        this.reactor = reactor;

        // expressions
        visitor.register(IntLiteralNode.class,           this::intLiteral);
        visitor.register(StringLiteralNode.class,        this::stringLiteral);
        visitor.register(ReferenceNode.class,            this::reference);
        visitor.register(ConstructorNode.class,          this::constructor);
        visitor.register(ArrayLiteralNode.class,         this::arrayLiteral);
        visitor.register(ParenthesizedNode.class,        this::parenthesized);
        visitor.register(AttributeAccessNode.class,      this::attrAccess);
        visitor.register(ArrayPullNode.class,          this::arrayAccess);
        visitor.register(FctCallNode.class,              this::fctCall);
        visitor.register(UnaryExpressionNode.class,      this::unaryExpression);
        visitor.register(BinaryExpressionNode.class,     this::binaryExpression);
        visitor.register(AssignmentNode.class,           this::assignment);

        // statement groups & declarations
        visitor.register(RootNode.class,                 this::root);
        visitor.register(BlockNode.class,                this::block);
        visitor.register(VarDeclarationNode.class,       this::varDecl);
        // no need to visitor other declarations! (use fallback)

        // statements
        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
        visitor.register(IfNode.class,                   this::ifStmt);
        visitor.register(WhileNode.class,                this::whileStmt);
        visitor.register(ReturnNode.class,               this::returnStmt);

        visitor.registerFallback(node -> null);
    }

    // ---------------------------------------------------------------------------------------------

    public Object run (SighNode node) {
        return visitor.apply(node);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Used to implement the control flow of the return statement.
     */
    private static class Return extends NoStackException {
        final Object value;
        private Return (Object value) {
            this.value = value;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private <T> T get(SighNode node) {
        return cast(visitor.apply(node));
    }

    // ---------------------------------------------------------------------------------------------

    private Long intLiteral (IntLiteralNode node) {
        return node.value;
    }

    private String stringLiteral (StringLiteralNode node) {
        return node.value;
    }

    // ---------------------------------------------------------------------------------------------

    private Object parenthesized (ParenthesizedNode node) {
        return get(node.expression);
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] arrayLiteral (ArrayLiteralNode node) {
        return map(node.components, new Object[0], visitor);
    }

    // ---------------------------------------------------------------------------------------------

    private Object binaryExpression (BinaryExpressionNode node)
    {
        Type leftType  = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");

        // Cases where both operands should not be evaluated.
        switch (node.operator) {
            case OR:  return booleanOp(node, false);
            case AND: return booleanOp(node, true);
        }

        Object left  = get(node.left);
        Object right = get(node.right);

        if (node.operator == BinaryOperator.PLUS
                && (leftType instanceof StringType || rightType instanceof StringType))
            return convertToString(left) + convertToString(right);

        boolean floating = leftType instanceof FloatType || rightType instanceof FloatType;
        boolean numeric  = floating || leftType instanceof IntType;

        if (numeric)
            return numericOp(node, floating, (Number) left, (Number) right);

        switch (node.operator) {
            case EQUAL:      return  left == right;
            case DIFF:    return  left != right;
        }

        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private boolean booleanOp (BinaryExpressionNode node, boolean isAnd)
    {
        boolean left = get(node.left);
        return isAnd
                ? left && (boolean) get(node.right)
                : left || (boolean) get(node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private Object numericOp
            (BinaryExpressionNode node, boolean floating, Number left, Number right)
    {
        long ileft, iright;
        double fleft, fright;

        if (floating) {
            fleft  = left.doubleValue();
            fright = right.doubleValue();
            ileft = iright = 0;
        } else {
            ileft  = left.longValue();
            iright = right.longValue();
            fleft = fright = 0;
        }

        Object result;
        if (floating)
            switch (node.operator) {
                case TIMES:      return fleft *  fright;
                case DIVID:        return fleft /  fright;
                case MODULO:     return fleft %  fright;
                case PLUS:           return fleft +  fright;
                case MINUS:      return fleft -  fright;
                case GREATER:       return fleft >  fright;
                case LOWER:         return fleft <  fright;
                case GREATER_EQUAL: return fleft >= fright;
                case LOWER_EQUAL:   return fleft <= fright;
                case EQUAL:      return fleft == fright;
                case DIFF:    return fleft != fright;
                default:
                    throw new Error("should not reach here");
            }
        else
            switch (node.operator) {
                case TIMES:      return ileft *  iright;
                case DIVID:        return ileft /  iright;
                case MODULO:     return ileft %  iright;
                case PLUS:           return ileft +  iright;
                case MINUS:      return ileft -  iright;
                case GREATER:       return ileft >  iright;
                case LOWER:         return ileft <  iright;
                case GREATER_EQUAL: return ileft >= iright;
                case LOWER_EQUAL:   return ileft <= iright;
                case EQUAL:      return ileft == iright;
                case DIFF:    return ileft != iright;
                default:
                    throw new Error("should not reach here");
            }
    }

    // ---------------------------------------------------------------------------------------------

    public Object assignment (AssignmentNode node)
    {
        if (node.left instanceof ReferenceNode) {
            Scope scope = reactor.get(node.left, "scope");
            String name = ((ReferenceNode) node.left).name;
            Object rvalue = get(node.right);
            frame.set(scope, name, rvalue);
            return rvalue;
        }

        if (node.left instanceof ArrayPullNode) {
            ArrayPullNode arrayAccess = (ArrayPullNode) node.left;
            Object[] array = getNonNullArray(arrayAccess.array);
            int index = getIndex(arrayAccess.index);
            return array[index] = get(node.right);
        }

        if (node.left instanceof AttributeAccessNode) {
            AttributeAccessNode attrAccess = (AttributeAccessNode) node.left;
            Object object = get(attrAccess.stem);
            if (object == Null.INSTANCE)
                throw new NullPointerException("accessing field of null object");
            Map<String, Object> struct = cast(object);
            Object right = get(node.right);
            struct.put(attrAccess.attrName, right);
            return right;
        }

        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private int getIndex (ExpressionNode node)
    {
        long index = get(node);
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException("Negative index: " + index);
        if (index >= Integer.MAX_VALUE - 1)
            throw new ArrayIndexOutOfBoundsException("Index exceeds max array index (2ˆ31 - 2): " + index);
        return (int) index;
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] getNonNullArray (ExpressionNode node)
    {
        Object object = get(node);
        if (object == Null.INSTANCE)
            throw new NullPointerException("indexing null array");
        return (Object[]) object;
    }

    // ---------------------------------------------------------------------------------------------

    private Object unaryExpression (UnaryExpressionNode node)
    {
        // there is only NOT
        return ! (boolean) get(node.operand);
    }

    // ---------------------------------------------------------------------------------------------

    private Object arrayAccess (ArrayPullNode node)
    {
        Object[] array = getNonNullArray(node.array);
        return array[getIndex(node.index)];
    }

    // ---------------------------------------------------------------------------------------------

    private Object root (RootNode node)
    {
        assert frame == null;
        RootScope root = reactor.get(node, "scope");
        frame = new Frame(root, null);
        frame.initRoot(root);

        try {
            node.statements.forEach(visitor::apply);
        } catch (Return r) {
            return r.value;
            // allow returning from the main script
        } finally {
            frame = null;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void block (BlockNode node) {
        Scope scope = reactor.get(node, "scope");
        frame = new Frame(scope, frame);
        node.statements.forEach(visitor::apply);
        frame = frame.parent;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Constructor constructor (ConstructorNode node) {
        // guaranteed safe by semantic analysis
        return new Constructor(get(node.ref));
    }

    // ---------------------------------------------------------------------------------------------

    private Object expressionStmt (ExpressionStatementNode node) {
        get(node.expression);
        return null;  // discard value
    }

    // ---------------------------------------------------------------------------------------------

    private Object attrAccess(AttributeAccessNode node)
    {
        Object stem = get(node.stem);
        if (stem == Null.INSTANCE)
            throw new NullPointerException("accessing field of null object");
        return stem instanceof Map
                ? Util.<Map<String, Object>>cast(stem).get(node.attrName)
                : (long) ((Object[]) stem).length; // only field on arrays
    }

    // ---------------------------------------------------------------------------------------------

    private Object fctCall (FctCallNode node)
    {
        Object decl = get(node.function);
        node.arguments.forEach(visitor::apply);
        Object[] args = map(node.arguments, new Object[0], visitor);

        if (decl == Null.INSTANCE)
            throw new NullPointerException("calling a null function");

        if (decl instanceof SyntheticDeclarationNode)
            return builtin(((SyntheticDeclarationNode) decl).name(), args);

        if (decl instanceof Constructor)
            return buildStruct(((Constructor) decl).declaration, args);

        Scope scope = reactor.get(decl, "scope");
        frame = new Frame(scope, frame);

        FctDeclarationNode funDecl = (FctDeclarationNode) decl;
        coIterate(args, funDecl.parameters,
                (arg, param) -> frame.set(scope, param.name, arg));

        try {
            get(funDecl.block);
        } catch (Return r) {
            return r.value;
        } finally {
            frame = frame.parent;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object builtin (String name, Object[] args)
    {
        assert name.equals("print"); // only one at the moment
        String out = convertToString(args[0]);
        System.out.println(out);
        return out;
    }

    // ---------------------------------------------------------------------------------------------

    private String convertToString (Object arg)
    {
        if (arg == Null.INSTANCE)
            return "null";
        else if (arg instanceof Object[])
            return Arrays.toString((Object[]) arg);
        else if (arg instanceof FctDeclarationNode)
            return ((FctDeclarationNode) arg).name;
        else if (arg instanceof StructDeclarationNode)
            return ((StructDeclarationNode) arg).name;
        else if (arg instanceof Constructor)
            return "$" + ((Constructor) arg).declaration.name;
        else
            return arg.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private HashMap<String, Object> buildStruct (StructDeclarationNode node, Object[] args)
    {
        HashMap<String, Object> struct = new HashMap<>();
        for (int i = 0; i < node.attributes.size(); ++i)
            struct.put(node.attributes.get(i).name, args[i]);
        return struct;
    }

    // ---------------------------------------------------------------------------------------------

    private Void ifStmt (IfNode node)
    {
        if (get(node.condition))
            get(node.trueStatement);
        else if (node.falseStatement != null)
            get(node.falseStatement);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void whileStmt (WhileNode node)
    {
        while (get(node.condition))
            get(node.block);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object reference (ReferenceNode node)
    {
        Scope scope = reactor.get(node, "scope");
        DeclarationNode decl = reactor.get(node, "decl");

        if (decl instanceof VarDeclarationNode
        || decl instanceof ParameterNode
        || decl instanceof SyntheticDeclarationNode
                && ((SyntheticDeclarationNode) decl).kind() == DeclarationKind.VARIABLE)
            return frame.get(scope, node.name);

        return decl; // structure or function
    }

    // ---------------------------------------------------------------------------------------------

    private Void returnStmt (ReturnNode node) {
        throw new Return(node.expression == null ? null : get(node.expression));
    }

    // ---------------------------------------------------------------------------------------------

    private Void varDecl (VarDeclarationNode node)
    {
        Scope scope = reactor.get(node, "scope");
        frame.set(scope, node.name, get(node.initializer));
        return null;
    }

    // ---------------------------------------------------------------------------------------------
}
