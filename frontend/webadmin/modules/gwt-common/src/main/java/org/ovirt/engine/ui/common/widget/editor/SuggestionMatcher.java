package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 * A matcher used by {@link RenderableSuggestOracle} to recognize suggestions corresponding to user query string.
 */
public interface SuggestionMatcher {

    /**
     *
     * @param query text typed by user into combobox
     * @param suggestion suggestion to match
     * @return is suggestion matching
     */
    public boolean match(String query, MultiWordSuggestOracle.MultiWordSuggestion suggestion);

    /**
     * Case independent matcher. Suggestion matches if replacementString starts with query.
     */
    public static class StartWithSuggestionMatcher implements SuggestionMatcher {

        @Override
        public boolean match(String query, MultiWordSuggestOracle.MultiWordSuggestion suggestion) {
            if (suggestion.getReplacementString() == null || query == null) {
                return false;
            }

            return suggestion.getReplacementString().toLowerCase().startsWith(query.toLowerCase());
        }
    }

    /**
     * Case independent matcher. Suggestion matches if all whitespace separated parts of query are contained in
     * replacement string.
     */
    public static class ContainsSuggestionMatcher implements SuggestionMatcher {

        @Override
        public boolean match(String query, MultiWordSuggestOracle.MultiWordSuggestion suggestion) {
            if (suggestion.getReplacementString() == null || query == null) {
                return false;
            }

            final String lowerQuery = query.toLowerCase();
            final String lowerReplacement = suggestion.getReplacementString().toLowerCase();
            final String[] querySegments = lowerQuery.split("\\s"); //$NON-NLS-1$
            for (String querySegment : querySegments) {
                if (!lowerReplacement.contains(querySegment)) {
                    return false;
                }
            }
            return true;
        }
    }
}
