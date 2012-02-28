package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public class BaseUrlUtil {
    static public String getBaseUrl() {
        return GWT.getModuleBaseURL();
    }
}
