package types;

public final class NotYetType extends Type
{
    /**
     * {@link NotYetType} is one of the most important feature of our language.
     * It allows the language to be very permissive, mostly during the interpretation of a function.
     *
     * Indeed, {@link ast.ParameterNode} (used as parameters function) are often Not Yet Typed.
     * This means that the use of theses variables are very dynamics.
     *
     * For example :
     * In two different instances of the same function, the compound operation could be very different
     * thanks to the dynamic type.
     *
     * Also, two different structures can have same attributes name, and so a function can use
     * this similitude to interact with different structures like it was the same.
     *
     * ----------------------------------------------
     * ----------examples with code samples----------
     * ----------------------------------------------
     *
     * def add (a, b) {
     *     return (a+b) ==> this will work with numbers and strings
     * }
     *
     * struct Leaf {name = "Leaf"; value = anInt ...}
     * struct Root {name = "Root"; value = anInt; left = new Leaf(); right = new Leaf() ...}
     *
     * def equals (nodeA, nodeB){
     *     return (nodeA.value == nodeB.value) ==> this will work with the two types of structure.
     * }
     */
    public static final NotYetType INSTANCE = new NotYetType();
    private NotYetType () {}

    @Override public boolean isPrimitive () {
        return true;
    }

    @Override public String name() {
        return "NotYet";
    }
}
