package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.system.ClientStorage;

import com.google.gwt.user.client.ui.IsWidget;

public class SubTabHelper {
    private static final int subTabPanelMaxHeight = 300;
    private static final String SUB_TAB_HEIGHT_KEY = "subtabHeight"; //$NON-NLS-1$

    private SubTabHelper() {
        // Don't allow instances of this class.
    }

    public static void storeSubTabHeight(ClientStorage clientStorage, IsWidget widget) {
        clientStorage.setLocalItem(SUB_TAB_HEIGHT_KEY, String.valueOf(widget.asWidget().getOffsetHeight()));
    }

    public static int getSubTabHeight(ClientStorage clientStorage, IsWidget widget) {
        int subTabHeight;
        String storedHeight = clientStorage.getLocalItem(SUB_TAB_HEIGHT_KEY);
        if (storedHeight != null) {
            try {
                subTabHeight = Integer.parseInt(storedHeight);
            } catch (NumberFormatException nfe) {
                //Default to max height if stored value is invalid.
                subTabHeight = subTabPanelMaxHeight;
            }
        } else {
            subTabHeight = widget.asWidget().getOffsetHeight() / 2;
            if (subTabHeight > subTabPanelMaxHeight) {
                subTabHeight = subTabPanelMaxHeight;
            }
        }
        return subTabHeight;
    }

}
