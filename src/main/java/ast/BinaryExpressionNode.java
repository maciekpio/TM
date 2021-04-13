package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class BinaryExpressionNode extends ExpressionNode
{
    public final ExpressionNode left, right;
    public final BinaryOperator operator;

    public BinaryExpressionNode (Span span, Object left, Object operator, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);
        this.operator = Util.cast(operator, BinaryOperator.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s %s %s",
            left.contents(), operator.string, right.contents());

        return candidate.length() <= contentsBudget()
            ? candidate
            : String.format("(?) %s (?)", operator.string);
    }

    @Override
    public boolean equals(Object obj) {
        BinaryExpressionNode o;
        try{
            o = (BinaryExpressionNode) obj;
        } catch (ClassCastException e){
            return false;
        }
        return this.left.equals(o.left) && this.right.equals(o.right) && this.operator.equals(o.operator);
    }

    @Override
    public String getType() {
        String leftType = left.getType();
        String rightType = right.getType();
        if(leftType.equals("Float")||rightType.equals("Float")){
            return "Float";
        }
        return "Int";
    }
}
