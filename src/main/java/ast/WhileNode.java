package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class WhileNode extends StatementNode
{
    public final ExpressionNode condition;
    public final BlockNode block;

    public WhileNode (Span span, Object condition, Object block) {
        super(span);
        this.condition = Util.cast(condition, ExpressionNode.class);
        this.block = Util.cast(block, BlockNode.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("while %s ...", condition.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : "while (?) ...";
    }
}
