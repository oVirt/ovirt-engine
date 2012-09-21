package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;

public class InternalBrokerUtils {

    private static final Guid ADMIN_GUID = Guid.createGuidFromString("fdfc627c-d875-11e0-90f0-83df133b58cc");

    private static DbUserDAO getDbUserDAO() {
        return DbFacade.getInstance().getDbUserDao();
    }

    public static ad_groups getGroupByGroupGuid(Guid groupGuid) {
        return null;
    }

    public static AdUser getUserByUserGuid(Guid userGuid) {
        AdUser retVal = null;
        DbUser dbUser = getDbUserDAO().get(userGuid);
        if (dbUser != null) {
            retVal = new AdUser(dbUser);
        }

        return retVal;
    }

    public static AdUser getUserByUPN(String userName) {
        AdUser retVal = null;
        DbUser dbUser = getDbUserDAO().getByUsername(userName);
        if (dbUser != null) {
            retVal = new AdUser(dbUser);
        }

        return retVal;
    }

    public static List<ad_groups> getAllGroups() {
        return new ArrayList<ad_groups>();
    }

    public static List<AdUser> getAllUsers() {
        List<AdUser> users = new ArrayList<AdUser>();
        AdUser user = getUserByUserGuid(ADMIN_GUID);
        if (user != null) {
            users.add(user);
        }
        return users;
    }

    public static UserAuthenticationResult authenticate(String userName, String password, String domain) {
        UserAuthenticationResult result = null;
        String adminPassword = Config.<String> GetValue(ConfigValues.AdminPassword).trim();
        String adminUser = Config.<String> GetValue(ConfigValues.AdminUser).trim();
        String adminDomain = Config.<String> GetValue(ConfigValues.AdminDomain).trim();

        if (userName.equals(adminUser)) {
            if (domain.equalsIgnoreCase(adminDomain)) {
                if (adminPassword.isEmpty()) {
                    result =
                            new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_ACCOUNT_IS_LOCKED_OR_DISABLED);
                } else if (adminPassword.equals(password)) {
                    result = new UserAuthenticationResult();
                } else {
                    result = new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
                }
            } else {
                result = new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            }
        } else {
            result = new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
        }

        return result;
    }
}
