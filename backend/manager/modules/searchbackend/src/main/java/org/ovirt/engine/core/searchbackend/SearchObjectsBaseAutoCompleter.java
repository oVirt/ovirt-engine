package org.ovirt.engine.core.searchbackend;

public class SearchObjectsBaseAutoCompleter extends BaseAutoCompleter {
    @Override
    public String changeCaseDisplay(String text) {
        return text.substring(0, 1).toUpperCase() + (text.substring(0, 0) + text.substring(0 + 1)).toLowerCase();
    }
}
