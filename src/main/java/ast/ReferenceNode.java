package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import static utils_static.UtilStatic.typesMap;

public final class ReferenceNode extends ExpressionNode
{
    public final String name;
    public String type;

    public ReferenceNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = typesMap.get(this.name);
        if(type != null){
            System.out.printf("The reference \"%s\" was indeed in the map as %s%n", this.name, typesMap.get(this.name));
        }else{
            throw new Error(String.format("The reference \"%s\" is not declared%n", this.name));
            //System.out.printf("The reference \"%s\" was not found in the map...%n", this.name);
        }
        //System.out.printf("The reference \"%s\" was used !%n", this.name);
    }

    @Override public String contents() {
        return name;
    }

    @Override
    public String getType() {
        if(type!=null) return type;
        return "Type";
    }

    public void setType(String strType){
        System.out.printf("The type %s of the node reference \"%s\" was changed to %s%n", this.getType(), name, strType);
        this.type = strType;
    }
}
