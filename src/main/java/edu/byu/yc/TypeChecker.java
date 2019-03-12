package edu.byu.yc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Peter Aldous <aldous@cs.byu.edu>
 * 
 */

public class TypeChecker {
    private static Logger logger = LoggerFactory.getLogger(TypeChecker.class);

    /**
     * Expand directory names to their contained Java files.
     * 
     * @param paths An array of file or directory names to be expanded.
     * @return A sequence of paths to Java files, either from paths or contained in
     *         a directory specified by paths.
     */
    private static ArrayList<String> expand(final String[] paths) {
        ArrayList<String> result = new ArrayList<>(Math.max(10, paths.length));
        for (final String path : paths) {
            final File f = new File(path);
            if (f.isDirectory()) {
                final File[] children = f.listFiles();
                String[] childPaths = new String[children.length];
                for (int i = 0; i < children.length; ++i) {
                    childPaths[i] = children[i].getPath();
                }
                result.addAll(expand(childPaths));
            } else {
                if (path.endsWith(".java")) {
                    result.add(f.getPath());
                }
            }
        }
        return result;
    }

    /**
     * Parse all of the Java files in paths and return the concatenation of their
     * contents.
     * 
     * @param paths A sequence of paths to Java files.
     * @return A single AST representing all file contents.
     */
    private static ASTNode parseAll(final ArrayList<String> paths) {

        StringBuilder ssb = new StringBuilder();
        for (final String path : paths) {
            ssb.append(readFile(path));
        }
        final String sourceString = ssb.toString();
        if (sourceString.isEmpty()) {
            logger.error("No java source found");
            System.exit(-1);
        }
        return parse(sourceString);
    }

    /**
     * Read the file at path and return its contents as a String.
     * 
     * @param path The location of the file to be read.
     * @return The contents of the file as a String.
     */
    static String readFile(final String path) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(path)));
        } catch (IOException ioe) {
            logger.debug(ioe.getMessage());
        }
        return "";
    }

    /**
     * Parse the given source.
     * 
     * @param sourceString The contents of some set of Java files.
     * @return An ASTNode representing the entire program.
     */
    static ASTNode parse(final String sourceString) {
        ASTParser p = ASTParser.newParser(AST.JLS3);
        p.setKind(ASTParser.K_COMPILATION_UNIT);
        p.setSource(sourceString.toCharArray());
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        p.setCompilerOptions(options);
        return p.createAST(null);
    }

    /**
     * Get the names of every identifier whose letters are all capitalized in node.
     * 
     * @param node An AST to be analyzed.
     * @return The set of all identifiers in all caps contained in the AST.
     */
    public static Map<List<String>, String> getSymbolTable(ASTNode node) {
        final SymbolTableVisitor v = new SymbolTableVisitor();
        node.accept(v);
        return v.getSymbolTable();
    }

	public static Set<String> getAllCaps(ASTNode node) {
		final NoAllCapsVisitor v = new NoAllCapsVisitor();
		node.accept(v);
		return v.getAllCaps();
	}
    
    public static void main(String[] args) {
        ASTNode node = parseAll(expand(args));

        Map<List<String>, String> st = getSymbolTable(node);
        TypeCheckerVisitor v = new TypeCheckerVisitor(st);
        node.accept(v);
    }
}
