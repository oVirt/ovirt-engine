package org.ovirt.engine.ui.uicommonweb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.URL;

public class HtmlParameters {

    private final Map<String, List<String>> paramsMap = new HashMap<>();

    public void addParameter(String key, String value) {
        List<String> oldParams = paramsMap.get(key);

        if (oldParams == null) {
            setParameter(key, value);
        } else {
            oldParams.add(encodeParameter(value));
        }
    }

    public void setParameter(String key, String... value) {
        paramsMap.put(key, new LinkedList<>(Arrays.asList(encodeParameters(value))));
    }

    public Map<String, List<String>> getParameters() {
        return Collections.unmodifiableMap(paramsMap);
    }

    public void removeParameter(String key) {
        paramsMap.remove(key);
    }

    private static String[] encodeParameters(String[] values) {
        for (int index = 0; index < values.length; ++index) {
                values[index] = encodeParameter(values[index]);
        }
        return values;
    }

    private static String encodeParameter(String value) {
        return URL.encode(value);
    }

    /**
     * Parse the passed in URL, and take any parameters found and put them in this {@code HtmlParameters} object.
     * This is a naive parse method.
     * This will not handle duplicate parameters, nor empty ones, nor hash tags, or partials.
     * So this will fail: ?a=b&c&d&e=f#g;h even though it is a valid URL.
     * @param baseUrl The {@code String} URL to parse.
     */
    public void parseUrlParams(String baseUrl) {
        if (baseUrl.indexOf('?') >= 0) {
            String params = baseUrl.substring(baseUrl.indexOf('?') + 1);
            for (String keyValue: params.split("&")) { //$NON-NLS-1$
                String[] pair = keyValue.split("="); //$NON-NLS-1$
                if (!"".equals(pair[1])) {
                    setParameter(pair[0], pair[1]);
                }
            }
        }
    }
}
