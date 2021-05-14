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

        String firstType = components.get(0).getType();

        if(firstType.equals("Bool") || firstType.equals("String") || firstType.equals("Float")) return firstType;

        if(firstType.equals("Int")){
            for (ExpressionNode contentExpression : components){
                if (contentExpression.getType().equals("Float")){
                    return "Float";
                }
                if (contentExpression.getType().equals("NotYet")){
                    return "NotYet";
                }
            }
            return "Int";
        }
        return firstType;
    }
}
