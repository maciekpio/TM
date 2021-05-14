import interpreter.PassthroughException;
import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMap;
import norswap.autumn.positions.LineMapString;
import ast.SighNode;
import interpreter.TMInterpreter;
import norswap.uranium.AttributeTreeFormatter;
import norswap.uranium.Reactor;
import norswap.utils.IO;
import norswap.utils.visitors.Walker;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;

import static norswap.utils.Util.cast;

public final class TMcompiler
{
    public static void main (String[] args) {
        String path;
        String src;
        StringBuilder stringBuilder = new StringBuilder("let args = [");
        final int PRE = stringBuilder.length();
        boolean seeWalker = false;

        if (args.length == 0){
            String file = "TM_code_samples.tm";
            path = Paths.get("examples/", file).toAbsolutePath().toString();
            src = IO.slurp(path);
        }
        else if (args[0].equals("-walker") && args.length == 1){
            seeWalker = true;
            String file = "TM_code_samples.tm";
            path = Paths.get("examples/", file).toAbsolutePath().toString();
            src = IO.slurp(path);
        }
        else{
            try {
                path = Paths.get(args[0]).toAbsolutePath().toString();
                src = IO.slurp(path);
                if (src == null)
                    throw new PassthroughException(new Error("The first arg \"" + path + "\" must be a valid path to a readable file"));
            } catch (InvalidPathException e){
                throw new PassthroughException(new Error("The first arg must be a valid path to a readable file : " + e.toString()));
            }

            if (args.length > 1){
                for (int i = 1; i < args.length; i++){
                    if (args[i].equals("-walker")){
                        seeWalker = true;
                        continue;
                    }
                    stringBuilder.append("\"").append(args[i]).append("\"");
                    if (i+1 < args.length)
                        stringBuilder.append(", ");
                    else
                        stringBuilder.append("]\n");
                }
            }
        }

        if (stringBuilder.length() != PRE)
            src = stringBuilder.append(src).toString();

        TMGrammar grammar = new TMGrammar();
        ParseOptions options = ParseOptions.builder().recordCallStack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);
        LineMap lineMap = new LineMapString(path, src);
        System.out.println(result.toString(lineMap, false));

        if (!result.fullMatch)
            return;

        SighNode tree = cast(result.topValue());
        Reactor reactor = new Reactor();
        Walker<SighNode> walker = TMSemantic.createWalker(reactor);
        walker.walk(tree);
        reactor.run();

        if (seeWalker)
            System.out.println(AttributeTreeFormatter.formatWalkFields(tree, reactor, SighNode.class));

        if (!reactor.errors().isEmpty()) {
            System.out.println(reactor.reportErrors(it ->
                it.toString() + " (" + ((SighNode) it).span.startString(lineMap) + ")"));
            return;
        }

        TMInterpreter interpreter = new TMInterpreter(reactor);
        interpreter.interpret(tree);
        System.out.println("SUCCESS");
    }
}
