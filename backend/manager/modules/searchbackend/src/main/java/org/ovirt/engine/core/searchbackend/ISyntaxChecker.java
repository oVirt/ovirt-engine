package org.ovirt.engine.core.searchbackend;

public interface ISyntaxChecker {
    SyntaxContainer analyzeSyntaxState(String searchText, boolean final2);

    SyntaxContainer getCompletion(String searchText);

    String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe);
}
