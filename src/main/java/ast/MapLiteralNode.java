package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.List;

public class MapLiteralNode extends ExpressionNode {
    
    public final List<MapEntryNode> entries;

    @SuppressWarnings("unchecked")
    public MapLiteralNode (Span span, Object entries) {
        super(span);
        this.entries = Util.cast(entries, List.class);
    }

    @Override public String contents ()
    {
        if (entries.size() == 0)
            return "{}";

        int budget = contentsBudget() - 2; // 2 == "[]".length()
        StringBuilder b = new StringBuilder("{");
        int i = 0;

        for (ExpressionNode it: entries)
        {
            if (i > 0) b.append(", ");
            String contents = it.contents();
            budget -= 2 + contents.length();
            if (i == entries.size() - 1) {
                if (budget < 0) break;
            } else {
                if (budget - ", ...".length() < 0) break;
            }
            b.append(contents);
            ++i;
        }

        if (i < entries.size())
            b.append("...");

        return b.append('}').toString();
    }

    @Override
    public String getType() {
        String type = entries.get(0).getType();

        if(type.equals("Bool") || type.equals("String") || type.equals("Float")) return type;

        if(type.equals("Int")){
            for (ExpressionNode contentExpression : entries){
                if (contentExpression.getType().equals("Type")){
                    return "Type";
                }
                if (contentExpression.getType().equals("Float")){
                    return "Float";
                }
            }
            return "Int";
        }
        return type;
    }
}
