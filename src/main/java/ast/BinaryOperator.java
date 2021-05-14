package ast;

public enum BinaryOperator
{
    TIMES("*"),
    POW("**"),
    DIVID("/"),
    MODULO("%"),
    PLUS("+"),
    MINUS("-"),
    EQUAL("=="),
    DIFF("!="),
    GREATER(">"),
    LOWER("<"),
    GREATER_EQUAL(">="),
    LOWER_EQUAL("<="),
    AND("&&"),
    OR("||");

    public final String string;

    BinaryOperator (String string) {
        this.string = string;
    }
}
