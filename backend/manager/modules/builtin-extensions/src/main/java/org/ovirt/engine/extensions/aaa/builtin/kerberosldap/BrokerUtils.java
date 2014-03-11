package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class BrokerUtils {

    public static String getLoginDomain(String loginName, String domain) {
        String retVal = domain;
        if (loginName.matches(".+@.+")) {
            String[] loginNameParts = loginName.split("@");
            retVal = loginNameParts[1];
        }
        return retVal;
    }

}
