package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

import static utils_static.UtilStatic.typesMap;

public final class ParameterNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;

    public ParameterNode (Span span, Object name, Object maybeType) {
        super(span);
        this.name = Util.cast(name, String.class);

        if(maybeType==null){
            this.type = Util.cast(new SimpleTypeNode(span, "NotYet"), TypeNode.class);
            UtilStatic.surePut(this.name, "NotYet");
            //System.out.printf("The current map is %s%n", typesMap.toString());
        }
        else {
            this.type = new SimpleTypeNode(span, ((ReferenceNode)maybeType).name);
            UtilStatic.surePut(this.name, type.contents());
            //System.out.printf("The current map is %s%n", typesMap.toString());
        }


    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return name;
    }

    @Override public String declaredThing () {
        return "parameter";
    }

    @Override
    public String getType() {
        return type.contents();
    }
}
