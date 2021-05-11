package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

public final class AttributeAccessNode extends ExpressionNode
{
    public final ExpressionNode parent;
    public final String attrName;

    public AttributeAccessNode (Span span, Object parent, Object fieldName) {
        super(span);
        this.parent = Util.cast(parent, ExpressionNode.class);
        this.attrName = Util.cast(fieldName, String.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s.%s", parent.contents(), attrName);
        return candidate.length() <= contentsBudget()
                ? candidate
                : "(?)." + attrName;
    }

    @Override
    public String getType() {
        return UtilStatic.typesMap.get(parent.getType()+"##"+attrName);
    }
}