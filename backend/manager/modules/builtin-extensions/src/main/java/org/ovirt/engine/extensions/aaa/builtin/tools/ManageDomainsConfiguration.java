package org.ovirt.engine.extensions.aaa.builtin.tools;

import java.io.FileInputStream;
import java.util.Properties;

public class ManageDomainsConfiguration {

    private Properties manageDomainsConf;
    private static String JAAS_FILE_PROPERTY = "jaasFile";
    private static String KRB5_FILE_PROPERTY = "krb5confFile";
    private static String ENGINE_CONFIG_EXECUTABLE_PROPERTY = "engineConfigExecutable";
    private static String LOCAL_HOST_ENTRY = "localHostEntry";
    private static String USE_DNS_LOOKUP = "useDnsLookup";
    private static String DOMAIN_REALM_MAPPING_FILE = "domainRealmMappingFile";

    ManageDomainsConfiguration(String confFilePath) throws Exception {
        manageDomainsConf = new Properties();
        try (FileInputStream inputStream = new FileInputStream(confFilePath)) {
            manageDomainsConf.load(inputStream);
        }
    }

    public String getJaasFilePath() {
        return manageDomainsConf.getProperty(JAAS_FILE_PROPERTY);
    }

    public String getkrb5confFilePath() {
        return manageDomainsConf.getProperty(KRB5_FILE_PROPERTY);
    }

    public String getEngineConfigExecutablePath() {
        return manageDomainsConf.getProperty(ENGINE_CONFIG_EXECUTABLE_PROPERTY);
    }

    public String getLocalHostEntry() {
        return manageDomainsConf.getProperty(LOCAL_HOST_ENTRY);
    }

    public boolean getUseDnsLookup() {
        String propValue = manageDomainsConf.getProperty(USE_DNS_LOOKUP);
        return propValue != null ? Boolean.parseBoolean(propValue) : false;
    }

    public String getDomainRealmMappingFile() {
        return manageDomainsConf.getProperty(DOMAIN_REALM_MAPPING_FILE);
    }

}
