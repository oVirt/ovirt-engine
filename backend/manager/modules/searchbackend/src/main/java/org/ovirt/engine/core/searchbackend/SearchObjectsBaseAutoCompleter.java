package org.ovirt.engine.core.searchbackend;

public class SearchObjectsBaseAutoCompleter extends BaseAutoCompleter {
    public SearchObjectsBaseAutoCompleter(String... text) {
        super(text);
    }

    public SearchObjectsBaseAutoCompleter(String text) {
        super(text);
    }

    @Override
    public String changeCaseDisplay(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public SearchObjectsBaseAutoCompleter() {
        super();
    }

    public SearchObjectsBaseAutoCompleter(String[] text, String[] noAutocomplete) {
        super(text, noAutocomplete);
    }
}
