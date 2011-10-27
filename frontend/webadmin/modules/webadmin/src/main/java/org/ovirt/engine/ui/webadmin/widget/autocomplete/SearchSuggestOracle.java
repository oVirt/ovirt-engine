package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SearchSuggestModel;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SuggestItemPartModel;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;

public class SearchSuggestOracle extends MultiWordSuggestOracle {

    private final SearchSuggestModel searchSuggestModel;

    private String searchStringPrefix;

    public SearchSuggestOracle() {
        searchSuggestModel = new SearchSuggestModel();
        searchStringPrefix = "";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {

        // Search string
        String search = request.getQuery();

        // Create suggestions list
        List<SearchSuggestion> suggestions = new ArrayList<SearchSuggestion>();

        // Invoke model update options by search string method
        searchSuggestModel.UpdateOptionsAsync(searchStringPrefix + search);

        // Get options list
        List options = searchSuggestModel.getItems();

        for (Object option : options) {
            List<SuggestItemPartModel> suggestItemPartModelList = (List<SuggestItemPartModel>) option;

            if (suggestItemPartModelList != null) {
                // Add the suggestion list
                suggestions.add(new SearchSuggestion(suggestItemPartModelList, searchStringPrefix));
            }
        }

        // Raise SuggestionsReady event for the specified request
        callback.onSuggestionsReady(request, new Response(suggestions));
    }

    public void setSearchPrefix(String searchStringPrefix) {
        this.searchStringPrefix = searchStringPrefix;
    }

}
