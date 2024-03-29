package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

public class ConstructorNode extends ExpressionNode
{
    public final ReferenceNode ref;

    public ConstructorNode (Span span, Object ref) {
        super(span);
        this.ref = Util.cast(ref, ReferenceNode.class);
    }

    @Override public String contents () {
        return "new "+ ref.name;
    }

    @Override
    public String getType() {
        return ref.getType();
    }
}
