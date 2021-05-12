package scopes;

import ast.RootNode;
import types.*;
import norswap.uranium.Reactor;

import static scopes.DeclarationKind.*;

/**
 * The lexical scope of a file in Sigh. It is notably responsible for introducing the default
 * declarations made by the language.
 */
public final class RootScope extends Scope
{
    // ---------------------------------------------------------------------------------------------

    private SyntheticDeclarationNode decl (String name, DeclarationKind kind) {
        SyntheticDeclarationNode decl = new SyntheticDeclarationNode(name, kind);
        declare(name,  decl);
        return decl;
    }

    // ---------------------------------------------------------------------------------------------

    // root scope types
    public final SyntheticDeclarationNode Bool   = decl("Bool",   TYPE);
    public final SyntheticDeclarationNode Int    = decl("Int",    TYPE);
    public final SyntheticDeclarationNode Float  = decl("Float",  TYPE);
    public final SyntheticDeclarationNode String = decl("String", TYPE);
    public final SyntheticDeclarationNode Void   = decl("Void",   TYPE);
    public final SyntheticDeclarationNode Type   = decl("Type",   TYPE);
    public final SyntheticDeclarationNode NotYet = decl("NotYet", TYPE);

    // root scope variables
    //public final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    //public final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    //public final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);

    // root scope functions
    public final SyntheticDeclarationNode print  = decl("print",  FUNCTION);
    public final SyntheticDeclarationNode rprint = decl("rprint", FUNCTION);
    public final SyntheticDeclarationNode is     = decl("is",     FUNCTION);
    //public final SyntheticDeclarationNode array  = decl("array",  FUNCTION);

    // ---------------------------------------------------------------------------------------------

    public RootScope (RootNode node, Reactor reactor) {
        super(node, null);

        reactor.set(Bool,   "type",       TypeType.INSTANCE);
        reactor.set(Int,    "type",       TypeType.INSTANCE);
        reactor.set(Float,  "type",       TypeType.INSTANCE);
        reactor.set(String, "type",       TypeType.INSTANCE);
        reactor.set(Void,   "type",       TypeType.INSTANCE);
        reactor.set(Type,   "type",       TypeType.INSTANCE);
        reactor.set(NotYet, "type",       TypeType.INSTANCE);

        reactor.set(Bool,   "declared",   BoolType.INSTANCE);
        reactor.set(Int,    "declared",    IntType.INSTANCE);
        reactor.set(Float,  "declared",  FloatType.INSTANCE);
        reactor.set(String, "declared", StringType.INSTANCE);
        reactor.set(Void,   "declared",   VoidType.INSTANCE);
        reactor.set(Type,   "declared",   TypeType.INSTANCE);
        reactor.set(NotYet, "declared", NotYetType.INSTANCE);

        //reactor.set(_true,  "type",       BoolType.INSTANCE);
        //reactor.set(_false, "type",       BoolType.INSTANCE);
        //reactor.set(_null,  "type",       NullType.INSTANCE);

        reactor.set(print,  "type", new FunType(VoidType.INSTANCE, StringType.INSTANCE));
        reactor.set(rprint, "type", new FunType(VoidType.INSTANCE, StringType.INSTANCE));
        //reactor.set(is,     "type", new FunType(BoolType.INSTANCE, StringType.INSTANCE));
    }

    // ---------------------------------------------------------------------------------------------
}
