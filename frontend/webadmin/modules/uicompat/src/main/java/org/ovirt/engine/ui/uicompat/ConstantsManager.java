package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public abstract class ConstantsManager {

    private static ConstantsManager instance = new GwtConstantsManager();

    public static ConstantsManager getInstance() {
        return instance;
    }

    public static void setInstance(ConstantsManager manager) {
        instance = manager;
    }

    public abstract UIConstants getConstants();
    public abstract UIMessages getMessages();
    public abstract Enums getEnums();

    static class GwtConstantsManager extends ConstantsManager {

        private static UIConstants constants;
        private static UIMessages messages;
        private static Enums enums;

        @Override
        public UIConstants getConstants() {
            if (constants == null) {
                constants = GWT.create(UIConstants.class);
            }
            return constants;
        }

        @Override
        public UIMessages getMessages() {
            if (messages == null) {
                messages = GWT.create(UIMessages.class);
            }
            return messages;
        }

        @Override
        public Enums getEnums() {
            if (enums == null) {
                enums = GWT.create(Enums.class);
            }
            return enums;
        }
    }
}
