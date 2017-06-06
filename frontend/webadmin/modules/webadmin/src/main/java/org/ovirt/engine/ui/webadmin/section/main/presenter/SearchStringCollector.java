package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Singleton object that listens for search string changes and stores them to be collected later in case interested
 * objects haven't been instantiated yet.
 */
public class SearchStringCollector {
    /**
     * Maps default search strings to actual search strings (map models to search strings essentially).
     */
    Map<String, String> searchStringMap = new HashMap<>();

    @Inject
    public SearchStringCollector(EventBus eventBus) {
        eventBus.addHandler(ApplySearchStringEvent.getType(), event -> {
            String searchString = event.getSearchString();
            int colonIndex = searchString.indexOf(':');
            if (colonIndex >= 0) {
                searchStringMap.put(searchString.substring(0, colonIndex + 1), searchString);
            }
        });
    }

    public String getSearchStringPrefix(String prefix) {
        String result = null;
        if (prefix != null) {
            result = searchStringMap.get(prefix);
            searchStringMap.remove(prefix);
        }
        return result;
    }
}
