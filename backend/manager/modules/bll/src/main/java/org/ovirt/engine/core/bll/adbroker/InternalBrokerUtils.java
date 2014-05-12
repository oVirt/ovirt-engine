package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;

public class InternalBrokerUtils {

    private static final ExternalId ADMIN_ID = new ExternalId(
        0xfd, 0xfc, 0x62, 0x7c, 0xd8, 0x75, 0x11, 0xe0,
        0x90, 0xf0, 0x83, 0xdf, 0x13, 0x3b, 0x58, 0xcc
    );

    private static DbUserDAO getDbUserDAO() {
        return DbFacade.getInstance().getDbUserDao();
    }

    public static LdapGroup getGroupById(ExternalId id) {
        return null;
    }

    public static LdapUser getUserById(ExternalId id) {
        LdapUser retVal = null;
        DbUser dbUser = getDbUserDAO().getByExternalId(Config.<String>getValue(ConfigValues.AdminDomain), id);
        if (dbUser != null) {
            retVal = new LdapUser(dbUser);
        }

        return retVal;
    }

    public static LdapUser getUserByUPN(String userName) {
        LdapUser retVal = null;
        DbUser dbUser = getDbUserDAO().getByUsernameAndDomain(userName, "internal");
        if (dbUser != null) {
            retVal = new LdapUser(dbUser);
        }

        return retVal;
    }

    public static List<LdapGroup> getAllGroups() {
        return new ArrayList<LdapGroup>();
    }

    public static List<LdapUser> getAllUsers() {
        List<LdapUser> users = new ArrayList<LdapUser>();
        LdapUser user = getUserById(ADMIN_ID);
        if (user != null) {
            users.add(user);
        }
        return users;
    }

    public static UserAuthenticationResult authenticate(String userName, String password, String domain) {
        UserAuthenticationResult result = null;
        String adminPassword = Config.<String> getValue(ConfigValues.AdminPassword).trim();
        String adminUser = Config.<String> getValue(ConfigValues.AdminUser).trim();
        String adminDomain = Config.<String> getValue(ConfigValues.AdminDomain).trim();

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
