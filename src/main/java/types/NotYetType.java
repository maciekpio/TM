package types;

public final class NotYetType extends Type
{
    public static final NotYetType INSTANCE = new NotYetType();
    private NotYetType () {}

    @Override public boolean isPrimitive () {
        return true;
    }

    @Override public String name() {
        return "NotYet";
    }
}
