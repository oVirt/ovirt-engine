package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public final class ConstantsManager {

    private static final ConstantsManager INSTANCE = new ConstantsManager();
    private static final Constants constants = GWT.create(Constants.class);
    private static final Messages messages = GWT.create(Messages.class);

    private ConstantsManager() {
    }

    public static ConstantsManager getInstance() {
        return INSTANCE;
    }

    public Constants getConstants() {
        return constants;
    }

    public Messages getMessages() {
        return messages;
    }

}
