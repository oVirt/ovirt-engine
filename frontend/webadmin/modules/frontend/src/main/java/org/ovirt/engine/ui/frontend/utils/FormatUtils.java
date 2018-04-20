package org.ovirt.engine.ui.frontend.utils;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

/**
 * This class contains static methods useful for formatting text for display in the GUI.
 */
public class FormatUtils {
    /**
     * Format the name of a user for display using the login name, followed by the at sign and the name of the
     * directory.
     *
     * @param user the user object
     */
    public static String getFullLoginName(DbUser user) {
        return getFullLoginName(user.getLoginName(), user.getDomain());
    }

    /**
     * Format the name of a user for display using the login name, followed by the at sign and the name of the
     * directory.
     *
     * @param loginName the login name of the user
     * @param directoryName the name of the directory
     */
    public static String getFullLoginName(String loginName, String directoryName) {
        if (loginName == null || loginName.length() == 0) {
            return "";
        }
        if (directoryName != null && directoryName.length() > 0) {
            return loginName + "@" + directoryName; //$NON-NLS-1$
        } else {
            return loginName;
        }
    }
}
