package org.ovirt.engine.ui.uicommonweb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HtmlParameters {

    private final Map<String, List<String>> paramsMap = new HashMap<String, List<String>>();

    public void addParameter(String key, String value) {
        List<String> oldParams = paramsMap.get(key);

        if (oldParams == null) {
            setParameter(key, value);
        } else {
            oldParams.add(value);
        }
    }

    public void setParameter(String key, String... value) {
        paramsMap.put(key, new LinkedList<String>(Arrays.asList(value)));
    }

    public Map<String, List<String>> getParameters() {
        return Collections.unmodifiableMap(paramsMap);
    }

    public void removeParameter(String key) {
        paramsMap.remove(key);
    }
}
