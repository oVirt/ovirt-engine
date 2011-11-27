/**
 *
 */
package org.ovirt.engine.core.utils.kerberos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.CLIParser;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.ipa.ReturnStatus;
import org.ovirt.engine.core.utils.ipa.SimpleAuthenticationCheck;

public class ManageDomains {

    public static final String CONF_FILE_PATH = "/etc/ovirt-engine/engine-manage-domains/engine-manage-domains.conf";
    private final String WARNING_ABOUT_TO_DELETE_LAST_DOMAIN =
            "WARNING: Domain %1$s is the last domain in the configuration. After deleting it you will have to either add another domain, or to use the internal admin user in order to login.";
    private final String WARNING_NOT_ADDING_PERMISSIONS =
        "WARNING: No permissions were added to the Engine. Login either with the internal admin user or with another configured user.";

    private final String SERVICE_RESTART_MESSAGE =
            "oVirt Engine restart is required in order for the changes to take place (service jboss-as restart).";
    private final String DELETE_DOMAIN_SUCCESS =
            "Successfully deleted domain %1$s. Please remove all users and groups of this domain using the Administration portal or the API. "
                    + SERVICE_RESTART_MESSAGE;
    private final String SUCCESSFULLY_COMPLETED_ACTION_ON_DOMAIN =
            "Successfully %1$s domain %2$s. " + SERVICE_RESTART_MESSAGE;

    private final String DEFAULT_AUTH_MODE = LdapAuthModeEnum.GSSAPI.name();
    private final String DOMAIN_SEPERATOR = ",";
    private final String VALUE_SEPERATOR = ":";
    private final String TESTING_KRB5_CONF_SUFFIX = ".manage_domains_utility";
    private final ManageDomainsResult OK_RESULT = new ManageDomainsResult(ManageDomainsResultEnum.OK);
    private ManageDomainsConfiguration utilityConfiguration;
    private ConfigurationProvider configurationProvider;
    private ManageDomainsDAOImpl daoImpl;
    private boolean reportAllErrors;
    private boolean addPermissions;
    private final static Logger log = Logger.getLogger(ManageDomains.class);
    private final static String REMOTE_LOCATION = "remote";

    public enum Arguments {
        action,
        domain,
        user,
        passwordFile,
        servers,
        configFile,
        propertiesFile,
        report,
        interactive,
        addPermissions
    }

    public enum ActionType {
        add,
        edit,
        delete,
        validate,
        list;
    }

    public enum OptionNames {
        AdUserName,
        AdUserPassword,
        DomainName,
        LDAPSecurityAuthentication;
    }

    // This function gets the user name and the domain, and constructs the UPN as follows:
    // If the user already contains the domain (contains @), it just makes it upper-case.
    // Otherwise, it returns a concatenation of the user name + @ + the upper-case domain.
    private static String constructUPN(String userName, String domain) {
        String returnUserName = userName;

        if (userName.contains("@")) {
            String[] parts = userName.split("@");
            int numberOfParts = parts.length;

            switch (numberOfParts) {
            case 1:
                returnUserName = parts[0];
                break;
            case 2:
                returnUserName = parts[0] + '@' + parts[1].toUpperCase();
                break;
            default:
                returnUserName = userName;
                break;
            }
        } else {
            returnUserName = userName + "@" + domain.toUpperCase();
        }
        return returnUserName;
    }

    public ManageDomains() {
    }

    public void init(String configFilePath) throws ManageDomainsResult {

        try {
            utilityConfiguration = new ManageDomainsConfiguration(configFilePath);
        } catch (ConfigurationException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CONFIGURATION, e.getMessage());
        }

        try {
            daoImpl = new ManageDomainsDAOImpl(utilityConfiguration);
        } catch (ConfigurationException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DB_EXCEPTION, e.getMessage());
        } catch (ConnectException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DB_EXCEPTION, e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DB_EXCEPTION, e.getMessage());
        } catch (SQLException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DB_EXCEPTION, e.getMessage());
        } catch (XPathExpressionException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DB_EXCEPTION, e.getMessage());
        }
    }

    private static void exitOnError(ManageDomainsResult result) {
        if (!result.isSuccessful()) {
            System.out.println(result.getDetailedMessage());
            System.exit(result.getExitCode());
        }
    }

    public static void main(String[] args) {
        ManageDomains util;
        util = new ManageDomains();

        CLIParser parser = new CLIParser(args);

        String configFilePath = CONF_FILE_PATH;
        if (parser.hasArg(Arguments.configFile.name())) {
            configFilePath = parser.getArg(Arguments.configFile.name());
        }
        if (parser.hasArg(Arguments.report.name())) {
            util.reportAllErrors = true;
        }
        if (parser.hasArg(Arguments.addPermissions.name())) {
            util.addPermissions = true;
        }

        try {
            // it's existence is checked during the parser validation
            String engineConfigProperties = parser.getArg(Arguments.propertiesFile.name());
            util.init(configFilePath);
            util.validate(parser);
            util.createConfigurationProvider(engineConfigProperties);
            util.runCommand(parser);
        } catch (ManageDomainsResult e) {
            exitOnError(e);
        }
        System.out.println(ManageDomainsResultEnum.OK.getDetailedMessage());
        System.exit(ManageDomainsResultEnum.OK.getExitCode());
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next().replace("\n", "");
    }

    public String getConfigValue(String engineConfigExecutable, String engineConfigProperties, ConfigValues enumValue)
            throws IOException,
            InterruptedException {
        Process engineConfigProcess =
                Runtime.getRuntime().exec(engineConfigExecutable + " -g "
                        + enumValue.name() + " --cver=general" + " -p " + engineConfigProperties);
        int retVal = engineConfigProcess.waitFor();
        if (retVal == 0) {
            InputStream processOutput = engineConfigProcess.getInputStream();
            return convertStreamToString(processOutput);
        } else {
            InputStream errorOutput = engineConfigProcess.getErrorStream();
            throw new FailedReadingConfigValueException(enumValue.name(), convertStreamToString(errorOutput));
        }
    }

    private void createConfigurationProvider(String engineConfigProperties) throws ManageDomainsResult {
        try {
            String engineConfigExecutable = utilityConfiguration.getEngineConfigExecutablePath();
            String adUserName = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.AdUserName);
            String adUserPassword =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.AdUserPassword);
            String ldapSecurityAuthentication =
                    getConfigValue(engineConfigExecutable,
                            engineConfigProperties,
                            ConfigValues.LDAPSecurityAuthentication);
            String domainName = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.DomainName);
            String adUserId = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.AdUserId);
            String ldapServers = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.LdapServers);

            configurationProvider =
                    new ConfigurationProvider(adUserName,
                            adUserPassword,
                            domainName,
                            ldapSecurityAuthentication,
                            ldapServers,
                            adUserId,
                            utilityConfiguration.getEngineConfigExecutablePath(),
                            engineConfigProperties);

        } catch (IOException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CURRENT_CONFIGURATION, e.getMessage());
        } catch (InterruptedException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CURRENT_CONFIGURATION, e.getMessage());
        } catch (FailedReadingConfigValueException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CURRENT_CONFIGURATION, e.getMessage());
        }
    }

    private void runCommand(CLIParser parser) throws ManageDomainsResult {
        String action = parser.getArg(Arguments.action.name());

        ActionType actionType;
        try {
            actionType = ActionType.valueOf(action);
        } catch (IllegalArgumentException ex) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, action);
        }

        if (actionType.equals(ActionType.add)) {
            addDomain(parser.getArg(Arguments.domain.name()).toLowerCase(),
                    parser.getArg(Arguments.user.name()),
                    getPasswordInput(parser),
                    REMOTE_LOCATION);
        } else if (actionType.equals(ActionType.edit)) {
            editDomain(parser.getArg(Arguments.domain.name()).toLowerCase(),
                    parser.getArg(Arguments.user.name()),
                    getPasswordInput(parser),
                    null);
        } else if (actionType.equals(ActionType.delete)) {
            deleteDomain(parser.getArg(Arguments.domain.name()).toLowerCase());
        } else if (actionType.equals(ActionType.validate)) {
            validate();
        } else if (actionType.equals(ActionType.list)) {
            getConfiguration();
        } else {
            throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, action);
        }
    }

    private String getPasswordInput(CLIParser parser) throws ManageDomainsResult {
        String pass = null;

        if (parser.hasArg(Arguments.passwordFile.name())) {
            try {
                String passwordFile = parser.getArg(Arguments.passwordFile.name());
                pass = readPasswordFile(passwordFile);
            } catch (Exception e) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_READING_PASSWORD_FILE, e.getMessage());
            }
        } else if (parser.hasArg(Arguments.interactive.name())) {
            pass = readPasswordInteractively();
        }

        return pass;
    }

    private String readPasswordInteractively() {
        String password = null;
        while (StringUtils.isBlank(password)) {
            System.out.println("Enter password:");
            password = new String(System.console().readPassword());
        }
        return password;
    }

    private String readPasswordFile(String passwordFile) throws FileNotFoundException, IOException {
        FileReader in = new FileReader(passwordFile);
        BufferedReader bufferedReader = new BufferedReader(in);
        String readLine = bufferedReader.readLine();
        FileUtil.closeQuietly(in, bufferedReader);
        return readLine;
    }

    private String getDomainAuthMode(String domainName) {
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        if (domainNameEntry.doesDomainExist(domainName)) {
            return domainNameEntry.getValueForDomain(domainName);
        } else {
            return DEFAULT_AUTH_MODE;
        }
    }

    public void validate() throws ManageDomainsResult {
        String currentDomainNameEntry = configurationProvider.getConfigValue(ConfigValues.DomainName);
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);

        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomainNameEntry, DOMAIN_SEPERATOR, null);
        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserPasswordEntry =
                new PasswordDomainsConfigurationEntry(currentAdUserPasswordEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserIdEntry =
            new DomainsConfigurationEntry(currentAdUserIdEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        testConfiguration(null,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                false,
                true);
    }

    public void getConfiguration() {
        String currentDomainNameEntry = configurationProvider.getConfigValue(ConfigValues.DomainName);
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);

        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomainNameEntry, DOMAIN_SEPERATOR, null);
        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        Set<Entry<String, String>> domainValues = domainNameEntry.getValues();
        for (Entry<String, String> currDomain : domainValues) {
            String domain = currDomain.getKey();
            String authMode = getDomainAuthMode(domain);
            String userName = adUserNameEntry.getValueForDomain(domain);

            System.out.println("Domain: " + domain);
            System.out.println("\tUser name: " + userName);
            if (authMode.equalsIgnoreCase(LdapAuthModeEnum.SIMPLE.name())) {
                System.out.println("\tThis domain is a local domain.");
            } else {
                System.out.println("\tThis domain is a remote domain.");
            }
        }
    }

    public void addDomain(String domainName,
            String userName,
            String password,
            String mode) throws ManageDomainsResult {
        String authMode = DEFAULT_AUTH_MODE;
        if (mode.equalsIgnoreCase(LdapModeEnum.LOCAL.name())) {
            authMode = LdapAuthModeEnum.SIMPLE.name();
        } else if (mode.equalsIgnoreCase(LdapModeEnum.REMOTE.name())) {
            authMode = LdapAuthModeEnum.GSSAPI.name();
        }

        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_ALREADY_EXISTS_IN_CONFIGURATION, domainName);
        }

        domainNameEntry.setValueForDomain(domainName, null);
        String domainNameEntryString = domainNameEntry.getDomainsConfigurationEntry();

        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);

        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserPasswordEntry =
                new PasswordDomainsConfigurationEntry(currentAdUserPasswordEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry ldapServersEntry =
                new DomainsConfigurationEntry(currentLdapServersEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserIdEntry =
                new DomainsConfigurationEntry(currentAdUserIdEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        adUserNameEntry.setValueForDomain(domainName, userName);
        adUserPasswordEntry.setValueForDomain(domainName, password);
        authModeEntry.setValueForDomain(domainName, authMode);

        if (authMode.equalsIgnoreCase(LdapAuthModeEnum.SIMPLE.name())) {
            ldapServersEntry.setValueForDomain(domainName, utilityConfiguration.getLocalHostEntry());
        }

        testConfiguration(domainName,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                true,
                false);

        if (addPermissions) {
            updatePermissionsTable(adUserNameEntry, adUserIdEntry);
        } else {
            System.out.println(WARNING_NOT_ADDING_PERMISSIONS);
        }

        // Update the configuration
        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry);

        System.out.println(String.format(SUCCESSFULLY_COMPLETED_ACTION_ON_DOMAIN, "added", domainName));
    }

    private ManageDomainsResult updatePermissionsTable(DomainsConfigurationEntry adUserNameEntry,
            DomainsConfigurationEntry adUseridEntry) {
        try {
            Set<Entry<String, String>> userNameValues = adUserNameEntry.getValues();

            for (Entry<String, String> currUserEntry : userNameValues) {
                String currDomain = currUserEntry.getKey();
                String currUser = currUserEntry.getValue();
                String guid = adUseridEntry.getValueForDomain(currDomain);

                daoImpl.updatePermissionsTable(guid, currUser, currDomain);
            }
            return OK_RESULT;
        } catch (SQLException e) {
            return new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_APPLYING_CHANGES_IN_DATABASE,
                    e.getMessage());
        }
    }

    public void editDomain(String domainName, String userName, String password, String mode) throws ManageDomainsResult {
        String authMode;
        if (mode == null) {
            authMode = getDomainAuthMode(domainName);
        } else {
            authMode = DEFAULT_AUTH_MODE;
            if (mode.equalsIgnoreCase(LdapModeEnum.LOCAL.name())) {
                authMode = LdapAuthModeEnum.SIMPLE.name();
            } else if (mode.equalsIgnoreCase(LdapModeEnum.REMOTE.name())) {
                authMode = LdapAuthModeEnum.GSSAPI.name();
            }
        }

        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (!domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_DOESNT_EXIST_IN_CONFIGURATION, domainName);
        }

        domainNameEntry.setValueForDomain(domainName, null);
        String domainNameEntryString = domainNameEntry.getDomainsConfigurationEntry();

        // Assuming we got here, we need to change the configuration of the others as well
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);

        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserPasswordEntry =
                new PasswordDomainsConfigurationEntry(currentAdUserPasswordEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry ldapServersEntry =
                new DomainsConfigurationEntry(currentLdapServersEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserIdEntry =
                new DomainsConfigurationEntry(currentAdUserIdEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        if (userName != null) {
            adUserNameEntry.setValueForDomain(domainName, userName);
        }
        if (password != null) {
            adUserPasswordEntry.setValueForDomain(domainName, password);
        }

        if (mode != null) {
            authModeEntry.setValueForDomain(domainName, authMode);
        }

        if (authMode.equalsIgnoreCase(LdapAuthModeEnum.SIMPLE.name())) {
            ldapServersEntry.setValueForDomain(domainName, utilityConfiguration.getLocalHostEntry());
        }

        testConfiguration(domainName,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                true,
                false);

        if (addPermissions) {
            updatePermissionsTable(adUserNameEntry, adUserIdEntry);
        } else {
            System.out.println(WARNING_NOT_ADDING_PERMISSIONS);
        }

        setConfigurationEntries(domainNameEntry,
                        adUserNameEntry,
                        adUserPasswordEntry,
                        authModeEntry,
                        ldapServersEntry,
                        adUserIdEntry);

        System.out.println(String.format(SUCCESSFULLY_COMPLETED_ACTION_ON_DOMAIN, "edited", domainName));
    }

    private void createKerberosConfiguration(DomainsConfigurationEntry gssapiDomains) throws ManageDomainsResult {
        if (!gssapiDomains.isEntryEmpty()) {
            String gssapiDomainsString = gssapiDomains.getDomainsConfigurationEntry();

            KrbConfCreator krbConfCreator;
            try {
                log.info("Creating kerberos configuration for domain(s): " + gssapiDomainsString);
                krbConfCreator = new KrbConfCreator(gssapiDomainsString);
                StringBuffer buffer = null;
                buffer = krbConfCreator.parse("y");
                krbConfCreator.toFile(utilityConfiguration.getkrb5confFilePath() + TESTING_KRB5_CONF_SUFFIX, buffer);
                log.info("Successfully created kerberos configuration for domain(s): " + gssapiDomainsString);

            } catch (Exception ex) {
                ManageDomainsResult result =
                        new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_CREATING_KERBEROS_CONFIGURATION,
                                ex.getMessage());
                throw result;
            }
        }
    }

    private void checkKerberosConfiguration(String domainName,
            DomainsConfigurationEntry users,
            DomainsConfigurationEntry passwords,
            DomainsConfigurationEntry gssapiDomains,
            DomainsConfigurationEntry userIds,
            String kerberosConfigFile,
            boolean isValidate) throws ManageDomainsResult {

        Set<Entry<String, String>> gssapiDomainValues = gssapiDomains.getValues();

        for (Entry<String, String> currDomain : gssapiDomainValues) {
            String domain = currDomain.getKey();

            String currUserName = users.getValueForDomain(domain);
            users.setValueForDomain(domain, constructUPN(currUserName, domain));
            try {
                log.info("Testing kerberos configuration for domain: " + domain);
                KerberosConfigCheck kerberosConfigCheck = new KerberosConfigCheck();
                StringBuffer userGuid = new StringBuffer();
                kerberosConfigCheck.checkInstallation(domain,
                        users.getValueForDomain(domain),
                        passwords.getValueForDomain(domain),
                        utilityConfiguration.getJaasFilePath(),
                        kerberosConfigFile,
                        userGuid);
                userIds.setValueForDomain(domain, userGuid.toString());
                if (isValidate) {
                    System.out.println("Domain " + domain + " is valid.");
                }
                log.info("Successfully tested kerberos configuration for domain: " + domain);
            } catch (Exception e) {
                ManageDomainsResult result = new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN, new String[] {
                        domain,
                        e.getMessage() });
                if ((isValidate && reportAllErrors) || ((domainName != null) && !domain.equals(domainName))) {
                    System.out.println("WARNING, domain: " + domain + " may not be functional: "
                            + result.getDetailedMessage());
                } else {
                    throw result;
                }
            }
        }
    }

    private void applyKerberosConfiguration() throws ManageDomainsResult {
        try {
            // We backup the kerberos configuration file in case it already exists
            if (FileUtil.fileExists(utilityConfiguration.getkrb5confFilePath())) {
                SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddhhmmsszzz");
                String destFileName = utilityConfiguration.getkrb5confFilePath() + ".backup_" + ft.format(new Date());
                log.info("Performing backup of kerberos configuration file to " + destFileName);
                FileUtil.copyFile(utilityConfiguration.getkrb5confFilePath(), destFileName);
            }

            log.info("Applying kerberos configuration");
            FileUtil.copyFile(utilityConfiguration.getkrb5confFilePath() + TESTING_KRB5_CONF_SUFFIX,
                    utilityConfiguration.getkrb5confFilePath());
            FileUtil.deleteFile(utilityConfiguration.getkrb5confFilePath() + TESTING_KRB5_CONF_SUFFIX);
        } catch (IOException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_APPLYING_KERBEROS_CONFIGURATION,
                    e.getMessage());
        }
    }

    private ManageDomainsResult checkSimple(String domain,
            String userName,
            String password,
            String address,
            StringBuffer userGuid) {
        log.info("Testing domain " + domain);
        SimpleAuthenticationCheck simpleAuthenticationCheck = new SimpleAuthenticationCheck();
        ReturnStatus returnStatus =
                simpleAuthenticationCheck.printUserGuid(domain, userName, password, address, userGuid);
        if (!returnStatus.equals(ReturnStatus.OK)) {
            return new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN, new String[] { domain,
                    returnStatus.getDetailedMessage() });
        }
        log.info("Successfully tested domain " + domain);
        return OK_RESULT;
    }

    private void checkSimpleDomains(String domainName,
            DomainsConfigurationEntry users,
            DomainsConfigurationEntry passwords,
            DomainsConfigurationEntry simpleDomains,
            DomainsConfigurationEntry userIds,
            String address,
            boolean isValidate) throws ManageDomainsResult {

        Set<Entry<String, String>> simpleDomainValues = simpleDomains.getValues();
        StringBuffer userGuid = new StringBuffer();
        for (Entry<String, String> currDomain : simpleDomainValues) {
            String domain = currDomain.getKey();
            ManageDomainsResult result = checkSimple(domain,
                    users.getValueForDomain(domain),
                    passwords.getValueForDomain(domain),
                    address,
                    userGuid);
            if (!result.isSuccessful()) {
                if (isValidate || ((domainName != null) && !domain.equals(domainName))) {
                    if (reportAllErrors) {
                        System.out.println("WARNING, domain: " + domain + " may not be functional: "
                                + result.getDetailedMessage());
                    } else {
                        throw result;
                    }
                } else {
                    throw result;
                }
            } else {
                userIds.setValueForDomain(domain, userGuid.toString());
                if (isValidate) {
                    System.out.println("Domain " + domain + " is valid.");
                }
            }
        }
    }

    // This method will test the configuration on all domains.
    // It will also reconfigure kerberos in case the reconfigure flag is on
    private void testConfiguration(String domainName,
            DomainsConfigurationEntry domains,
            DomainsConfigurationEntry users,
            DomainsConfigurationEntry passwords,
            DomainsConfigurationEntry authModes,
            DomainsConfigurationEntry userIds,
            boolean reconfigure,
            boolean isValidate) throws ManageDomainsResult {

        Set<Entry<String, String>> domainValues = domains.getValues();

        DomainsConfigurationEntry gssapiDomains = new DomainsConfigurationEntry("", DOMAIN_SEPERATOR, null);
        DomainsConfigurationEntry simpleDomains = new DomainsConfigurationEntry("", DOMAIN_SEPERATOR, null);

        for (Entry<String, String> currDomain : domainValues) {
            String domain = currDomain.getKey();
            String authMode = authModes.getValueForDomain(domain);
            if (authMode == null) {
                authMode = getDomainAuthMode(domain);
            }
            if (authMode.equalsIgnoreCase(LdapAuthModeEnum.GSSAPI.name())) {
                gssapiDomains.setValueForDomain(domain, null);
            } else {
                simpleDomains.setValueForDomain(domain, null);
            }
        }

                checkSimpleDomains(domainName,
                        users,
                        passwords,
                        simpleDomains,
                        userIds,
                        utilityConfiguration.getLocalHostEntry(),
                        isValidate);

        boolean domainIsGssapi = gssapiDomains.doesDomainExist(domainName);

        if (!gssapiDomains.isEntryEmpty()) {
            String kerberosConfigFile = utilityConfiguration.getkrb5confFilePath();

            if (domainIsGssapi && reconfigure) {
                createKerberosConfiguration(gssapiDomains);
                kerberosConfigFile += TESTING_KRB5_CONF_SUFFIX;
            }

            checkKerberosConfiguration(domainName,
                    users,
                    passwords,
                    gssapiDomains,
                    userIds,
                    kerberosConfigFile,
                    isValidate);
            if (domainIsGssapi && reconfigure) {
                applyKerberosConfiguration();
            }
        }
    }

    private void setConfigurationEntries(DomainsConfigurationEntry domainNameEntry,
            DomainsConfigurationEntry adUserNameEntry,
            DomainsConfigurationEntry adPasswordEntry,
            DomainsConfigurationEntry authModeEntry,
            DomainsConfigurationEntry ldapServersEntry,
            DomainsConfigurationEntry adUserIdEntry) throws ManageDomainsResult {
        // Update the configuration
        configurationProvider.setConfigValue(ConfigValues.AdUserName,
                adUserNameEntry.getDomainsConfigurationEntry(),
                adUserNameEntry.getDomainsLoggingEntry());

        configurationProvider.setConfigValue(ConfigValues.AdUserPassword,
                adPasswordEntry.getDomainsConfigurationEntry(),
                adPasswordEntry.getDomainsLoggingEntry());

        configurationProvider.setConfigValue(ConfigValues.LdapServers,
                ldapServersEntry.getDomainsConfigurationEntry(),
                ldapServersEntry.getDomainsLoggingEntry());

        configurationProvider.setConfigValue(ConfigValues.AdUserId,
                adUserIdEntry.getDomainsConfigurationEntry(),
                adUserIdEntry.getDomainsLoggingEntry());

        configurationProvider.setConfigValue(ConfigValues.LDAPSecurityAuthentication,
                authModeEntry.getDomainsConfigurationEntry(),
                authModeEntry.getDomainsLoggingEntry());

        configurationProvider.setConfigValue(ConfigValues.DomainName,
                domainNameEntry.getDomainsConfigurationEntry(),
                domainNameEntry.getDomainsLoggingEntry());
    }

    public void deleteDomain(String domainName) throws ManageDomainsResult {

        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (!domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_DOESNT_EXIST_IN_CONFIGURATION, domainName);
        }

        domainNameEntry.removeValueForDomain(domainName);

        if (domainNameEntry.isEntryEmpty()) {
            System.out.println(String.format(WARNING_ABOUT_TO_DELETE_LAST_DOMAIN, domainName));
        }

        // Assuming we got here, we need to change the configuration of the others as well
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserId = configurationProvider.getConfigValue(ConfigValues.AdUserId);

        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserPasswordEntry =
                new PasswordDomainsConfigurationEntry(currentAdUserPasswordEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry ldapServersEntry =
                new DomainsConfigurationEntry(currentLdapServersEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserIdEntry =
                new DomainsConfigurationEntry(currentAdUserId, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        adUserNameEntry.removeValueForDomain(domainName);
        adUserIdEntry.removeValueForDomain(domainName);
        adUserPasswordEntry.removeValueForDomain(domainName);
        authModeEntry.removeValueForDomain(domainName);
        ldapServersEntry.removeValueForDomain(domainName);

        // Update the configuration
        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry);

        System.out.println(String.format(DELETE_DOMAIN_SUCCESS, domainName));
    }

    private void validate(CLIParser parser) throws ManageDomainsResult {

        if (parser.hasArg(Arguments.propertiesFile.name())) {
            if (parser.hasArg(Arguments.action.name())) {
                String action = parser.getArg(Arguments.action.name());
                ActionType actionType;
                try {
                    actionType = ActionType.valueOf(action);
                } catch (IllegalArgumentException ex) {
                    throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, action);
                }
                if (actionType.equals(ActionType.add)) {
                    requireArgs(parser, Arguments.domain, Arguments.user);
                    requireAtLeastOneArg(parser, Arguments.passwordFile, Arguments.interactive);
                } else if (actionType.equals(ActionType.edit)) {
                    requireArgs(parser, Arguments.domain);
                } else if (actionType.equals(ActionType.delete)) {
                    requireArgs(parser, Arguments.domain);
                } else if (actionType.equals(ActionType.validate)) {
                    checkInvalidArgs(parser,
                            Arguments.domain,
                            Arguments.user,
                            Arguments.passwordFile,
                            Arguments.interactive);

                } else if (actionType.equals(ActionType.list)) {
                    checkInvalidArgs(parser,
                            Arguments.domain,
                            Arguments.user,
                            Arguments.passwordFile,
                            Arguments.interactive);
                }
            } else {
                throw new ManageDomainsResult(ManageDomainsResultEnum.ACTION_IS_NOT_SPECIFIED);
            }
        } else {
            throw new ManageDomainsResult(ManageDomainsResultEnum.PROPERTIES_FILE_IS_NOT_SPECIFIED);
        }
        if (parser.getArgs().size() > Arguments.values().length) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.TOO_MANY_ARGUMENTS);
        }
    }

    private void requireArgs(CLIParser parser, Arguments... args) throws ManageDomainsResult {
        for (Arguments arg: args) {
            if (!parser.hasArg(arg.name())) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.ARGUMENT_IS_REQUIRED, arg.name());
            }
        }
    }

    private void requireAtLeastOneArg(CLIParser parser, Arguments... args) throws ManageDomainsResult {
        for (Arguments arg : args) {
            if (parser.hasArg(arg.name())) {
                return;
            }
        }
        throw new ManageDomainsResult(ManageDomainsResultEnum.ARGUMENT_IS_REQUIRED, Arrays.deepToString(args));
    }

    private void checkInvalidArgs(CLIParser parser, Arguments... args) throws ManageDomainsResult {
        for (Arguments arg : args) {
            if (parser.hasArg(arg.name())) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ARGUMENT_FOR_COMMAND, arg.name());
            }
        }
    }
}
