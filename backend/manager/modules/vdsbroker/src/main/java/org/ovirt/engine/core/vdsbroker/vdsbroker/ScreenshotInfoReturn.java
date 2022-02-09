package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class ScreenshotInfoReturn extends StatusReturn {
    private static final String DATA = "data";
    private static final String ENCODING = "encoding";
    private static final String MIME_TYPE = "mime_type";

    public String encodedScreenshotData;
    public String encoding;
    public String mimeType;


    public ScreenshotInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        encodedScreenshotData = innerMap.containsKey(DATA) ? (String) innerMap.get(DATA) : null;
        encoding = innerMap.containsKey(ENCODING) ? (String) innerMap.get(ENCODING) : null;
        mimeType = innerMap.containsKey(MIME_TYPE) ? (String) innerMap.get(MIME_TYPE) : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        builder.append(encoding);
        builder.append("\n");
        builder.append(mimeType);
        return builder.toString();
    }

    public String getEncodedScreenshotInfo() {
        return encodedScreenshotData;
    }
}
