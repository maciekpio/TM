package types;

public final class MapType extends Type
{
    public final Type componentType;

    public MapType (Type componentType) {
        this.componentType = componentType;
    }

    @Override public String name() {
        return componentType.toString() + "{}";
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof MapType && componentType.equals(o);
    }

    @Override public int hashCode () {
        return componentType.hashCode();
    }
}
