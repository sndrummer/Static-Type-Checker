# Intro

Your task is to write code using the visitor pattern that assembles a symbol table for a given Java program. This is required for type checking. In particular, you must assemble information about the classes, fields, and methods declared in the program. The result should be a table that allows you to return a type when given an identifier.

# Requirements

Naturally, there are many language features in Java that are beyond the scope of this class. The language features that your type checker will need to support are:

* Fields
* Number literals
* String literals
* Binary operators
* Unary operators
* Method calls (without virtual dispatch)
* Casting (see below)
* Local variables
* Assignments
* Control flow statements (`if`, `for`, `while`, `switch`, `continue`, `break`)

Most of these language features are not relevant to the symbol checker; however, they will be a crucial part of your type checker.

Your code should assume that all imported classes and all classes referenced by fully-qualified names matching  `java.*` exist.

## Casting

Casting is difficult to reason about generally; however, one special case is tractable (assume that the environment knows that `y` is of type **C**):

```java
if (x instanceof C) y = (C) x;
```

# Interface

The **ISymbolTable** interface has been added to the `edu.byu.yc` package in the visitor skeleton. Your visitor must produce an implementation of this interface after visiting a Java file. The implementation may use stubs for `addLocal()` and `removeLocal()` (in other words, you are not required to have a working implementation of either of these methods; just add empty functions).

# Testing

Use black-box testing to test your visitor. Since the symbol table does not validate programs, there are no "correct" or "incorrect" programs. Instead, consider which equivalence classes you can test. Explain your choice of tests in Javadoc comments or in a text file. In either case, your tests and your explanation of why they are sufficient must be a part of the patch you submit.

# Recommendations

The same circular dependence problem that affects the Undeclared Type Checker affects the generation of a symbol table. The recommended solution is the same: do two passes. The first should look at `import` statements and type declarations. The second should examine field and method declarations.

The **ISymbolTable** interface operates mostly on fully-qualified names (FQNs). It is recommended that declared types be stored as fully-qualified names. In contrast, it is recommended that imported names be stored without their qualifiers. In the following code,

```java
package edu.byu;

import java.util.HashSet;

class C {
    HashSet s;
}
```

it is recommended that the symbol table contain entries for `edu.byu.C` and `HashSet`.

Remember that the purpose of this assignment is not for you to implement a type checker for the next version of Java. If you come across some corner case that seems unreasonable, it is likely that you are not expected to implement it. Feel free to double-check with TAs or instructors and simply include comments such as:

    // We assume that all inner classes of imports are valid

These comments make grading easier and also allow the instructors to clarify this assignment for future semesters.

# Submission

Create a patch for a single commit with respect to the master branch of the visitor skeleton. Be sure to include Javadoc comments with an `@author` tag in each source file. If you work with a partner, make sure both names are in each `@author` tag. Submit your patch in Canvas.

If you work with a partner, only one of you needs to submit a patch. However, the other member of the group should submit a brief text file that indicates his/her partner's name. Something as simple as

    I worked with Jane Doe on this project and she submitted our patch.

is sufficient.

# Grading

* 10 points for implementing the existence functions (`classExists()`, etc.)
* 15 additional points for also collecting appropriate return types (`getFieldType()`, etc.)
* 5 additional points for also creating a suitable `toString()` function
* 15 points for sufficient black-box tests to test existence functions
* 15 points for black-box tests that also check return types
* 10 points for code style (passing SonarLint and legibility)
* 15 points for documentation of visitor code
* 15 points for documentation of black-box tests
