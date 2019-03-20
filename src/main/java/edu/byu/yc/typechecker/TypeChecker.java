package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import edu.byu.yc.typechecker.symboltable.QualifiedClassVisitor;
import edu.byu.yc.typechecker.symboltable.SymbolTable;
import edu.byu.yc.typechecker.symboltable.SymbolTableVisitor;


/**
 * @author Peter Aldous <aldous@cs.byu.edu>
 */

public class TypeChecker {
    private static Logger logger = LoggerFactory.getLogger(TypeChecker.class);

    /**
     * Expand directory names to their contained Java files.
     *
     * @param paths An array of file or directory names to be expanded.
     * @return A sequence of paths to Java files, either from paths or contained in
     * a directory specified by paths.
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
     * First, get all the class qualified names and then pass them to the symbolTableVisitor
     *
     * @param node ASTNode to be visited
     * @return Symbol Table
     */
    public static SymbolTable createSymbolTable(ASTNode node) {

        final QualifiedClassVisitor classVisitor = new QualifiedClassVisitor();
        node.accept(classVisitor);

        SymbolTable symbolTable = new SymbolTable(classVisitor.getSimpleNameToFullyQualifiedName(), classVisitor.getClassValidators().get(0));
        final SymbolTableVisitor v = new SymbolTableVisitor(symbolTable);
        node.accept(v);

        return symbolTable;
    }


    public static void main(String[] args) {
        ASTNode node = parseAll(expand(args));

        SymbolTable symbolTable = createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(symbolTable);
        node.accept(typeCheckerVisitor);
    }
}
