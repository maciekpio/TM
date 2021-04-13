package ast;

import norswap.autumn.positions.Span;

public class BooleanNode extends ExpressionNode
{
    public final boolean value;

    public BooleanNode (Span span, boolean value) {
        super(span);
        this.value = value;
    }

    @Override public String contents() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        BooleanNode o;
        try{
            o = (BooleanNode) obj;
        } catch (ClassCastException e){
            return false;
        }
        return this.value == o.value;
    }

    @Override
    public String getType() {
        return "Bool";
    }
}