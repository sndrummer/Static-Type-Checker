# Intro

The purpose of this assignment is to get more practice with the visitor pattern. In this instance, it will be important for you to think carefully about how your visitor maintains state.

This visitor will examine Java code for undeclared types. For example,

    public void doStuff() {
        int x = 3;
        String s = "I have character.";
    }

uses three types: `void`, `int`, and `String`. All three of these types are built into Java and are therefore declared types. However,

    public void doOtherStuff() {
        int y = 4;
        SomeClass s = null;
    }

may or may not be safe. It depends on whether or not **SomeClass** is defined in the current package or imported in the current file.

Additionally, you must consider Java generics, such as the following:

    class Storage<E> {
        private ArrayList<E> store;
    }

In this last example, `E` stands in for a type. It represents a type within the environment of the **Storage** class but does not define a type elsewhere.

Lastly, classes may be defined within other classes, as follows:

    class Outer {
        class Inner {
        }
    }

In this example, methods that belong to the **Outer** class may reference **Inner** directly. Elsewhere, **Inner** can be referenced by a qualified name: `Outer.Inner`.

# Lab

Your task is to use the visitor pattern to correctly identify undeclared types in Java code. Use the same visitor skeleton as before.

You must handle `import` statements. Wildcard `import` statements are not required. Test files must explicitly import classes that they use from `java.lang`.

Any reference to an undeclared type should result in a clear error message being sent to the terminal. Please note that the analysis does not check the use of a variable but rather the declaration of a variable, and **it only checks that the declared type at the point of declaration is a known type**. As always, your code must pass SonarLint; as such, you will need to use a logger instead of referencing `System.out` directly.

## Structure

It is recommended that you accomplish this task by creating two different visitors. The first should traverse **ImportDeclaration** and **TypeDeclaration** nodes and should assemble a set of types that are declared in the program. The **PackageDeclaration** node can be used to determine the fully qualified name of each declared class. `import` statements make classes available without qualifiers, so be sure to record the unqualified class names that are imported.

The second visitor should traverse the program again, using **TypeDeclaration** nodes to keep track of the current environment. In the example that introduces **Outer** and **Inner** (if we assume that it occurs in the package `pack.current`), the environment within the **Outer** class is `pack.current.Outer`. As type parameters (e.g., `<E>`) are defined per-environment, it may be useful to create a data structure that associates each environment with its type parameters. Remember that environments nest inside of each other!

The second visitor should visit subtypes of the **Type** class, such as **SimpleType** and **QualifiedType**. In each case, it should check to make sure that the type in question is declared in the current environment.

## Tips

* A **TypeDeclaration** node is visited before its children. You may find the `endVisit()` method useful for cleaning up after visiting a **TypeDeclaration** and its descendants.
* The design of the second visitor is simplified by the use of a method that checks a type name in the current environment. With such a method, the `visit()` method for each subtype of **Type** can use the same code to check that a type has been declared.
* You may find it useful to create a helper class to store the results of the first visitor so they can be easily passed to the second.
* Using different logging levels can simplify debugging. For example, `logger.debug()` could be used to print the names of types that are declared in the current environment.
* You do not need to consider class visibility (e.g., `public`, `protected`, or `private` access levels).

# Testing

Use Black-box testing to test your visitor. In a text file or in comments, explain the methodology used and why the test cases are sufficient with respect to that methodology.

# Grading

* 15 points for basic functionality (no inner classes or type parameters)
* 10 additional points for also handling inner classes
* 5 additional points for also handling type parameters
* 20 points for sufficient black-box tests to test basic functionality
* 5 points for black-box tests that also check inner class functionality
* 5 points for black-box tests that also check support for type parameter
* 10 points for code style (passing SonarLint and legibility)
* 15 points for documentation of visitor code
* 15 points for documentation of black-box tests
