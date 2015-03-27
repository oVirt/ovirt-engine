package org.ovirt.engine.ui.common.uicommon;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class ContextSensitiveHelpManager {
    private static Map<String, String> documentationPathMap;

    // GWT overlay for the JSON object containing the help mappings
    private static class Mapping extends JavaScriptObject {
        @SuppressWarnings("unused")
        protected Mapping() {} // required for GWT
    }

    public static String getPath(String helpTag) {
        String path = null;

        if (helpTag != null && documentationPathMap != null) {
            path = documentationPathMap.get(helpTag);
        }

        return path;
    }

    public static void init(String fileContent) {

        // fileContent is a JSON object with all unknown fields
        Mapping mapping = JsonUtils.safeEval(fileContent);
        JSONObject mappingJson = new JSONObject(mapping);

        documentationPathMap = new HashMap<String, String>();

        for (String docTag : mappingJson.keySet()) {
            JSONString urlString = mappingJson.get(docTag).isString();
            if (docTag != null && urlString != null && !docTag.isEmpty() &&
                    !urlString.stringValue().isEmpty() && !documentationPathMap.containsKey(docTag)) {
                documentationPathMap.put(docTag, urlString.stringValue());
            }
        }
    }
}
