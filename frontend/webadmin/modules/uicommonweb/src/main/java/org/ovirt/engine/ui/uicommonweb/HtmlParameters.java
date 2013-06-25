package org.ovirt.engine.ui.uicommonweb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.URL;

public class HtmlParameters {

    private final Map<String, List<String>> paramsMap = new HashMap<String, List<String>>();

    public void addParameter(String key, String value) {
        List<String> oldParams = paramsMap.get(key);

        if (oldParams == null) {
            setParameter(key, value);
        } else {
            oldParams.add(encodeParameter(value));
        }
    }

    public void setParameter(String key, String... value) {
        paramsMap.put(key, new LinkedList<String>(Arrays.asList(encodeParameters(value))));
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
}
