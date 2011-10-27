package org.ovirt.engine.core.searchbackend;

public interface ISyntaxChecker {
    // C# TO JAVA CONVERTER TODO TASK: final is a keyword in Java. Change the
    // name:
    SyntaxContainer analyzeSyntaxState(String searchText, boolean final2);

    SyntaxContainer getCompletion(String searchText);

    String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe);
}
