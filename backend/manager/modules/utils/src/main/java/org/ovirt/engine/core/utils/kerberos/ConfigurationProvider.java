package org.ovirt.engine.core.utils.kerberos;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.config.ConfigValues;

public class ConfigurationProvider {

    private String AdUserName;
    private String AdUserPassword;
    private String DomainName;
    private String LDAPSecurityAuthentication;
    private String LdapServers;
    private String AdUserId;
    private String engineConfigExecutable;
    private String engineConfigProperties;
    private final static Logger log = Logger.getLogger(ManageDomainsDAOImpl.class);

    public ConfigurationProvider(String adUserName,
            String adUserPassword,
            String domainName,
            String ldapSecurityAuthentication,
            String ldapServers,
            String adUserId,
            String engineConfigExecutable,
            String engineConfigProperties) {
        super();
        AdUserName = adUserName;
        AdUserPassword = adUserPassword;
        DomainName = domainName;
        LDAPSecurityAuthentication = ldapSecurityAuthentication;
        LdapServers = ldapServers;
        AdUserId = adUserId;
        this.engineConfigExecutable = engineConfigExecutable;
        this.engineConfigProperties = engineConfigProperties;
    }

    public String getConfigValue(ConfigValues enumValue) {
        if (enumValue.equals(ConfigValues.AdUserName)) {
            return AdUserName;
        } else if (enumValue.equals(ConfigValues.AdUserPassword)) {
            return AdUserPassword;
        } else if (enumValue.equals(ConfigValues.LDAPSecurityAuthentication)) {
            return LDAPSecurityAuthentication;
        } else if (enumValue.equals(ConfigValues.DomainName)) {
            return DomainName;
        } else if (enumValue.equals(ConfigValues.LdapServers)) {
            return LdapServers;
        } else if (enumValue.equals(ConfigValues.AdUserId)) {
            return AdUserId;
        } else {
            return "";
        }
    }

    public void setConfigValue(ConfigValues enumValue, String value, String loggingValue) throws ManageDomainsResult {

        log.info("Setting value for " + enumValue.toString() + " to " + loggingValue);

        try {
            Process engineConfigProcess =
                    Runtime.getRuntime().exec(engineConfigExecutable + " -s "
                            + enumValue.name() + "="
                            + value + " -p " + engineConfigProperties);
            int retVal = engineConfigProcess.waitFor();
            if (retVal != 0) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION,
                        enumValue.name());
            }
        } catch (IOException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION_WITH_DETAILS,
                    new String[] { enumValue.name(), e.getMessage() });
        } catch (InterruptedException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION_WITH_DETAILS,
                    new String[] { enumValue.name(), e.getMessage() });
        }

    }
}
