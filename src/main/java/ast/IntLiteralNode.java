package ast;

import norswap.autumn.positions.Span;

public final class IntLiteralNode extends ExpressionNode
{
    public final long value;

    public IntLiteralNode (Span span, long value) {
        super(span);
        this.value = value;
    }

    @Override public String contents() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        IntLiteralNode o;
        try{
            o = (IntLiteralNode) obj;
        } catch (ClassCastException e){
            return false;
        }
        return this.value == (o.value);
    }
}
