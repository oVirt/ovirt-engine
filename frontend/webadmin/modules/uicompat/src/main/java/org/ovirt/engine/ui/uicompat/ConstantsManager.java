package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public final class ConstantsManager {

    private static final ConstantsManager INSTANCE = new ConstantsManager();
    private static final UIConstants constants = GWT.create(UIConstants.class);
    private static final UIMessages messages = GWT.create(UIMessages.class);

    private ConstantsManager() {
    }

    public static ConstantsManager getInstance() {
        return INSTANCE;
    }

    public UIConstants getConstants() {
        return constants;
    }

    public UIMessages getMessages() {
        return messages;
    }

}
