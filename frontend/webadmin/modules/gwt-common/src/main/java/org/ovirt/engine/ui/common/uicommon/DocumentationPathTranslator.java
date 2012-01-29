package org.ovirt.engine.ui.common.uicommon;

import java.util.HashMap;
import java.util.Map;

public class DocumentationPathTranslator {
    private static Map<String, String> documentationPathMap;

    public static String getPath(String hashName)
    {
        String path = null;

        if (hashName != null && documentationPathMap != null) {
            path = documentationPathMap.get(hashName);
        }

        return path;
    }

    public static void init(String fileContent)
    {
        documentationPathMap = new HashMap<String, String>();

        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            String[] parts = line.split(",");

            if (parts.length > 1) {
                String name = parts[0] != null && !parts[0].isEmpty() ? parts[0] : null;
                String path = parts[1] != null && !parts[1].isEmpty() ? parts[1] : null;
                if (name != null && path != null && !documentationPathMap.containsKey(name)) {
                    documentationPathMap.put(name, path);
                }
            }
        }
    }
}
