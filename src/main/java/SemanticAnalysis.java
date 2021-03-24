import ast.*;
import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;

import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;

public final class SemanticAnalysis {
    // =============================================================================================
    // region [Initialization]
    // =============================================================================================

    private final Reactor R;

    // ---------------------------------------------------------------------------------------------

    private SemanticAnalysis(Reactor reactor) {
        this.R = reactor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call this method to create a tree walker that will instantiate the typing rules defined
     * in this class when used on an AST, using the given {@code reactor}.
     */
    public static Walker<SighNode> createWalker(Reactor reactor) {
        ReflectiveFieldWalker<SighNode> walker = new ReflectiveFieldWalker<>(
                SighNode.class, PRE_VISIT, POST_VISIT);

        SemanticAnalysis analysis = new SemanticAnalysis(reactor);

        // TODO Add your rules here

        // Fallback rules
        walker.registerFallback(PRE_VISIT, node -> {
        });
        walker.registerFallback(POST_VISIT, node -> {
        });

        return walker;
    }
}