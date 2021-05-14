import ast.*;
import scopes.*;
import types.*;
import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static norswap.utils.Util.cast;
import static ast.BinaryOperator.*;
import static java.lang.String.format;
import static norswap.utils.Vanilla.list;
import static norswap.utils.Vanilla.forEachIndexed;
import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;
import static utils_static.UtilStatic.*;

public final class TMSemantic {
    // =============================================================================================
    // region [Initialization]
    // =============================================================================================

    private final Reactor R;

    private Scope scope; //Current scope

    private SighNode inferenceContext;

    private int argumentIndex;//index of the current function argument

    // ---------------------------------------------------------------------------------------------

    private TMSemantic(Reactor reactor) {
        this.R = reactor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call this method to create a tree walker that will instantiate the typing rules defined
     * in this class when used on an AST, using the given {@code reactor}.
     */
    public static Walker<SighNode> createWalker(Reactor reactor) {
        ReflectiveFieldWalker<SighNode> walker = new ReflectiveFieldWalker<>(
                SighNode.class, PRE_VISIT, POST_VISIT);

        TMSemantic analysis = new TMSemantic(reactor);

        // TODO Add your rules here
        // expressions
        walker.register(BooleanLiteralNode.class,       PRE_VISIT,  analysis::boolLiteral);
        walker.register(IntLiteralNode.class,           PRE_VISIT,  analysis::intLiteral);
        walker.register(FloatLiteralNode.class,         PRE_VISIT,  analysis::floatLiteral);
        walker.register(StringLiteralNode.class,        PRE_VISIT,  analysis::stringLiteral);
        walker.register(ReferenceNode.class,            PRE_VISIT,  analysis::reference);
        walker.register(ConstructorNode.class,          PRE_VISIT,  analysis::constructor);
        walker.register(ArrayLiteralNode.class,         PRE_VISIT,  analysis::arrayLiteral);
        walker.register(MapLiteralNode.class,           PRE_VISIT,  analysis::mapLiteral);
        walker.register(ArrayOfNode.class,              PRE_VISIT,  analysis::arrayOf);
        walker.register(MapEntryNode.class,             PRE_VISIT,  analysis::mapEntry);
        walker.register(ParenthesizedNode.class,        PRE_VISIT,  analysis::parenthesized);
        walker.register(AttributeAccessNode.class,      PRE_VISIT,  analysis::attrAccess);
        walker.register(ArrayMapGetNode.class,          PRE_VISIT,  analysis::arrayMapGet);
        walker.register(ArrayMapPutNode.class,          PRE_VISIT,  analysis::arrayMapPut);
        walker.register(FctCallNode.class,              PRE_VISIT,  analysis::fctCall);
        walker.register(UnaryExpressionNode.class,      PRE_VISIT,  analysis::unaryExpression);//TODO prevent NotYetType
        walker.register(BinaryExpressionNode.class,     PRE_VISIT,  analysis::binaryExpression);
        walker.register(AssignmentNode.class,           PRE_VISIT,  analysis::assignment);

        // types
        walker.register(SimpleTypeNode.class,           PRE_VISIT,  analysis::simpleType);
        walker.register(ArrayTypeNode.class,            PRE_VISIT,  analysis::arrayType);
        walker.register(MapTypeNode.class,              PRE_VISIT,  analysis::mapType);

        // declarations & scopes
        walker.register(RootNode.class,                 PRE_VISIT,  analysis::root);
        walker.register(BlockNode.class,                PRE_VISIT,  analysis::block);
        walker.register(LetDeclarationNode.class,       PRE_VISIT,  analysis::letDecl);
        walker.register(AttributeDeclarationNode.class, PRE_VISIT,  analysis::attrDecl);
        walker.register(ParameterNode.class,            PRE_VISIT,  analysis::parameter);
        walker.register(FctDeclarationNode.class,       PRE_VISIT,  analysis::fctDecl);
        walker.register(StructDeclarationNode.class,    PRE_VISIT,  analysis::structDecl);

        walker.register(RootNode.class,                 POST_VISIT, analysis::popScope);
        walker.register(BlockNode.class,                POST_VISIT, analysis::popScope);
        walker.register(FctDeclarationNode.class,       POST_VISIT, analysis::popScope);

        // statements
        walker.register(ExpressionStatementNode.class,  PRE_VISIT,  node -> {});
        walker.register(IfNode.class,                   PRE_VISIT,  analysis::ifStmt);
        walker.register(WhileNode.class,                PRE_VISIT,  analysis::whileStmt);
        walker.register(ReturnNode.class,               PRE_VISIT,  analysis::returnStmt);

        // Fallback rules
        walker.registerFallback(PRE_VISIT, node -> {
        });
        walker.registerFallback(POST_VISIT, node -> {
        });

        return walker;
    }

// endregion
    // =============================================================================================
    // region [Expressions]
    // =============================================================================================

    private void intLiteral (IntLiteralNode node) {
        R.set(node, "type", IntType.INSTANCE);
    }

    // ---------------------------------------------------------------------------------------------

    private void boolLiteral (BooleanLiteralNode node) {
        R.set(node, "type", BoolType.INSTANCE);
    }

    // ---------------------------------------------------------------------------------------------

    private void floatLiteral (FloatLiteralNode node) {
        R.set(node, "type", FloatType.INSTANCE);
    }

    // ---------------------------------------------------------------------------------------------

    private void stringLiteral (StringLiteralNode node) {
        R.set(node, "type", StringType.INSTANCE);
    }

    // ---------------------------------------------------------------------------------------------

    private void reference (ReferenceNode node)
    {
        final Scope scope = this.scope;

        // Try to lookup immediately. This must succeed for variables, but not necessarily for
        // functions or types. By looking up now, we can report looked up variables later
        // as being used before being defined.
        DeclarationContext maybeCtx = scope.lookup(node.name);

        if (maybeCtx != null) {
            R.set(node, "decl",  maybeCtx.declaration);
            R.set(node, "scope", maybeCtx.scope);
            R.rule(node, "type")
            .using(maybeCtx.declaration, "type")
            .by(Rule::copyFirst);
            return;
        }

        // Re-lookup after the scopes have been built.
        R.rule(node.attr("decl"), node.attr("scope"))
        .by(r -> {
            DeclarationContext ctx = scope.lookup(node.name);
            DeclarationNode decl = ctx == null ? null : ctx.declaration;

            if (ctx == null) {
                r.errorFor("could not resolve: " + node.name,
                    node, node.attr("decl"), node.attr("scope"), node.attr("type"));
            }
            else {
                r.set(node, "scope", ctx.scope);
                r.set(node, "decl", decl);

                if (decl instanceof LetDeclarationNode)
                    r.errorFor("variable used before declaration: " + node.name,
                        node, node.attr("type"));
                else
                    R.rule(node, "type")
                    .using(decl, "type")
                    .by(Rule::copyFirst);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void constructor (ConstructorNode node)
    {
        R.rule()
        .using(node.ref, "decl")
        .by(r -> {
            DeclarationNode decl = r.get(0);

            if (!(decl instanceof StructDeclarationNode)) {
                String description =
                        "Applying the constructor operator (new) to non-struct reference for: "
                        + decl;
                r.errorFor(description, node, node.attr("type"));
                return;
            }

            StructDeclarationNode structDecl = (StructDeclarationNode) decl;

            Attribute[] dependencies = new Attribute[structDecl.attributes.size() + 1];
            dependencies[0] = decl.attr("declared");
            forEachIndexed(structDecl.attributes, (i, field) ->
                dependencies[i + 1] = field.attr("type"));

            R.rule(node, "type")
            .using(dependencies)
            .by(rr -> {
                Type structType = rr.get(0);
                Type[] params = IntStream.range(1, dependencies.length).<Type>mapToObj(rr::get)
                        .toArray(Type[]::new);
                rr.set(0, new FunType(structType, params));
            });
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayOf (ArrayOfNode node)
    {
        R.rule()
        .using(node.length, "type")
        .by(r -> {
            Type type = r.get(0);
            if (!(type instanceof IntType || type instanceof NotYetType))
                r.error("Initializing an array using a non-int-valued expression as length.", node.length);
            //r.set(0, type);
        });

        R.rule(node, "type")
        .using(node.initializer, "type")
        .by(r -> {
            Type type = r.get(0);
            if (type instanceof VoidType)
                r.error("Void type is used as an initializer type of an array.", node.initializer);
            r.set(0, new ArrayType(type));
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayLiteral (ArrayLiteralNode node)
    {
        Attribute[] dependencies =
            node.components.stream().map(it -> it.attr("type")).toArray(Attribute[]::new);

        R.rule(node, "type")
        .using(dependencies)
        .by(r -> {
            Type[] types = IntStream.range(0, dependencies.length).<Type>mapToObj(r::get)
                    .distinct().toArray(Type[]::new);

            int i = 0;
            Type supertype = null;
            for (Type type: types) {
                if (type instanceof VoidType)
                    // We report the error, but compute a type for the array from the other elements.
                    r.errorFor("Void-valued expression in array literal", node.components.get(i));
                else if (supertype == null)
                    supertype = type;
                else {
                    supertype = commonSupertype(supertype, type);
                    if (supertype == null) {
                        r.error("Could not find common supertype in array literal.", node);
                        return;
                    }
                }
                ++i;
            }

            if (supertype == null)
                r.error(
                    "Could not find common supertype in array literal: all members have Void type.",
                    node);
            else
                r.set(0, new ArrayType(supertype));
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void mapLiteral (MapLiteralNode node)
    {
        Attribute[] dependencies =
                node.entries.stream().map(it -> it.attr("type")).toArray(Attribute[]::new);

        R.rule(node, "type")
                .using(dependencies)
                .by(r -> {
                    Type[] types = IntStream.range(0, dependencies.length).<Type>mapToObj(r::get)
                            .distinct().toArray(Type[]::new);

                    int i = 0;
                    Type supertype = null;
                    for (Type type: types) {
                        if (type instanceof VoidType)
                            // We report the error, but compute a type for the array from the other elements.
                            r.errorFor("Void-valued expression in map literal", node.entries.get(i));
                        else if (supertype == null)
                            supertype = type;
                        else {
                            supertype = commonSupertype(supertype, type);
                            if (supertype == null) {
                                r.error("Could not find common supertype in map literal.", node);
                                return;
                            }
                        }
                        ++i;
                    }

                    if (supertype == null)
                        r.error(
                                "Could not find common supertype in map literal: all members have Void type.",
                                node);
                    else
                        r.set(0, new MapType(supertype));
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void mapEntry (MapEntryNode node)
    {
        R.rule(node, "type")
        .using(node.value, "type")
        .by(Rule::copyFirst);

        R.rule()
        .using(node.key, "type")
        .by(r -> {
            if (!isInstanceOf(r.get(0), StringType.class, NotYetType.class))
                r.errorFor("Trying to use a non-string as key for a map entry", node.key);
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void parenthesized (ParenthesizedNode node)
    {
        R.rule(node, "type")
        .using(node.expression, "type")
        .by(Rule::copyFirst);
    }

    // ---------------------------------------------------------------------------------------------

    private void attrAccess(AttributeAccessNode node)
    {
        R.rule()
        .using(node.parent, "type")
        .by(r -> {
            Type type = r.get(0);

            if (type instanceof NotYetType) {
                R.rule(node, "type")
                .by(rr -> rr.set(0, NotYetType.INSTANCE));
                return;
            }

            if (type instanceof ArrayType) {
                if (node.attrName.equals("length"))
                    R.rule(node, "type")
                    .by(rr -> rr.set(0, IntType.INSTANCE));
                else
                    r.errorFor("Trying to access a non-length field on an array", node,
                        node.attr("type"));
                return;
            }
            
            if (!(type instanceof StructType)) {
                r.errorFor("Trying to access a field on an expression of type " + type,
                        node,
                        node.attr("type"));
                return;
            }

            StructDeclarationNode decl = ((StructType) type).node;

            for (DeclarationNode attribute: decl.attributes)
            {
                if (!attribute.name().equals(node.attrName)) continue;

                R.rule(node, "type")
                .using(attribute, "type")
                .by(Rule::copyFirst);
                return;
            }

            String description = format("Trying to access missing field %s on struct %s",
                    node.attrName, decl.name);
            r.errorFor(description, node, node.attr("type"));
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayMapGet(ArrayMapGetNode node)
    {
        R.rule()
        .using(node.index_key, "type")
        .by(r -> {
            Type type = r.get(0);
            if (!isInstanceOf(type, IntType.class, StringType.class, NotYetType.class))
                r.error("Indexing an array/map using a non-int-valued/non-string-valued expression.", node.index_key);
        });

        R.rule(node, "type")
        .using(node.array_map, "type")
        .by(r -> {
            Type type = r.get(0);
            if (type instanceof NotYetType)
                r.set(0, NotYetType.INSTANCE);
            else if (type instanceof ArrayType)
                r.set(0, ((ArrayType) type).componentType);
            else if (type instanceof MapType)
                r.set(0, ((MapType) type).componentType);
            else
                r.error("Trying to index a non-array/non-map expression of type " + type, node);
        });
    }

    private void arrayMapPut(ArrayMapPutNode node)
    {
        R.rule()
        .using(node.index_key, "type")
        .by(r -> {
            Type type = r.get(0);
            if (!isInstanceOf(type, IntType.class, StringType.class, NotYetType.class))
                r.error("Indexing an array/map using a non-int-valued/non-string-valued expression.", node.index_key);
        });

        R.rule(node, "type")
        .using(node.array_map.attr("type"), node.object_valuePut.attr("type"))
        .by(r -> {
            Type array_mapType = r.get(0);
            Type putType = r.get(1);

            if (!atLeastOneNYT(array_mapType, putType)){
                if (array_mapType instanceof ArrayType){
                    Type comType = ((ArrayType) array_mapType).componentType;
                    if (!putType.equals(comType))
                        r.error(String.format("Trying to put %s type in an array of %s", putType.name(), comType.name()), node);
                }
                else if (array_mapType instanceof MapType){
                    Type comType = ((MapType) array_mapType).componentType;
                    if (!putType.equals(comType))
                        r.error(String.format("Trying to put %s type in an array of %s", putType.name(), comType.name()), node);
                }
                else{
                    r.error("Trying to index a non-array/non-map expression of type " + array_mapType, node);
                }
            }
            r.set(0, BoolType.INSTANCE);
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void fctCall (FctCallNode node)
    {
        this.inferenceContext = node;

        Attribute[] dependencies = new Attribute[node.arguments.size() + 1];
        dependencies[0] = node.function.attr("type");
        forEachIndexed(node.arguments, (i, arg) -> {
            dependencies[i + 1] = arg.attr("type");
            R.set(arg, "index", i);
        });

        R.rule(node, "type")
        .using(dependencies)
        .by(r -> {
            Type maybeFunType = r.get(0);

            if (!(maybeFunType instanceof FunType)) {
                r.error("trying to call a non-function expression: " + node.function, node.function);
                return;
            }

            FunType funType = cast(maybeFunType);
            r.set(0, funType.returnType);

            Type[] params = funType.paramTypes;
            List<ExpressionNode> args = node.arguments;

            boolean isDefaultConstructor = false;

            if(node.function instanceof ConstructorNode && args.size()==0) isDefaultConstructor=true;

            if (params.length != args.size() && !isDefaultConstructor)
                r.errorFor(format("wrong number of arguments, expected %d but got %d",
                        params.length, args.size()), node);

            int checkedArgs = Math.min(params.length, args.size());

            for (int i = 0; i < checkedArgs; ++i) {
                Type argType = r.get(i + 1);
                Type paramType = funType.paramTypes[i];
                if (!isAssignableTo(argType, paramType))
                    r.errorFor(format(
                            "incompatible argument provided for argument %d: expected %s but got %s",
                            i, paramType, argType),
                        node.arguments.get(i));
            }
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void unaryExpression (UnaryExpressionNode node)
    {
        assert node.operator == UnaryOperator.NOT; // only one for now
        R.set(node, "type", BoolType.INSTANCE);

        R.rule()
        .using(node.operand, "type")
        .by(r -> {
            Type opType = r.get(0);
            if (!(isInstanceOf(opType,BoolType.class, NotYetType.class)))
                r.error("Trying to negate type: " + opType, node);
        });
    }

    // endregion
    // =============================================================================================
    // region [Binary Expressions]
    // =============================================================================================

    private void binaryExpression (BinaryExpressionNode node)
    {
        R.rule(node, "type")
        .using(node.left.attr("type"), node.right.attr("type"))
        .by(r -> {
            Type left  = r.get(0);
            Type right = r.get(1);

            if(left instanceof NotYetType && right instanceof NotYetType)
                r.set(0, NotYetType.INSTANCE);
            else if (node.operator == PLUS && (left instanceof StringType || right instanceof StringType))
                r.set(0, StringType.INSTANCE);
            else if (isArithmetic(node.operator))
                binaryArithmetic(r, node, left, right);
            else if (isComparison(node.operator))
                binaryComparison(r, node, left, right);
            else if (isLogic(node.operator))
                binaryLogic(r, node, left, right);
            else if (isEquality(node.operator))
                binaryEquality(r, node, left, right);
        });
    }

    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------

    private void binaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        if (isInstanceOf(left, NotYetType.class, FloatType.class) && isInstanceOf(right, NotYetType.class, FloatType.class))
            r.set(0, FloatType.INSTANCE);
        else if (atLeastOneNYT(left, right))
            r.set(0, NotYetType.INSTANCE);
        else if (left instanceof IntType)
            if (right instanceof IntType)
                r.set(0, IntType.INSTANCE);
            else if (right instanceof FloatType)
                r.set(0, FloatType.INSTANCE);
            else
                r.error(arithmeticError(node, "int", right), node);
        else if (left instanceof FloatType)
            if (right instanceof IntType || right instanceof FloatType)
                r.set(0, FloatType.INSTANCE);
            else
                r.error(arithmeticError(node, "float", right), node);
        else
            r.error(arithmeticError(node, left, right), node);
    }

    // ---------------------------------------------------------------------------------------------

    private static String arithmeticError (BinaryExpressionNode node, Object left, Object right) {
        return format("Trying to %s %s with %s", node.operator.name().toLowerCase(), left, right);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryComparison (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (atLeastOneNYT(left, right)) return;

        if (!(isNumber(left)))
            r.errorFor("Attempting to perform arithmetic comparison on non-numeric type: " + left,
                node.left);
        if (!(isNumber(right)))
            r.errorFor("Attempting to perform arithmetic comparison on non-numeric type: " + right,
                node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryEquality (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (!isComparableTo(left, right))
            r.errorFor(format("Trying to compare incomparable types %s and %s", left, right),
                node);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryLogic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (atLeastOneNYT(left, right)) return;

        if (!(left instanceof BoolType))
            r.errorFor("Attempting to perform binary logic on non-boolean type: " + left,
                node.left);
        if (!(right instanceof BoolType))
            r.errorFor("Attempting to perform binary logic on non-boolean type: " + right,
                node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private void assignment (AssignmentNode node)
    {
        R.rule(node, "type")
        .using(node.left.attr("type"), node.right.attr("type"))
        .by(r -> {
            Type left  = r.get(0);
            Type right = r.get(1);
            //System.out.printf("Assignment of (%s) %s with (%s) %s%n", left.toString(), node.left.contents(), right.toString(), node.right.contents());

            r.set(0, right); // the type of the assignment is the right-side type

            if (node.left instanceof ReferenceNode || node.left instanceof AttributeAccessNode) {
                if (!isAssignableTo(right, left) && !left.toString().equals("Type") && !left.toString().equals("Type[]")) {
                    r.errorFor("Trying to assign a value to a non-compatible lvalue.", node);
                }
            }
            else if (node.left instanceof ArrayMapGetNode)
                r.errorFor("\"array.get(i) = object\" is not permitted. Instead use array.put(i:object)", node);
            else
                r.errorFor("Trying to assign to an non-lvalue expression.", node.left);
        });
    }

    // endregion
    // =============================================================================================
    // region [Types & Typing Utilities]
    // =============================================================================================

    private void simpleType (SimpleTypeNode node)
    {
        final Scope scope = this.scope;

        R.rule()
        .by(r -> {
            // type declarations may occur after use
            DeclarationContext ctx = scope.lookup(node.name);
            DeclarationNode decl = ctx == null ? null : ctx.declaration;

            if (ctx == null)
                r.errorFor("could not resolve: " + node.name,
                    node,
                    node.attr("value"));

            else if (!isTypeDecl(decl))
                r.errorFor(format(
                    "%s did not resolve to a type declaration but to a %s declaration",
                    node.name, decl.declaredThing()),
                    node,
                    node.attr("value"));

            else
                R.rule(node, "value")
                .using(decl, "declared")
                .by(Rule::copyFirst);
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayType (ArrayTypeNode node)
    {
        R.rule(node, "value")
        .using(node.componentType, "value")
        .by(r -> r.set(0, new ArrayType(r.get(0))));
    }

    // ---------------------------------------------------------------------------------------------

    private void mapType (MapTypeNode node)
    {
        R.rule(node, "value")
        .using(node.componentType, "value")
        .by(r -> r.set(0, new MapType(r.get(0))));
    }

    // ---------------------------------------------------------------------------------------------

    private static boolean isTypeDecl (DeclarationNode decl)
    {
        if (decl instanceof StructDeclarationNode) return true;
        if (!(decl instanceof SyntheticDeclarationNode)) return false;
        SyntheticDeclarationNode synthetic = cast(decl);
        return synthetic.kind() == DeclarationKind.TYPE;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether a value of type {@code a} can be assigned to a location (variable,
     * parameter, ...) of type {@code b}.
     */
    private static boolean isAssignableTo (Type a, Type b)
    {
        if (atLeastOneNYT(a, b))
            return true;

        if (a instanceof VoidType || b instanceof VoidType)
            return false;

        if (a instanceof IntType && b instanceof FloatType)
            return true;

        if (a instanceof ArrayType)
            return b instanceof ArrayType
                && isAssignableTo(((ArrayType)a).componentType, ((ArrayType)b).componentType);

        if (a instanceof MapType)
            return b instanceof MapType
                    && isAssignableTo(((MapType)a).componentType, ((MapType)b).componentType);

        return a instanceof NullType && b.isReference() || a.equals(b);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether the two types are comparable.
     */
    private static boolean isComparableTo (Type a, Type b)
    {
        if (atLeastOneNYT(a, b))
            return true;

        if (a instanceof VoidType || b instanceof VoidType)
            return false;

        return a.isReference() && b.isReference()
            || a.equals(b)
            || a instanceof IntType && b instanceof FloatType
            || a instanceof FloatType && b instanceof IntType;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the common supertype between both types, or {@code null} if no such supertype
     * exists.
     */
    private static Type commonSupertype (Type a, Type b)
    {
        if (a instanceof VoidType || b instanceof VoidType)
            return null;
        if (isAssignableTo(a, b))
            return b;
        if (isAssignableTo(b, a))
            return a;
        else
            return null;
    }

    // endregion
    // =============================================================================================
    // region [Scopes & Declarations]
    // =============================================================================================

    private void popScope (SighNode node) {
        scope = scope.parent;
    }

    // ---------------------------------------------------------------------------------------------

    private void root (RootNode node) {
        assert scope == null;
        scope = new RootScope(node, R);
        R.set(node, "scope", scope);
    }

    // ---------------------------------------------------------------------------------------------

    private void block (BlockNode node) {
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        Attribute[] deps = getReturnsDependencies(node.statements);
        R.rule(node, "returns")
        .using(deps)
        .by(r -> r.set(0, deps.length != 0 && Arrays.stream(deps).anyMatch(r::get)));
    }

    // ---------------------------------------------------------------------------------------------

    private void letDecl(LetDeclarationNode node)
    {
        this.inferenceContext = node;

        //System.out.println(this.inferenceContext.contents() + " (1)");

        scope.declare(node.name, node);
        R.set(node, "scope", scope);

        R.rule(node, "type")
        .using(node.type, "value")
        .by(Rule::copyFirst);

        R.rule()
        .using(node.type.attr("value"), node.initializer.attr("type"))
        .by(r -> {
            Type expected = r.get(0);
            Type actual = r.get(1);

            //System.out.printf("expected %s and got %s%n", expected.toString(), actual.toString());

            if (!isAssignableTo(actual, expected))
                    r.error(format(
                            "incompatible initializer type provided for variable `%s`: expected %s but got %s",
                            node.name, expected, actual),
                            node.initializer);
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void attrDecl (AttributeDeclarationNode node)
    {
        this.inferenceContext = node;

        scope.declare(node.structName+"##"+node.name, node);
        R.set(node, "scope", scope);

        R.rule(node, "type")
        .using(node.type, "value")
        .by(Rule::copyFirst);
    }

    // ---------------------------------------------------------------------------------------------

    private void parameter (ParameterNode node)
    {
        R.set(node, "scope", scope);
        scope.declare(node.name, node); // scope pushed by FunDeclarationNode

        String str = node.getType();

        if(node.getType().equals("NotYet")){
            R.rule(node, "type");
            R.set(node, "type", NotYetType.INSTANCE);
        }
        else{
            R.rule(node, "type")
            .using(node.type, "value")
            .by(Rule::copyFirst);
        }


    }

    // ---------------------------------------------------------------------------------------------

    private void fctDecl (FctDeclarationNode node)
    {
        scope.declare(node.name, node);
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        Attribute[] dependencies = new Attribute[node.parameters.size() + 2];
        dependencies[0] = node.returnType.attr("value");
        dependencies[1] = node.block.attr("returns");

        forEachIndexed(node.parameters, (i, param) ->
                dependencies[i + 2] = param.attr("type"));

        R.rule(node, "type")
                .using(dependencies)
                .by (r -> {
                    boolean returns = r.get(1);
                    Type reType = returns ? r.get(0) : VoidType.INSTANCE;

                    Type[] paramTypes = new Type[node.parameters.size()];
                    for (int i = 0; i < paramTypes.length; ++i)
                        paramTypes[i] = r.get(i + 2);

                    r.set(0, new FunType(reType, paramTypes));
                });

        /*R.rule()
                .using(node.block.attr("returns"), node.returnType.attr("value"))
                .by(r -> {
                    boolean returns = r.get(0);
                    Type returnType = r.get(1);
                    if (!returns && !(returnType instanceof VoidType))
                        r.error("Missing return in function.", node);
                    // NOTE: The returned value presence & type is checked in returnStmt().
                });*/
    }

    // ---------------------------------------------------------------------------------------------

    private void structDecl (StructDeclarationNode node) {
        scope.declare(node.name, node);
        R.set(node, "scope", scope);
        R.set(node, "type", TypeType.INSTANCE);
        R.set(node, "declared", new StructType(node));
    }

    // endregion
    // =============================================================================================
    // region [Other Statements]
    // =============================================================================================

    private void ifStmt (IfNode node) {
        R.rule()
        .using(node.condition, "type")
        .by(r -> {
            Type type = r.get(0);
            if (!isInstanceOf(type, BoolType.class, NotYetType.class)) {
                r.error("If statement with a non-boolean condition of type: " + type,
                    node.condition);
            }
        });

        Attribute[] deps = getReturnsDependencies(list(node.trueStatement, node.falseStatement));
        R.rule(node, "returns")
        .using(deps)
        .by(r -> r.set(0, deps.length == 2 && Arrays.stream(deps).allMatch(r::get)));
    }

    // ---------------------------------------------------------------------------------------------

    private void whileStmt (WhileNode node) {
        R.rule()
        .using(node.condition, "type")
        .by(r -> {
            Type type = r.get(0);
            if (!isInstanceOf(type, BoolType.class, NotYetType.class)) {
                r.error("While statement with a non-boolean condition of type: " + type,
                    node.condition);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------

    private void returnStmt (ReturnNode node)
    {
        R.set(node, "returns", true);

        /*FctDeclarationNode function = currentFunction();
        if (function == null) // top-level return
            return;

        if (node.expression == null)
            R.rule()
            .using(function.returnType, "value")
            .by(r -> {
               Type returnType = r.get(0);
               if (!(returnType instanceof VoidType))
                   r.error("Return without value in a function with a return type.", node);
            });
        else
            R.rule()
            .using(function.returnType.attr("value"), node.expression.attr("type"))
            .by(r -> {
                Type formal = r.get(0);
                Type actual = r.get(1);
                if (formal instanceof VoidType)
                    r.error("Return with value in a Void function.", node);
                else if (!isAssignableTo(actual, formal)) {
                    r.errorFor(format(
                        "Incompatible return type, expected %s but got %s", formal, actual),
                        node.expression);
                }
            });*/
    }

    // ---------------------------------------------------------------------------------------------

    private FctDeclarationNode currentFunction()
    {
        Scope scope = this.scope;
        while (scope != null) {
            SighNode node = scope.node;
            if (node instanceof FctDeclarationNode)
                return (FctDeclarationNode) node;
            scope = scope.parent;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private boolean isReturnContainer (SighNode node) {
        return node instanceof BlockNode
            || node instanceof IfNode
            || node instanceof ReturnNode;
    }

    // ---------------------------------------------------------------------------------------------

    /** Get the depedencies necessary to compute the "returns" attribute of the parent. */
    private Attribute[] getReturnsDependencies (List<? extends SighNode> children) {
        return children.stream()
            .filter(Objects::nonNull)
            .filter(this::isReturnContainer)
            .map(it -> it.attr("returns"))
            .toArray(Attribute[]::new);
    }

    // endregion
    // =============================================================================================
}