package org.ovirt.engine.core.searchbackend;

public interface IAutoCompleter {
    String[] getCompletion(String wordPart);

    boolean validate(String text);

    boolean validateCompletion(String text);

    String changeCaseDisplay(String text);
}
