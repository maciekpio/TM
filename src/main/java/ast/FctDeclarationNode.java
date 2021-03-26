package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class FctDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ParameterNode> parameters;
    public final BlockNode block;
    public final ReturnNode fct_return;

    @SuppressWarnings("unchecked")
    public FctDeclarationNode
            (Span span, Object name, Object parameters, Object block, Object fct_return) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);
        this.block = Util.cast(block, BlockNode.class);
        this.fct_return = Util.cast(fct_return, ReturnNode.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "fun " + name;
    }

    @Override public String declaredThing () {
        return "function";
    }
}
