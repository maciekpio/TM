package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class PutParametersNode extends ExpressionNode{
    public final ExpressionNode objectExpression;
    public final ExpressionNode indexExpression;

    public PutParametersNode(Span span, Object objectExpression, Object indexExpression) {
        super(span);
        this.objectExpression = Util.cast(objectExpression, ExpressionNode.class);
        this.indexExpression = Util.cast(indexExpression, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("(%s)", indexExpression.contents());
    }
}
