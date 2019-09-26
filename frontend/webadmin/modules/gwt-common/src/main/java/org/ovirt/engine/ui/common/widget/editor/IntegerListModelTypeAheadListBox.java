package org.ovirt.engine.ui.common.widget.editor;

/**
 * Type ahead box that accepts only integers but allows custom string to be set for the null value.
 *
 */
public class IntegerListModelTypeAheadListBox extends ListModelTypeAheadListBox<Integer> {

    private String nullString;

    public IntegerListModelTypeAheadListBox(String nullString) {
        super(new IntegerSuggestBoxRenderer(nullString), true, new SuggestionMatcher.StartWithSuggestionMatcher());
        this.nullString = nullString;
    }

    @Override
    protected Integer asEntity(String provided) {
        if (nullString == null || nullString.equals(provided)) {
            return null;
        }
        try {
            return provided == null ? null : Integer.parseInt(provided);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static class IntegerSuggestBoxRenderer implements ListModelTypeAheadListBoxEditor.SuggestBoxRenderer<Integer> {

        private String nullString;

        public IntegerSuggestBoxRenderer(String nullString) {
            this.nullString = nullString;
        }

        public String getReplacementString(Integer data) {
            return data == null
                    ? nullString
                    : data.toString();
        }

        public String getDisplayString(Integer data) {
            return data == null
                    ? nullString
                    : data.toString();
        };
    }
}
