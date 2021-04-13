package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class AttributeAccessNode extends ExpressionNode
{
    public final ExpressionNode stem;
    public final String attrName;

    public AttributeAccessNode (Span span, Object stem, Object fieldName) {
        super(span);
        this.stem = Util.cast(stem, ExpressionNode.class);
        this.attrName = Util.cast(fieldName, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s.%s", stem.contents(), attrName);
        return candidate.length() <= contentsBudget()
                ? candidate
                : "(?)." + attrName;
    }

    @Override
    public String getType() {
        return "attrName";
    }
}