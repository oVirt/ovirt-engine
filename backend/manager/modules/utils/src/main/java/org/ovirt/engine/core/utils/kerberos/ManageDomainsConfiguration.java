package org.ovirt.engine.core.utils.kerberos;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ManageDomainsConfiguration {

    private PropertiesConfiguration manageDomainsConf;
    private static String JAAS_FILE_PROPERTY = "jaasFile";
    private static String KRB5_FILE_PROPERTY = "krb5confFile";
    private static String JBOSS_DS_FILE_PROPERTY = "jbossDataSourceFile";
    private static String LOGIN_CONFIG_FILE_PROPERTY = "jbossLoginConfigFile";
    private static String ENGINE_CONFIG_EXECUTABLE_PROPERTY = "engineConfigExecutable";
    private static String LOCAL_HOST_ENTRY = "localHostEntry";

    ManageDomainsConfiguration(String confFilePath) throws ConfigurationException {
        manageDomainsConf = new PropertiesConfiguration(confFilePath);
    }

    public String getJaasFilePath() {
        return (String) manageDomainsConf.getProperty(JAAS_FILE_PROPERTY);
    }

    public String getkrb5confFilePath() {
        return (String) manageDomainsConf.getProperty(KRB5_FILE_PROPERTY);
    }

    public String getJbossDataSourceFilePath() {
        return (String) manageDomainsConf.getProperty(JBOSS_DS_FILE_PROPERTY);
    }

    public String getLoginConfigFilePath() {
        return (String) manageDomainsConf.getProperty(LOGIN_CONFIG_FILE_PROPERTY);
    }

    public String getEngineConfigExecutablePath() {
        return (String) manageDomainsConf.getProperty(ENGINE_CONFIG_EXECUTABLE_PROPERTY);
    }

    public String getLocalHostEntry() {
        return (String) manageDomainsConf.getProperty(LOCAL_HOST_ENTRY);
    }

}
