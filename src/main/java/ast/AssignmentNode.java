package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import static utils_static.UtilStatic.*;

public class AssignmentNode extends ExpressionNode
{
    public final ExpressionNode left;
    public final ExpressionNode right;

    public AssignmentNode (Span span, Object left, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);

        if (left instanceof ReferenceNode)
            surePut(((ReferenceNode) this.left).name, this.right.getType());

        //System.out.printf("The current map is %s%n", typesMap.toString());
    }

    @Override public String contents ()
    {
        String leftEqual = left.contents() + " = ";

        String candidate = leftEqual + right.contents();
        if (candidate.length() <= contentsBudget())
            return candidate;

        candidate = leftEqual + "(?)";
        return candidate.length() <= contentsBudget()
            ? candidate
            : "(?) = (?)";
    }

    @Override
    public String getType() {
        return "Void";
    }
}
