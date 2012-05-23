package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;

import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.DataProvider;

public class ApplicationModeHelper {

    private static ApplicationMode UI_MODE = ApplicationMode.AllModes;

    public static boolean isAvailableInMode(int availableModes)
    {
        return (availableModes & UI_MODE.getValue()) > 0;
    }

    public static boolean isModeSupported(ApplicationMode mode)
    {
        return (mode.getValue() & UI_MODE.getValue()) > 0;
    }

    public static ApplicationMode getUiMode()
    {
        return UI_MODE;
    }

    public static void setUiMode(ApplicationMode uiMode)
    {
        if (uiMode != null)
        {
            UI_MODE = uiMode;
        }
    }

    public static ArrayList<EventNotificationEntity> getModeSpecificEventNotificationTypeList()
    {
        ArrayList<EventNotificationEntity> subList = new ArrayList<EventNotificationEntity>();
        for (EventNotificationEntity entity : DataProvider.GetEventNotificationTypeList())
        {
            if ((entity.getAvailableInModes() & UI_MODE.getValue()) > 0)
            {
                subList.add(entity);
            }
        }
        return subList;
    }
}
