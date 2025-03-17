package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.ApplyHiddenSearchStringEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Singleton object that listens for search string changes and stores them to be collected later in case interested
 * objects haven't been instantiated yet.
 * This class is mostly copy of the {@link SearchStringCollector}
 */
public class HiddenSearchStringCollector {
    /**
     * Maps default search strings to actual search strings (map models to search strings essentially).
     */
    Map<String, String> hiddenSearchStringMap = new HashMap<>();

    @Inject
    public HiddenSearchStringCollector(EventBus eventBus) {
        eventBus.addHandler(ApplyHiddenSearchStringEvent.getType(), event -> {
            String hiddenSearchString = event.getHiddenSearchString();
            int colonIndex = hiddenSearchString.indexOf(':');
            if (colonIndex >= 0) {
                hiddenSearchStringMap.put(hiddenSearchString.substring(0, colonIndex + 1), hiddenSearchString);
            }
        });
    }

    public String getHiddenSearchStringPrefix(String prefix) {
        String result = null;
        if (prefix != null) {
            result = hiddenSearchStringMap.get(prefix);
            hiddenSearchStringMap.remove(prefix);
        }
        return result;
    }
}
