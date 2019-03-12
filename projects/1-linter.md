# Linter

The purpose of this lab is to complete a task using a small but nontrivial instance of the visitor pattern. Your task is to write a visitor that, given a Java file, will determine whether or not it complies with some style guidelines. In other words, your visitor should be a linter.

# Requirements

Your linter should use the visitor pattern and must be based on the [Visitor Skeleton](https://bitbucket.org/byucs329/visitor-skeleton) project (where this file is located). The added code should take the form of a visitor or multiple visitors. If the TypeChecker.java file changes, it should change nothing except that it uses your visitors.

You will likely find the [AST View](https://www.eclipse.org/jdt/ui/astview/index.php) Eclipse plug-in to be useful. Some users have trouble installing it on some versions of Eclipse. If you are unable to install it via the Marketplace, you may download the jar file and put it in your `eclipse/[current-version]/eclipse/dropins` directory. The root of this structure is often in your user's home directory and is *not* the same as the `.eclipse` directory, which holds user settings.

You are encouraged but not required to complete this lab with a partner. Your name (as well as the name of your partner, if you have one) must be included in a JavaDoc @author tag in each visitor that you implement. If you do complete this with a partner, use Canvas to create a group for the project.

## Linting rules

Implement at least two of the following four linter rules:

* The leftmost descendant of an assignment ('=') must never be a '+'.
* An `if` statement that is the immediate child of an `else` statement must contain an `else` clause of its own.
* Switch statements may contain no more than six cases (not counting `default`).
* For loops should contain initializers, updaters, or both.

Report all violations to STDOUT. Do not stop at the first violation of a rule.

## Implementation

SonarLint, with its default configuration, must not report anything when it analyzes your code. As a result, you may not use System.out directly. Instead, use an appropriately configured logback logger.

Your visitors must clearly indicate which rules they implement in JavaDoc comments. These comments need not be verbose.

## Testing

You must test your code with JUnit 5 Jupiter tests. For each rule, there must be at least one test case that verifies that a program conforms to the rule and another that verifies that the linter correctly reports a violation. Each test must be clearly documented with JavaDoc.

## Documentation

In some combination of JavaDoc comments and a README.md file, document the following:

* What your program does and how
* Why you believe your tests are adequate
* What was most valuable to you about this experience
* Anything else a grader should know about your program

# Turning it in

Submit a git patch through canvas. Applying this patch to the master branch of the Visitor Skeleton project should transform the working tree into your code. **If you use another IDE besides Eclipse, your patch must not remove Eclipse project files!**
