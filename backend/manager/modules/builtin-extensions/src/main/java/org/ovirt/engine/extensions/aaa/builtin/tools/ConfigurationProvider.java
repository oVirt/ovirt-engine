package org.ovirt.engine.extensions.aaa.builtin.tools;

import static org.ovirt.engine.core.common.config.ConfigValues.AdUserId;
import static org.ovirt.engine.core.common.config.ConfigValues.AdUserName;
import static org.ovirt.engine.core.common.config.ConfigValues.AdUserPassword;
import static org.ovirt.engine.core.common.config.ConfigValues.ChangePasswordMsg;
import static org.ovirt.engine.core.common.config.ConfigValues.DomainName;
import static org.ovirt.engine.core.common.config.ConfigValues.LDAPProviderTypes;
import static org.ovirt.engine.core.common.config.ConfigValues.LDAPSecurityAuthentication;
import static org.ovirt.engine.core.common.config.ConfigValues.LDAPServerPort;
import static org.ovirt.engine.core.common.config.ConfigValues.LdapServers;
import static org.ovirt.engine.core.common.config.ConfigValues.SASL_QOP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProvider {

    private EnumMap<ConfigValues, String> configVals = new EnumMap<ConfigValues, String>(ConfigValues.class);
    private String engineConfigExecutable;
    private String engineConfigProperties;
    private final static Logger log = LoggerFactory.getLogger(ManageDomainsDAOImpl.class);

    public ConfigurationProvider(String adUserName,
            String adUserPassword,
            String domainName,
            String ldapSecurityAuthentication,
            String ldapServers,
            String adUserId,
            String ldapProviderTypes,
            String engineConfigExecutable,
            String engineConfigProperties, String ldapServerPort, String passwordChangeUrls, String saslQOP) {
        super();
        configVals.put(AdUserName, adUserName);
        configVals.put(AdUserPassword, adUserPassword);
        configVals.put(DomainName, domainName);
        configVals.put(LDAPSecurityAuthentication, ldapSecurityAuthentication);
        configVals.put(LdapServers, ldapServers);
        configVals.put(AdUserId, adUserId);
        configVals.put(LDAPProviderTypes, ldapProviderTypes);
        configVals.put(LDAPServerPort, ldapServerPort);
        configVals.put(ChangePasswordMsg, passwordChangeUrls);
        configVals.put(SASL_QOP, saslQOP);
        this.engineConfigExecutable = engineConfigExecutable;
        this.engineConfigProperties = engineConfigProperties;
    }

    public String getConfigValue(ConfigValues enumValue) {
        if (configVals.containsKey(enumValue)) {
            return configVals.get(enumValue);
        }
        return "";
    }

    public void setConfigValue(ConfigValues enumValue, DomainsConfigurationEntry entry) throws ManageDomainsResult {
        setConfigValue(enumValue, entry, true);
    }

    protected File createPassFile(String value) throws IOException {
        File temp = File.createTempFile("ovirt", ".tmp");
        String filePath = temp.getAbsolutePath();

        Process chmodProcess = Runtime.getRuntime().exec("chmod 600 " + filePath);
        try {
            int chmodExitCode = chmodProcess.waitFor();
            if (chmodExitCode != 0) {
                throw new IOException("Failed to change permissions for file \"" + filePath
                        + "\", the chmod command returns exit code " + chmodExitCode + "\".");
            }
        } catch (InterruptedException ie) {
            throw new IOException("Failed to change permissions for file \"" + filePath + "\"", ie);
        }

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(temp));
            out.write(value);
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
        return temp;
    }

    public void setConfigValue(ConfigValues enumValue, DomainsConfigurationEntry entry, boolean passedAsValue)
            throws ManageDomainsResult {

        log.info("Setting value for {} to {}", enumValue.toString(), entry.getDomainsLoggingEntry());
        File passFile = null;

        try {
            StringBuilder executeCmd = new StringBuilder(engineConfigExecutable);
            executeCmd.append(" -s ").append(enumValue.name());
            if (passedAsValue) {
                executeCmd.append("=" + entry.getDomainsConfigurationEntry());
            } else {
                passFile = createPassFile(entry.getDomainsConfigurationEntry());
                executeCmd.append(" --admin-pass-file " + passFile.getAbsolutePath());
            }
            executeCmd.append(" -p " + engineConfigProperties);
            Process engineConfigProcess = Runtime.getRuntime().exec(executeCmd.toString());

            int retVal = engineConfigProcess.waitFor();
            if (retVal != 0) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION,
                        enumValue.name() + " - execute command: " + executeCmd.toString());
            }
        } catch (Throwable e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION_WITH_DETAILS,
                    enumValue.name(), e.getMessage());
        } finally {
            disposePassFile(passFile);
        }
    }

    protected void disposePassFile(File file) {
        if (file != null) {
            if (file.exists()) {
                if (!file.delete()) {
                    log.info("Failed deleting file {}. Continuing anyway.", file.getAbsolutePath());
                }
            }
        }
    }
}
