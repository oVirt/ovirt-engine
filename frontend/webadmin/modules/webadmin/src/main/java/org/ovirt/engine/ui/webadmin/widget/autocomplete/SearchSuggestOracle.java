package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SearchSuggestModel;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SuggestItemPartModel;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;

public class SearchSuggestOracle extends MultiWordSuggestOracle {

    private final SearchSuggestModel searchSuggestModel;

    private String searchStringPrefix;

    private SearchableListModel<?, ?> model;

    public SearchSuggestOracle() {
        searchSuggestModel = new SearchSuggestModel();
        searchStringPrefix = ""; //$NON-NLS-1$
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
        // Search string
        String search = request.getQuery();

        // Create suggestions list
        List<SearchSuggestion> suggestions = new ArrayList<>();

        // Invoke model update options by search string method
        searchSuggestModel.setSearchObjectFilter(getSearchObjectFilter());
        searchSuggestModel.updateOptionsAsync(searchStringPrefix + search);

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

    /**
     * Returns search object suggestions that should be excluded due to their model being not available.
     */
    private String[] getSearchObjectFilter() {
        List<String> filter = new ArrayList<>();

        if (model != null && !model.getIsAvailable()) {
            String[] searchObjects = model.getSearchObjects();

            if (searchObjects != null) {
                filter.addAll(Arrays.asList(searchObjects));
            }
        }

        return filter.toArray(new String[0]);
    }

    public void setSearchPrefix(String searchStringPrefix) {
        this.searchStringPrefix = searchStringPrefix;
    }

    public void setModel(SearchableListModel<?, ?> model) {
        this.model = model;
    }

}
