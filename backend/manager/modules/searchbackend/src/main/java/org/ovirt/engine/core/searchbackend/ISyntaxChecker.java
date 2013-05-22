package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

public interface ISyntaxChecker {
    SyntaxContainer analyzeSyntaxState(String searchText, boolean final2);

    SyntaxContainer getCompletion(String searchText);

    String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe);

    void setVmCompletionMap(HashMap<Integer, String> map);

    HashMap<Integer, String> getVmCompletionMap();
}
