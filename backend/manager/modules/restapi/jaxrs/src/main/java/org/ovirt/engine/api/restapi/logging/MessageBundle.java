package org.ovirt.engine.api.restapi.logging;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MessageBundle {

    private Map<String, String> messages;
    private String path;

    public MessageBundle() {
        messages = new HashMap<>();
    }

    public void populate() {
        ResourceBundle bundle = ResourceBundle.getBundle(path);
        for (String key : bundle.keySet()) {
            messages.put(key, bundle.getString(key));
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String localize(Messages message, Object... parameters) {
        String key = message.name();
        return messages.containsKey(key) ? MessageFormat.format(messages.get(key), parameters) : key;
    }

    public String localize(Messages message, Locale locale, Object... parameters) {
        String key = message.name();
        // REVISIT cache bundles by locale
        ResourceBundle bundle = ResourceBundle.getBundle(path, locale);
        return bundle.containsKey(key) ? MessageFormat.format(bundle.getString(key), parameters) : key;
    }
}
