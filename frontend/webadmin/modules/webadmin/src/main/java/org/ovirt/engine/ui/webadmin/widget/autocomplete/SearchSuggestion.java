package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SuggestItemPartModel;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SearchSuggestion implements Suggestion {

    private String searchStringPrefix;
    private SuggestItemPartModel prefix;
    private SuggestItemPartModel postfix;

    public SuggestItemPartModel getSuggestionPrefix() {
        return prefix;
    }

    public SuggestItemPartModel getSuggestionPostfix() {
        return postfix;
    }

    public SearchSuggestion(List<SuggestItemPartModel> suggestItemPartModelList, String searchStringPrefix) {
        this.prefix = suggestItemPartModelList.get(0);
        this.postfix = suggestItemPartModelList.get(1);
        this.searchStringPrefix = searchStringPrefix;
    }

    @Override
    public String getReplacementString() {
        String replacementString = prefix.getPartString() + postfix.getPartString();
        if (replacementString.startsWith(searchStringPrefix)) {
            replacementString = replacementString.replace(searchStringPrefix, ""); //$NON-NLS-1$
        }
        return replacementString;
    }

    @Override
    public String getDisplayString() {
        return prefix.getPartString() + postfix.getPartString();
    }

}
