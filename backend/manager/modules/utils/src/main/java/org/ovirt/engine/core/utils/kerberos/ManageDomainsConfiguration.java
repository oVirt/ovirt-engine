package org.ovirt.engine.core.utils.kerberos;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ManageDomainsConfiguration {

    private PropertiesConfiguration manageDomainsConf;
    private static String JAAS_FILE_PROPERTY = "jaasFile";
    private static String KRB5_FILE_PROPERTY = "krb5confFile";
    private static String ENGINE_CONFIG_EXECUTABLE_PROPERTY = "engineConfigExecutable";
    private static String LOCAL_HOST_ENTRY = "localHostEntry";
    private static String USE_DNS_LOOKUP = "useDnsLookup";

    ManageDomainsConfiguration(String confFilePath) throws ConfigurationException {
        manageDomainsConf = new PropertiesConfiguration(confFilePath);
    }

    public String getJaasFilePath() {
        return (String) manageDomainsConf.getProperty(JAAS_FILE_PROPERTY);
    }

    public String getkrb5confFilePath() {
        return (String) manageDomainsConf.getProperty(KRB5_FILE_PROPERTY);
    }

    public String getEngineConfigExecutablePath() {
        return (String) manageDomainsConf.getProperty(ENGINE_CONFIG_EXECUTABLE_PROPERTY);
    }

    public String getLocalHostEntry() {
        return (String) manageDomainsConf.getProperty(LOCAL_HOST_ENTRY);
    }

    public boolean getUseDnsLookup() {
        return manageDomainsConf.getBoolean(USE_DNS_LOOKUP);
    }

}
