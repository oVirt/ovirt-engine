package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public class SpiceConstantsManager {

    private static final SpiceConstantsManager INSTANCE = new SpiceConstantsManager();
    private final SpiceRedKeys spiceRedKeys = GWT.create(SpiceRedKeys.class);

    private SpiceConstantsManager() {
    }

    public static SpiceConstantsManager getInstance() {
        return INSTANCE;
    }

    public SpiceRedKeys getSpiceRedKeys() {
        return spiceRedKeys;
    }
}
