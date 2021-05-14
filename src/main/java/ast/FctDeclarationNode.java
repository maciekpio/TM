package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import utils_static.UtilStatic;

import java.util.List;

import static utils_static.UtilStatic.typesMap;

public class FctDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ParameterNode> parameters;
    public final BlockNode block;
    //public final ReturnNode fct_return;
    public TypeNode returnType;

    @SuppressWarnings("unchecked")
    public FctDeclarationNode
            (Span span, Object name, Object parameters, Object block) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);
        this.block = Util.cast(block, BlockNode.class);
        this.returnType = Util.cast(new SimpleTypeNode(span, "NotYet"), TypeNode.class);

        UtilStatic.surePut(this.name, returnType.contents());
        System.out.printf("The current map is %s%n", typesMap.toString());
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return String.format("def %s (%s) {%s}", name, parameters.toString(), block.contents());
    }

    @Override public String declaredThing () {
        return "function";
    }

    @Override
    public String getType() {
        return returnType.contents();
    }
}
