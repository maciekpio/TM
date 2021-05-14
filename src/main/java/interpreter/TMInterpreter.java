package interpreter;

import ast.*;
import scopes.DeclarationKind;
import scopes.RootScope;
import scopes.Scope;
import scopes.SyntheticDeclarationNode;
import types.*;
import norswap.uranium.Reactor;
import norswap.utils.Util;
import norswap.utils.exceptions.Exceptions;
import norswap.utils.exceptions.NoStackException;
import norswap.utils.visitors.ValuedVisitor;

import java.util.*;

import static java.lang.Integer.parseInt;
import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.coIterate;
import static norswap.utils.Vanilla.map;
import static utils_static.UtilStatic.*;

/**
 * Implements a simple but inefficient interpreter for Sigh.
 *
 * <h2>Limitations</h2>
 * <ul>
 *     <li>The compiled code currently doesn't support closures (using variables in functions that
 *     are declared in some surroudning scopes outside the function). The top scope is supported.
 *     </li>
 * </ul>
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
public final class TMInterpreter
{
    // ---------------------------------------------------------------------------------------------

    private final ValuedVisitor<SighNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;
    private ScopeStorage storage = null;
    private RootScope rootScope;
    private ScopeStorage rootStorage;

    // ---------------------------------------------------------------------------------------------

    public TMInterpreter(Reactor reactor) {
        this.reactor = reactor;

        // expressions
        visitor.register(IntLiteralNode.class,           this::intLiteral);
        visitor.register(FloatLiteralNode.class,         this::floatLiteral);
        visitor.register(StringLiteralNode.class,        this::stringLiteral);
        visitor.register(BooleanLiteralNode.class,       this::booleanLiteral);
        visitor.register(ReferenceNode.class,            this::reference);
        visitor.register(ConstructorNode.class,          this::constructor);
        visitor.register(MapLiteralNode.class,           this::mapLiteral);
        visitor.register(ArrayLiteralNode.class,         this::arrayLiteral);
        visitor.register(ArrayOfNode.class,              this::arrayOf);
        visitor.register(ParenthesizedNode.class,        this::parenthesized);
        visitor.register(AttributeAccessNode.class,      this::attrAccess);//TODO prevent errors
        visitor.register(ArrayMapGetNode.class,          this::arrayMapGet);//TODO prevent errors
        visitor.register(ArrayMapPutNode.class,          this::arrayMapPut);//TODO prevent errors
        visitor.register(FctCallNode.class,              this::fctCall);
        visitor.register(UnaryExpressionNode.class,      this::unaryExpression);//TODO prevent errors
        visitor.register(BinaryExpressionNode.class,     this::binaryExpression);
        visitor.register(AssignmentNode.class,           this::assignment);//TODO prevent errors
        visitor.register(StructDeclarationNode.class,    this::structDecl);

        // statement groups & declarations
        visitor.register(RootNode.class,                 this::root);
        visitor.register(BlockNode.class,                this::block);
        visitor.register(LetDeclarationNode.class,       this::letDecl);
        // no need to visitor other declarations! (use fallback)

        // statements
        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
        visitor.register(IfNode.class,                   this::ifStmt);
        visitor.register(WhileNode.class,                this::whileStmt);
        visitor.register(ReturnNode.class,               this::returnStmt);

        visitor.registerFallback(node -> null);
    }

    // ---------------------------------------------------------------------------------------------

    public Object interpret (SighNode root) {
        try {
            return run(root);
        } catch (PassthroughException e) {
            throw Exceptions.runtime(e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object run (SighNode node) {
        try {
            return visitor.apply(node);
        } catch (InterpreterException | Return | PassthroughException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InterpreterException("exception while executing " + node, e);
        }
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
        return cast(run(node));
    }

    // ---------------------------------------------------------------------------------------------

    private Long intLiteral (IntLiteralNode node) {
        return node.value;
    }

    private Double floatLiteral (FloatLiteralNode node) {
        return node.value;
    }

    private String stringLiteral (StringLiteralNode node) {
        return node.value;
    }

    private Boolean booleanLiteral (BooleanLiteralNode node){
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

    private Object[] arrayOf (ArrayOfNode node) {
        Long len;
        try{
            len = get(node.length);
        }
        catch (ClassCastException e) {
            throw new PassthroughException(new Throwable("Initializing an array using a non-int-valued expression as length."));
        }

        Object init = get(node.initializer);
        Object[] map = new Object[Math.toIntExact(len)];
        Arrays.fill(map, init);
        return map;
    }

    // ---------------------------------------------------------------------------------------------

    private Object mapLiteral (MapLiteralNode node) {
        HashMap<String, Object> map = new HashMap<>();
        for (MapEntryNode entry : node.entries){
            String key;
            try {
                key = get(entry.key);
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Trying to use a non-string as key for a map entry"));
            }
            map.put(key, get(entry.value));
        }
        return map;
    }

    // ---------------------------------------------------------------------------------------------

    private Object binaryExpression (BinaryExpressionNode node)
    {
        Type leftType  = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");
        Object left  = get(node.left);
        Object right = get(node.right);

        if (atLeastOneNYT(leftType, rightType)){
            leftType = whichTypeIs(left);
            rightType = whichTypeIs(right);
        }

        boolean stringFormatted = (node.operator == BinaryOperator.PLUS && (leftType instanceof StringType || rightType instanceof StringType));
        if(stringFormatted) return convertToString(left) + convertToString(right);

        String errorCause = String.format("Trying to do : (%s)%s %s (%s)%s", leftType.toString(), left.toString(), node.operator.string, rightType.toString(), right.toString());

        if (isArithmetic(node.operator) || isComparison(node.operator)) {
            if (!isNumber(leftType) || !isNumber(rightType))
                throw new PassthroughException(new Throwable(errorCause));
        } else if (isLogic(node.operator)) {
            if (!(leftType instanceof BoolType && rightType instanceof BoolType))
                throw new PassthroughException(new Throwable(errorCause));
        }

        // Cases where both operands should not be evaluated.
        switch (node.operator) {
            case OR:  return booleanGate(node, false);
            case AND: return booleanGate(node, true);
        }

        /*if (node.operator == BinaryOperator.PLUS
                && (leftType instanceof StringType || rightType instanceof StringType))
            return convertToString(left) + convertToString(right);*/

        boolean floating = leftType instanceof FloatType || rightType instanceof FloatType;
        boolean numeric  = floating || leftType instanceof IntType;

        if (numeric)
            return numericOp(node, floating, (Number) left, (Number) right);

        switch (node.operator) {
            case EQUAL:
                if (leftType instanceof ArrayType && rightType instanceof ArrayType){
                    return Arrays.equals((Object[]) left, (Object[]) right);
                }
                else {
                    return  left.equals(right);
                }
            case DIFF:
                if (leftType instanceof ArrayType && rightType instanceof ArrayType){
                    return !Arrays.equals((Object[]) left, (Object[]) right);
                }
                else {
                    return  !left.equals(right);
                }
        }

        throw new Error("should not reach here");
    }

    /*private boolean isSameArrays(Object left, Object right){
        Object[] leftArray = (Object[]) left;
        Object[] rightArray = (Object[]) right;
        if (leftArray.length != rightArray.length) return false;
        for (int i = 0; i < leftArray.length; i++){
            if(!leftArray[i].equals(rightArray[i])) return false;
        }
        return true;
    }*/

    // ---------------------------------------------------------------------------------------------

    private boolean booleanGate(BinaryExpressionNode node, boolean isAnd)
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


        if (floating)
            switch (node.operator) {
                case TIMES:         return fleft *  fright;
                case DIVID:         return fleft /  fright;
                case MODULO:        return fleft %  fright;
                case PLUS:          return fleft +  fright;
                case MINUS:         return fleft -  fright;
                case GREATER:       return fleft >  fright;
                case LOWER:         return fleft <  fright;
                case GREATER_EQUAL: return fleft >= fright;
                case LOWER_EQUAL:   return fleft <= fright;
                case EQUAL:         return fleft == fright;
                case DIFF:          return fleft != fright;
                case POW:           return Math.pow(fleft,fright);
                default:
                    throw new Error("should not reach here");
            }
        else
            switch (node.operator) {
                case TIMES:         return ileft *  iright;
                case DIVID:         return ileft /  iright;
                case MODULO:        return ileft %  iright;
                case PLUS:          return ileft +  iright;
                case MINUS:         return ileft -  iright;
                case GREATER:       return ileft >  iright;
                case LOWER:         return ileft <  iright;
                case GREATER_EQUAL: return ileft >= iright;
                case LOWER_EQUAL:   return ileft <= iright;
                case EQUAL:         return ileft == iright;
                case DIFF:          return ileft != iright;
                case POW:           return Math.round(Math.pow(ileft,iright));
                default:
                    throw new Error("should not reach here");
            }
    }

    // ---------------------------------------------------------------------------------------------

    public Object assignment (AssignmentNode node)
    {
        Type leftType = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");
        if (atLeastOneNYT(leftType, rightType)){
            leftType  = whichTypeIs(get(node.left));
            rightType = whichTypeIs(get(node.right));
        }
        if (!isAssignableTo(rightType, leftType))
            throw new PassthroughException(new Throwable(String.format("Trying to assign %s to a %s variable", rightType.toString(), leftType.toString())));

        if (node.left instanceof ReferenceNode) {
            Scope scope = reactor.get(node.left, "scope");
            String name = ((ReferenceNode) node.left).name;
            Object rvalue = get(node.right);
            assign(scope, name, rvalue, leftType);
            return rvalue;
        }

        if (node.left instanceof AttributeAccessNode) {
            AttributeAccessNode fieldAccess = (AttributeAccessNode) node.left;
            Object object = get(fieldAccess.parent);
            if (object == Null.INSTANCE)
                throw new PassthroughException(
                    new NullPointerException("accessing field of null object"));
            Map<String, Object> struct = cast(object);
            Object newAttr = get(node.right);
            struct.put(fieldAccess.attrName, newAttr);
            return newAttr;
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

    private Object unaryExpression (UnaryExpressionNode node)
    {
        // there is only NOT
        assert node.operator == UnaryOperator.NOT;
        Object o = get(node.operand);
        try{
            return ! (boolean) o;
        } catch (ClassCastException e){
            throw new PassthroughException(new Throwable(String.format("Trying to inverse an non-boolean object as !%s", whichTypeIs(o).toString())));
        }

    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Object arrayMapGet(ArrayMapGetNode node)
    {
        Object array_map = get(node.array_map);
        if (array_map instanceof HashMap)
        {
            HashMap <String, Object> map;
            String key;
            try {
                map = (HashMap <String, Object>) array_map;
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Reading a non-map object."));
            }
            try {
                key = get(node.index_key);
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Trying to use a non-string as key to read."));
            }
            Object o = map.get(key);
            if (o==null){
                throw new PassthroughException(new NullPointerException("A key is missing in the map."));
            }
            return o;
        }
        else if (array_map instanceof Object[])
        {
            Object[] array;
            int index;
            try {
                array = (Object[]) array_map;
            } catch (ClassCastException e) {
                throw new PassthroughException(new Throwable("Indexing a non-array object."));
            }
            try{
                index = getIndex(node.index_key);
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Indexing with a non-int index."));
            }
            try{
                return array[index];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new PassthroughException(e);
            }
        }

        throw new PassthroughException(new Throwable("Getting on a non-array/non-map object."));
    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Boolean arrayMapPut(ArrayMapPutNode node)
    {
        Object o = get(node.object_valuePut);
        Type arrayMapType = whichTypeIs(get(node.array_map));
        Type putType = whichTypeIs(o);

        if (!isInstanceOf(arrayMapType, ArrayType.class, MapType.class))
            throw new PassthroughException(new Throwable("Putting on a non-array/non-map object."));

        if (arrayMapType instanceof ArrayType)
            arrayMapType = ((ArrayType) arrayMapType).componentType;

        if (arrayMapType instanceof MapType)
            arrayMapType = ((MapType) arrayMapType).componentType;

        if (!arrayMapType.equals(putType))
            throw new PassthroughException(new Throwable(String.format("Trying to put a %s object in an array/map of %s", putType.toString(), arrayMapType.toString())));

        Object array_map = get(node.array_map);

        if (array_map instanceof HashMap)
        {
            HashMap <String, Object> map;
            String key;
            try {
                map = (HashMap <String, Object>) array_map;
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Writing in a non-map object."));
            }
            try {
                key = get(node.index_key);
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Trying to use a non-string as key to read."));
            }

            Object previous = map.put(key, o);
            return previous!=null ? Boolean.FALSE : Boolean.TRUE;
        }
        else if (array_map instanceof Object[])
        {
            Object[] array;
            int index;
            try {
                array = (Object[]) array_map;
            } catch (ClassCastException e) {
                throw new PassthroughException(new Throwable("Indexing a non-array object."));
            }
            try{
                index = getIndex(node.index_key);
            } catch (ClassCastException e){
                throw new PassthroughException(new Throwable("Indexing with a non-int index."));
            }

            try{
                array[index] = o;
                return Boolean.TRUE;
            } catch (ArrayIndexOutOfBoundsException e){
                Arrays.copyOf(array, index+1)[index] = o;
                return Boolean.FALSE;
            }
        }
        throw new PassthroughException(new Throwable("Putting on a non-array/non-map object."));
    }

    // ---------------------------------------------------------------------------------------------

    private Object root (RootNode node)
    {
        assert storage == null;
        rootScope = reactor.get(node, "scope");
        storage = rootStorage = new ScopeStorage(rootScope, null);
        storage.initRoot(rootScope);

        try {
            node.statements.forEach(this::run);
        } catch (Return r) {
            return r.value;
            // allow returning from the main script
        } finally {
            storage = null;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void block (BlockNode node) {
        Scope scope = reactor.get(node, "scope");
        storage = new ScopeStorage(scope, storage);
        node.statements.forEach(this::run);
        storage = storage.parent;
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
        Object stem = get(node.parent);

        if (stem == Null.INSTANCE)
            throw new PassthroughException(
                new NullPointerException("accessing field of null object"));
        return stem instanceof Map
                ? Util.<Map<String, Object>>cast(stem).get(node.attrName)
                : (long) ((Object[]) stem).length; // only field on arrays
    }

    // ---------------------------------------------------------------------------------------------

    private Object fctCall(FctCallNode node)
    {
        Object decl = get(node.function);
        node.arguments.forEach(this::run);
        Object[] args = map(node.arguments, new Object[0], visitor);

        if (decl == Null.INSTANCE)
            throw new PassthroughException(new NullPointerException("calling a null function"));

        if (decl instanceof SyntheticDeclarationNode)
            return builtin(((SyntheticDeclarationNode) decl).name(), args);

        if (decl instanceof Constructor)
            return buildStruct(((Constructor) decl).declaration, args);

        ScopeStorage oldStorage = storage;
        Scope scope = reactor.get(decl, "scope");
        storage = new ScopeStorage(scope, storage);

        FctDeclarationNode funDecl = (FctDeclarationNode) decl;
        coIterate(args, funDecl.parameters,
                (arg, param) -> storage.set(scope, param.name, arg));

        try {
            get(funDecl.block);
        } catch (Return r) {
            return r.value;
        } finally {
            storage = oldStorage;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object builtin (String name, Object[] args)
    {
        switch (name){
            case "print":
                System.out.println(convertToString(args[0]));
                break;
            case "rprint":
                System.out.println("Error: "+convertToString(args[0]));
                break;
            case "parseInt":
                try{
                    return parseInt(convertToString(args[0]));
                }catch (NumberFormatException e){
                    throw new PassthroughException(new Throwable("String passed in args cannot be converted to int"));
                }


            default:
                throw new Error("should not reach here");
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private String convertToString (Object arg)
    {
        if (arg == Null.INSTANCE)
            return "null";
        else if (arg instanceof Object[])
            return Arrays.deepToString((Object[]) arg);
        else if (arg instanceof FctDeclarationNode)
            return ((FctDeclarationNode) arg).name;
        else if (arg instanceof StructDeclarationNode)
            return ((StructDeclarationNode) arg).name;
        else if (arg instanceof Constructor)
            return "new " + ((Constructor) arg).declaration.name;
        else
            return arg.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private HashMap<String, Object> buildStruct (StructDeclarationNode node, Object[] args)
    {
        HashMap<String, Object> struct = new HashMap<>();
        if (args.length > 0){
            for (int i = 0; i < node.attributes.size(); ++i)
                struct.put(node.attributes.get(i).name, args[i]);
        }
        else {
            Scope scope = reactor.get(node, "scope");
            for (int i = 0; i < node.attributes.size(); ++i)
                struct.put(node.attributes.get(i).name, rootStorage.get(scope, node.name+"##"+node.attributes.get(i).name));
        }
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

        if (decl instanceof LetDeclarationNode
        || decl instanceof ParameterNode
        || decl instanceof SyntheticDeclarationNode
                && ((SyntheticDeclarationNode) decl).kind() == DeclarationKind.VARIABLE)
            return scope == rootScope
                ? rootStorage.get(scope, node.name)
                : storage.get(scope, node.name);

        return decl; // structure or function
    }

    // ---------------------------------------------------------------------------------------------

    private Void returnStmt (ReturnNode node) {
        throw new Return(node.expression == null ? null : get(node.expression));
    }

    // ---------------------------------------------------------------------------------------------

    private Void structDecl(StructDeclarationNode node)
    {
        Scope scope = reactor.get(node, "scope");
        for (AttributeDeclarationNode attr : node.attributes)
            assign(scope, node.name+"##"+attr.name, get(attr.initializer), reactor.get(node, "type"));
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void letDecl(LetDeclarationNode node)
    {
        Scope scope = reactor.get(node, "scope");
        Object init = get(node.initializer);
        Type initType = reactor.get(node, "type");
        if (initType instanceof NotYetType)
            initType = whichTypeIs(init);

        assign(scope, node.name, init, initType);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private void assign (Scope scope, String name, Object value, Type targetType)
    {
        if (value instanceof Long && targetType instanceof FloatType){
            double dvalue = ((Long) value).doubleValue();
            storage.set(scope, name, dvalue);
            return;
        }
        storage.set(scope, name, value);
    }

    // ---------------------------------------------------------------------------------------------

    private boolean isAssignableTo (Type a, Type b)
    {
        if (atLeastOneNYT(a, b))
            throw new Error("isAssignableTo : should not reach here");

        if (a instanceof VoidType || b instanceof VoidType)
            return false;

        if (a instanceof IntType && b instanceof FloatType)
            return true;

        if (a instanceof ArrayType)
            return b instanceof ArrayType
                    && isAssignableTo(((ArrayType)a).componentType, ((ArrayType)b).componentType);

        return a instanceof NullType && b.isReference() || a.equals(b);
    }
}
