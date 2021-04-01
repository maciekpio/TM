import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMap;
import norswap.autumn.positions.LineMapString;
import ast.SighNode;
import interpreter.Interpreter;
import norswap.uranium.Reactor;
import norswap.utils.IO;
import norswap.utils.visitors.Walker;
import java.nio.file.Paths;

import static norswap.utils.Util.cast;

public final class Test
{
    public static void main (String[] args) {
//         String file = "fizzbuzz.si";
        String file = "kitchensink.si";
        String path = Paths.get("examples/", file).toAbsolutePath().toString();
        String src = IO.slurp(path);
        ArithmeticGrammar grammar = new ArithmeticGrammar();
        ParseOptions options = ParseOptions.builder().recordCallStack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);
        LineMap lineMap = new LineMapString(path, src);
        System.out.println(result.toString(lineMap, false));

        if (!result.fullMatch)
            return;

        SighNode tree = cast(result.topValue());
        Reactor reactor = new Reactor();
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(tree);
        reactor.run();

        if (!reactor.errors().isEmpty()) {
            System.out.println(reactor.reportErrors(it -> it.toString() + " (" + ((SighNode) it).span.startString(lineMap) + ")"));

            // Alternatively, print the whole tree:
            // System.out.println(AttributeTreeFormatter.format(tree, reactor,
            //    new ReflectiveFieldWalker<>(SighNode.class, PRE_VISIT, POST_VISIT)));
            return;
        }

        Interpreter interpreter = new Interpreter(reactor);
        interpreter.run(tree);
        System.out.println("success");
    }
}
