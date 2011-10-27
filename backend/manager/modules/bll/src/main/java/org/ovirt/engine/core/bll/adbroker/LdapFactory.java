package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.bll.InternalBrokerImpl;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public final class LdapFactory {

    private static LdapBroker internalInstance;
    private static LdapBroker ldapInstance;
    private static String internalDomain = Config.<String> GetValue(ConfigValues.AdminDomain).trim();

    static {
        internalInstance = new InternalBrokerImpl();
        ldapInstance = new LdapBrokerImpl();
    }

    public static LdapBroker getInstance(String domain) {
        if (domain.equalsIgnoreCase(internalDomain)) {
            return internalInstance;
        } else {
            return ldapInstance;
        }
    }
}
