package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.uimode.UiMode;

public class ApplicationModeHelper {

    private static UiMode UI_MODE = UiMode.AllModes;

    public static boolean isAvailableInMode(int availableModes)
    {
        return (availableModes & UI_MODE.getValue()) > 0;
    }

    public static UiMode getUiMode()
    {
        return UI_MODE;
    }

    public static void setUiMode(UiMode uiMode)
    {
        if (uiMode != null)
        {
            UI_MODE = uiMode;
        }
    }
}
