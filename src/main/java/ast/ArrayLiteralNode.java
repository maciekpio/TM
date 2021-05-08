package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class ArrayLiteralNode extends ExpressionNode
{
    public final List<ExpressionNode> components;

    @SuppressWarnings("unchecked")
    public ArrayLiteralNode (Span span, Object components) {
        super(span);
        this.components = Util.cast(components, List.class);
    }

    @Override public String contents ()
    {
        if (components.size() == 0)
            return "[]";

        int budget = contentsBudget() - 2; // 2 == "[]".length()
        StringBuilder b = new StringBuilder("[");
        int i = 0;

        for (ExpressionNode it: components)
        {
            if (i > 0) b.append(", ");
            String contents = it.contents();
            budget -= 2 + contents.length();
            if (i == components.size() - 1) {
                if (budget < 0) break;
            } else {
                if (budget - ", ...".length() < 0) break;
            }
            b.append(contents);
            ++i;
        }

        if (i < components.size())
            b.append("...");

        return b.append(']').toString();
    }

    @Override
    public String getType() {

        if (components.get(0).getType().equals("Bool")) return "Bool";
        if (components.get(0).getType().equals("String")) return "String";

        for (ExpressionNode contentExpression : components){
            if (contentExpression.getType().equals("Type")){
                return "Type";
            }
            if (contentExpression.getType().equals("Float")){
                return "Float";
            }
        }
        return "Int";
    }
}
