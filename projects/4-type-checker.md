# Intro

Your task is to write type checker using the [symbol table](3-symbol-table.md) from the previous project. The type checker should produce a proof certificate of the type correctness in the form of a JUnit 5 `DynamicNode`. If the node passes JUnit, then it is a type proof of the system. If the node fails, then the failed test(s) show where the type proof is unable to show the correctness of the program.

# Requirements

The type checker should support all the language features indicated in the write-up for the [symbol table](3-symbol-table.md) with the assumptions on imports and variables declared with fully-qualified names into the `java` package. Additionally, assume that function calls to fully-qualified names in the `java` package (e.g., `java.lang.HashSet`) type check; whatever arguments they're given are correct and whatever they return is correct.

You do not have to consider a type hierarchy for objects; however, you must check primitives for type compatibility. You must support widening conversions (see the left side of slide 21 in the [type checking slides](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/compilers/09-type-checking.ppt) - conversions that ascend the tree are widening).

## Proof Certificates 

The proof certificate is a proof tree in the form of a JUnit 5 `DynamicNode`. A `DynamicNode` can be either a `DynamicTest` or a `DynamicContainer` that contains a stream of `DynamicNodes`. In general, there should be a container for each node in the type proof tree, and a test for each obligation in the node. Nodes in a type proof tree are anything separated by a horizontal line.

An example is worth much more than any description. Consider the `TypeCheckerDynamicTestVisitor`. The class creates a proof certificate for infix expressions. The top-level container in the statement that *E |- a+b\*p*. The sub-containers and sub-tests are the obligations needed for the proof.

The output from the type checker should be a similar proof certificate.

# Interface

The interface should be a class with a method that has the `@TestFactory` annotation that when run as a JUnit test creates the proof certificate. It should be relatively easy to change the input program, and there should be several input programs available to run.

# Testing

Use white-box testing to test your visitor. Use branch coverage as the coverage target. Document and report errors found in the white-box testing. Additionally, apply black-box testing to the proof-certificate. Visual inspection is sufficient for the black-box tests (i.e., valid proof certificate, invalid proof certificate, where the certificate fails can be visually inspected).

In order to test on multiple inputs, you may need to change the way that JUnit is invoked. `TypeCheckerDynamicTestVisitorTests.java` now uses a system property called `testfile`, which reads an argument passed to the JVM (_not_ the program) in the run configuration: `-Dtestfile=Adder.java`. It is possible to combine multiple launches into a single coverage session. It is also possible to use the JUnit 5 Launcher system to invoke tests from a Java program invoked as an application and not as a JUnit test. This latter approach has the disadvantage of not displaying the tests in a GUI. More details will follow.

# Advice 

Start small and add one language feature at a time. Perhaps consider the following

* Produce a certificate for an empty class (not much to do)
* Produce a certificate for a class with a few fields (each field much be a valid type)
* Produce a certificate for a class with a few fields and an empty method (i.e., no statements)
* Produce a certificate for a class with a few fields and a method that does a single field access (or other simple statement)
* Produce a certificate for a class with a few fields and a method that has a sequence of field accesses
* Add one new class of statements at a time to the method generating its proof certificate and then repeat.

# Submission

Create a patch for a single commit with respect to the master branch of the visitor skeleton. Be sure to include Javadoc comments with an `@author` tag in each source file. If you work with a partner, make sure both names are in each `@author` tag. Submit your patch in Canvas.

If you work with a partner, only one of you needs to submit a patch. However, the other member of the group should submit a brief text file that indicates his/her partner's name. Something as simple as

    I worked with Jane Doe on this project and she submitted our patch.

is sufficient.

# Grading

* 30 points for a type checker that generates a JUnit 5 `DynamicNode` that is a proof certificate of the type correctness of the program.
* 15 points for sufficient white-box tests for branch coverage the code for the type checker
* 15 points for additional black-box tests on the proof certificate
* 10 points for code style (passing SonarLint and legibility)
* 15 points for documentation of visitor code
* 15 points for documentation of all tests
