/**
 *
 */
package org.ovirt.engine.core.domains;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.ldap.LdapSRVLocator;
import org.ovirt.engine.core.utils.CLIParser;
import org.ovirt.engine.core.utils.dns.DnsSRVLocator;
import org.ovirt.engine.core.utils.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.core.utils.ipa.ReturnStatus;
import org.ovirt.engine.core.utils.ipa.SimpleAuthenticationCheck;
import org.ovirt.engine.core.utils.kerberos.KDCLocator;
import org.ovirt.engine.core.utils.kerberos.KerberosConfigCheck;
import org.ovirt.engine.core.utils.kerberos.KrbConfCreator;

public class ManageDomains {

    public static final String CONF_FILE_PATH = "/etc/ovirt-engine/engine-manage-domains/engine-manage-domains.conf";
    private final String WARNING_ABOUT_TO_DELETE_LAST_DOMAIN =
            "WARNING: Domain %1$s is the last domain in the configuration. After deleting it you will have to either add another domain, or to use the internal admin user in order to login.";
    private final String INFO_ABOUT_NOT_ADDING_PERMISSIONS =
            "The domain %1$s has been added to the engine as an authentication source but no users from that domain have been granted permissions within the oVirt Manager.\n"+
            "Users from this domain can be granted permissions from the Web administration interface or by editing the domain using -action=edit and specifying -addPermissions.";

    private final String SERVICE_RESTART_MESSAGE =
            "oVirt Engine restart is required in order for the changes to take place (service ovirt-engine restart).";
    private final String DELETE_DOMAIN_SUCCESS =
            "Successfully deleted domain %1$s. Please remove all users and groups of this domain using the Administration portal or the API. "
                    + SERVICE_RESTART_MESSAGE;
    private final String SUCCESS_MESSAGE_FOR_ACTION_WITH_ADD_PERMISSIONS =
            "Successfully %1$s domain %2$s. ";
    private final String ILLEGAL_PASSWORD_CHARACTERS = ",";

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
    private boolean useDnsLookup;

    private final static Logger log = Logger.getLogger(ManageDomains.class);
    private static final String DEFAULT_LDAP_SERVER_PORT = "389";

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
        addPermissions,
        provider,
        forceDelete,
        ldapServers,
    }

    public enum ActionType {
        add,
        edit,
        delete,
        validate,
        list;
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
            daoImpl = new ManageDomainsDAOImpl();
        } catch (SQLException e) {
            throw new ManageDomainsResult("Please verify the following:\n1. Your database credentials are valid.\n2. The database machine is accessible.\n3. The database service is running",
                    ManageDomainsResultEnum.DB_EXCEPTION,
                    e.getMessage());
        }
    }

    private static void exitOnError(ManageDomainsResult result) {
        if (!result.isSuccessful()) {
            log.error(result.getDetailedMessage());
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
            String domainName = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.DomainName);
            String ldapSecurityAuthentication =
                    getConfigValue(engineConfigExecutable,
                            engineConfigProperties,
                            ConfigValues.LDAPSecurityAuthentication);
            String adUserPassword =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.AdUserPassword);
            String adUserId = getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.AdUserId);
            String ldapServers =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.LdapServers);
            String ldapProviderTypes =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.LDAPProviderTypes);
            String ldapPort =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.LDAPServerPort);
            if (ldapPort == null)
            {
                ldapPort = DEFAULT_LDAP_SERVER_PORT;
            }

            configurationProvider =
                    new ConfigurationProvider(adUserName,
                            adUserPassword,
                            domainName,
                            ldapSecurityAuthentication,
                            ldapServers,
                            adUserId,
                            ldapProviderTypes,
                            utilityConfiguration.getEngineConfigExecutablePath(),
                            engineConfigProperties, ldapPort);

        } catch (Throwable e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CURRENT_CONFIGURATION, e.getMessage());
        }
    }

    private void validateKdcServers(String ldapSecurityAuthentication, String domainName) throws ManageDomainsResult {
        KDCLocator locator = new KDCLocator();
        DnsSRVResult result = null;
        boolean foundServers = true;
        try
        {
            result = locator.getKdc(DnsSRVLocator.TCP, domainName);
            if (!foundSrvRecords(result)) {
                result = locator.getKdc(DnsSRVLocator.UDP,domainName);
                if (!foundSrvRecords(result)) {
                    foundServers =false;
                }
            }
        } catch (Exception ex) {
            foundServers = false;
        }
        if (!foundServers) {
            throw new ManageDomainsResult("Could not locate KDC servers to be used to validate the input of the utility",
                    ManageDomainsResultEnum.NO_KDC_SERVERS_FOR_DOMAIN, domainName);
        }

    }

    private boolean foundSrvRecords(DnsSRVResult result) {
        return result != null && result.getNumOfValidAddresses() > 0;
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
            addDomain(parser);
        } else if (actionType.equals(ActionType.edit)) {
            editDomain(parser);
        } else if (actionType.equals(ActionType.delete)) {
            deleteDomain(parser.getArg(Arguments.domain.name()).toLowerCase(), parser.hasArg(Arguments.forceDelete.name()));
        } else if (actionType.equals(ActionType.validate)) {
            validate();
        } else if (actionType.equals(ActionType.list)) {
            getConfiguration();
        } else {
            throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, action);
        }
    }

    protected LdapProviderType getLdapProviderType(CLIParser parser) throws ManageDomainsResult {

        try {
            return LdapProviderType.valueOfIgnoreCase(parser.getArg(Arguments.provider.name()));
        } catch (IllegalArgumentException i) {
            // continue and print message
        } catch (NullPointerException e) {
            // continue and print message
        }

        StringBuffer sb = new StringBuffer();
        sb.append(parser.getArg(Arguments.provider.name()) + ". Supported provider types are:\n");
        for (LdapProviderType t : LdapProviderType.values()) {
            sb.append(" " + t.name() + "\n");
        }
        throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ARGUMENT_FOR_COMMAND, sb.toString());
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
            if (pass == null) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.EMPTY_PASSWORD_FILE);
            }
        } else if (parser.hasArg(Arguments.interactive.name())) {
            pass = readPasswordInteractively();
        }

        validatePassword(pass);

        return pass;
    }

    private void validatePassword(String pass) throws ManageDomainsResult {
        if (StringUtils.containsAny(pass, ILLEGAL_PASSWORD_CHARACTERS)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.ILLEGAL_PASSWORD);
        }
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
        closeQuietly(in, bufferedReader);
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
        String currentLdapProviderTypesEntry = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String ldapServerPort = configurationProvider.getConfigValue(ConfigValues.LDAPServerPort);


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
        DomainsConfigurationEntry ldapProviderTypeEntry =
                new DomainsConfigurationEntry(currentLdapProviderTypesEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        DomainsConfigurationEntry ldapServersEntry =
                new DomainsConfigurationEntry(currentLdapServersEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);


        testConfiguration(null,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                ldapProviderTypeEntry,
                ldapServersEntry,
                ldapServerPort,
                false,
                true,
                null);
    }

    public void getConfiguration() {
        String currentDomainNameEntry = configurationProvider.getConfigValue(ConfigValues.DomainName);
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);

        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomainNameEntry, DOMAIN_SEPERATOR, null);
        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        Set<String> domainNames = new TreeSet<String>(domainNameEntry.getDomainNames());
        for (String domain : domainNames) {
            String authMode = getDomainAuthMode(domain);
            String userName = adUserNameEntry.getValueForDomain(domain);

            System.out.println("Domain: " + domain);
            System.out.println("\tUser name: " + userName);
        }
    }

    protected List<String> getLdapServers(CLIParser parser, String domainName) throws ManageDomainsResult {
        String argValue = parser.getArg(Arguments.ldapServers.toString().toLowerCase());
        if (StringUtils.isEmpty(argValue)) {
            LdapSRVLocator locator = new LdapSRVLocator();
            DnsSRVResult ldapDnsResult = null;
            boolean foundServers = true;
            try {
                ldapDnsResult = locator.getLdapServers(domainName);
                if (!foundSrvRecords(ldapDnsResult)) {
                    foundServers = false;
                }
            } catch (Exception ex) {
                foundServers = false;
            }
            if (!foundServers) {
                throw new ManageDomainsResult("Could not locate LDAP servers to be used to validate the input of the utility",
                        ManageDomainsResultEnum.NO_LDAP_SERVERS_FOR_DOMAIN, domainName);

            }
            ArrayList<String> result = new ArrayList<String>();
            for (int counter = 0; counter < ldapDnsResult.getNumOfValidAddresses(); counter++) {
                result.add(ldapDnsResult.getAddresses()[counter]);
            }
            return result;
        }
        return new ArrayList<String>(Arrays.asList(argValue.split(",")));
    }

    public void addDomain(CLIParser parser) throws ManageDomainsResult {
        String authMode = LdapAuthModeEnum.GSSAPI.name();
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        String domainName = parser.getArg(Arguments.domain.toString()).toLowerCase();
        if (domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_ALREADY_EXISTS_IN_CONFIGURATION, domainName);
        }
        List<String> ldapServers = getLdapServers(parser, domainName);
        validateKdcServers(authMode,domainName);
        domainNameEntry.setValueForDomain(domainName, null);

        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);
        String currentLDAPProviderTypes = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);
        String ldapServerPort = configurationProvider.getConfigValue(ConfigValues.LDAPServerPort);

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
        DomainsConfigurationEntry ldapProviderTypesEntry =
                new DomainsConfigurationEntry(currentLDAPProviderTypes, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        LdapProviderType ldapProviderType = getLdapProviderType(parser);
        adUserNameEntry.setValueForDomain(domainName, parser.getArg(Arguments.user.toString()));
        adUserPasswordEntry.setValueForDomain(domainName, getPasswordInput(parser));
        authModeEntry.setValueForDomain(domainName, authMode);
        ldapProviderTypesEntry.setValueForDomain(domainName, ldapProviderType.name());

        String ldapServersStr = parser.getArg(Arguments.ldapServers.name());
        if (!StringUtils.isEmpty(ldapServersStr)) {
            //Replacing "," with ";" - from user perspective of the utility, passing comma delimited string makes more sense and more natural
            //But "," is used as domain separate character when storing to DB.
            ldapServersStr = ldapServersStr.replace(',',';');
            ldapServersEntry.setValueForDomain(domainName, ldapServersStr);
        }


        testConfiguration(domainName,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                ldapProviderTypesEntry,
                ldapServersEntry,
                ldapServerPort,
                true,
                false,
                ldapServers);

        handleAddPermissions(domainName, adUserNameEntry, adUserIdEntry);

        // Update the configuration
        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry,
                ldapProviderTypesEntry);
        printSuccessMessage(domainName,"added");
    }

    private void printSuccessMessage(String domainName, String action) {
        if (addPermissions) {
            System.out.print(String.format(SUCCESS_MESSAGE_FOR_ACTION_WITH_ADD_PERMISSIONS, "added", domainName));
        }
        System.out.println(SERVICE_RESTART_MESSAGE);
    }

    private void handleAddPermissions(String domainName,DomainsConfigurationEntry adUserNameEntry, DomainsConfigurationEntry adUserIdEntry) {
        if (addPermissions) {
            updatePermissionsTable(adUserNameEntry, adUserIdEntry);
        } else
        if (!userHasPermissions(adUserNameEntry, adUserIdEntry)) {
            System.out.println(String.format(INFO_ABOUT_NOT_ADDING_PERMISSIONS, domainName));
        }
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

    private boolean userHasPermissions(DomainsConfigurationEntry adUserNameEntry,
        DomainsConfigurationEntry adUseridEntry) {
        try {
            Set<Entry<String, String>> userNameValues = adUserNameEntry.getValues();
            for (Entry<String, String> currUserEntry : userNameValues) {
                String currDomain = currUserEntry.getKey();
                String currUser = currUserEntry.getValue();
                if (daoImpl.getUserHasPermissions(currUser, currDomain)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error(e);
        }
        return false;
    }

    public void editDomain(CLIParser parser) throws ManageDomainsResult {
        String authMode;
        String domainName = parser.getArg(Arguments.domain.toString()).toLowerCase();
        authMode = getDomainAuthMode(domainName);
        List<String> ldapServers = getLdapServers(parser, domainName);
        validateKdcServers(authMode,domainName);
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        String userName  = parser.getArg(Arguments.user.toString());
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (!domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_DOESNT_EXIST_IN_CONFIGURATION, domainName);
        }

        domainNameEntry.setValueForDomain(domainName, null);

        // Assuming we got here, we need to change the configuration of the others as well
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);
        String currentLdapProviderTypeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);
        String ldapServerPort = configurationProvider.getConfigValue(ConfigValues.LDAPServerPort);


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
        DomainsConfigurationEntry ldapProviderTypeEntry =
                new DomainsConfigurationEntry(currentLdapProviderTypeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        if (userName != null) {
            adUserNameEntry.setValueForDomain(domainName, userName);
        }
        String password = getPasswordInput(parser);
        if (password != null) {
            adUserPasswordEntry.setValueForDomain(domainName, password);
        }

        if (authMode.equalsIgnoreCase(LdapAuthModeEnum.SIMPLE.name())) {
            ldapServersEntry.setValueForDomain(domainName, utilityConfiguration.getLocalHostEntry());
        }
        LdapProviderType ldapProviderType = getLdapProviderType(parser);
        if (ldapProviderType != null) {
            ldapProviderTypeEntry.setValueForDomain(domainName, ldapProviderType.name());
        }

        testConfiguration(domainName,
                domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                adUserIdEntry,
                ldapProviderTypeEntry,
                ldapServersEntry,
                ldapServerPort,
                true,
                false,
                ldapServers);

        handleAddPermissions(domainName,adUserNameEntry, adUserIdEntry);

        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry,
                ldapProviderTypeEntry);

        printSuccessMessage(domainName,"edited");
    }

    private void createKerberosConfiguration(DomainsConfigurationEntry gssapiDomains, Map<String, List<String>> ldapServersPerGSSAPIDomains) throws ManageDomainsResult {
        if (!gssapiDomains.isEntryEmpty()) {
            String gssapiDomainsString = gssapiDomains.getDomainsConfigurationEntry();

            KrbConfCreator krbConfCreator;
            try {
                log.info("Creating kerberos configuration for domain(s): " + gssapiDomainsString);
                useDnsLookup = utilityConfiguration.getUseDnsLookup();
                krbConfCreator = new KrbConfCreator(gssapiDomainsString, useDnsLookup, ldapServersPerGSSAPIDomains);
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
            DomainsConfigurationEntry ldapProviderTypes,
            Map<String,List<String>> ldapServersPerDomainMap,
            String kerberosConfigFile,
            String ldapServerPort,
            boolean isValidate,
            List<String> ldapServers) throws ManageDomainsResult {

        Set<Entry<String, String>> gssapiDomainValues = gssapiDomains.getValues();

        for (Entry<String, String> currDomain : gssapiDomainValues) {
            String domain = currDomain.getKey();

            String currUserName = users.getValueForDomain(domain);
            users.setValueForDomain(domain, constructUPN(currUserName, domain));
            try {
                log.info("Testing kerberos configuration for domain: " + domain);
                List<String> ldapServersPerDomain = ldapServersPerDomainMap.get(domain);
                KerberosConfigCheck kerberosConfigCheck = new KerberosConfigCheck(ldapServersPerDomain, ldapServerPort);
                StringBuffer userGuid = new StringBuffer();
                kerberosConfigCheck.checkInstallation(domain,
                        users.getValueForDomain(domain),
                        passwords.getValueForDomain(domain),
                        utilityConfiguration.getJaasFilePath(),
                        kerberosConfigFile,
                        userGuid,
                        LdapProviderType.valueOf(ldapProviderTypes.getValueForDomain(domain)));
                userIds.setValueForDomain(domain, userGuid.toString());
                if (isValidate) {
                    System.out.println("Domain " + domain + " is valid.");
                    System.out.println("The configured user for domain " + domain + " is " + currUserName);
                }
                log.info("Successfully tested kerberos configuration for domain: " + domain);
            } catch (Exception e) {
                ManageDomainsResult result =
                        new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN,
                                new String[] { domain, e.getMessage() });
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
            if (new File(utilityConfiguration.getkrb5confFilePath()).exists()) {
                SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddhhmmsszzz");
                String destFileName = utilityConfiguration.getkrb5confFilePath() + ".backup_" + ft.format(new Date());
                log.info("Performing backup of kerberos configuration file to " + destFileName);
                copyFile(utilityConfiguration.getkrb5confFilePath(), destFileName);
            }

            log.info("Applying kerberos configuration");
            copyFile(utilityConfiguration.getkrb5confFilePath() + TESTING_KRB5_CONF_SUFFIX,
                    utilityConfiguration.getkrb5confFilePath());
            deleteFile(utilityConfiguration.getkrb5confFilePath() + TESTING_KRB5_CONF_SUFFIX);
        } catch (IOException e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_APPLYING_KERBEROS_CONFIGURATION,
                    e.getMessage());
        }
    }

    private ManageDomainsResult checkSimple(String domain,
            String userName,
            String password,
            StringBuffer userGuid, LdapProviderType ldapProviderType, List<String> ldapServers) {
        log.info("Testing domain " + domain);
        SimpleAuthenticationCheck simpleAuthenticationCheck = new SimpleAuthenticationCheck();
        Pair<ReturnStatus,String> simpleCheckResult =
                simpleAuthenticationCheck.printUserGuid(domain, userName, password, userGuid, ldapProviderType, ldapServers);
        if (!simpleCheckResult.getFirst().equals(ReturnStatus.OK)) {
            System.err.println(simpleCheckResult.getSecond());
            return new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN,
                    new String[] { domain, simpleCheckResult.getFirst().getDetailedMessage() });
        }
        log.info("Successfully tested domain " + domain);
        return OK_RESULT;
    }

    private void checkSimpleDomains(String domainName,
            DomainsConfigurationEntry users,
            DomainsConfigurationEntry passwords,
            DomainsConfigurationEntry simpleDomains,
            DomainsConfigurationEntry userIds,
            DomainsConfigurationEntry ldapProviderType,
            Map<String,List<String>>  ldapServersMapPerDomainMap,
            boolean isValidate) throws ManageDomainsResult {

        Set<Entry<String, String>> simpleDomainValues = simpleDomains.getValues();
        StringBuffer userGuid = new StringBuffer();
        for (Entry<String, String> currDomain : simpleDomainValues) {
            String domain = currDomain.getKey();
            List<String> domainLdapServers = ldapServersMapPerDomainMap.get(domain);
            ManageDomainsResult result = checkSimple(domain,
                    users.getValueForDomain(domain),
                    passwords.getValueForDomain(domain),
                    userGuid, LdapProviderType.valueOf(ldapProviderType.getValueForDomain(domain)), domainLdapServers);
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
            DomainsConfigurationEntry ldapProviderType,
            DomainsConfigurationEntry ldapServersEntry,
            String ldapServerPort,
            boolean reconfigure,
            boolean isValidate,
            List<String> ldapServers) throws ManageDomainsResult {

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

        Map<String,List<String>> ldapServersPerSimpleDomains = new HashMap<String, List<String>>();
        Map<String,List<String>> ldapServersPerGSSAPIDomains = new HashMap<String, List<String>>();


        for (Entry<String,String> currLdapServerEntry: ldapServersEntry.getValues()) {
            if (gssapiDomains.contains(currLdapServerEntry.getKey())) {
                ldapServersPerGSSAPIDomains.put(currLdapServerEntry.getKey(),
                        new ArrayList<String>(Arrays.asList(currLdapServerEntry.getValue().split(";"))));
            } else
            {
                ldapServersPerSimpleDomains.put(currLdapServerEntry.getKey(),
                        new ArrayList<String>(Arrays.asList(currLdapServerEntry.getValue().split(";"))));

            }
        }

        checkSimpleDomains(domainName,
                users,
                passwords,
                simpleDomains,
                userIds,
                ldapProviderType,
                ldapServersPerSimpleDomains,
                isValidate);

        boolean domainIsGssapi = gssapiDomains.doesDomainExist(domainName);

        if (!gssapiDomains.isEntryEmpty()) {
            String kerberosConfigFile = utilityConfiguration.getkrb5confFilePath();

            if (domainIsGssapi && reconfigure) {
                createKerberosConfiguration(gssapiDomains, ldapServersPerGSSAPIDomains);
                kerberosConfigFile += TESTING_KRB5_CONF_SUFFIX;
            }

            checkKerberosConfiguration(domainName,
                    users,
                    passwords,
                    gssapiDomains,
                    userIds,
                    ldapProviderType,
                    ldapServersPerGSSAPIDomains,
                    kerberosConfigFile,
                    ldapServerPort,
                    isValidate,
                    ldapServers);
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
            DomainsConfigurationEntry adUserIdEntry,
            DomainsConfigurationEntry ldapProviderTypeEntry) throws ManageDomainsResult {
        // Update the configuration
        configurationProvider.setConfigValue(ConfigValues.AdUserName,
                adUserNameEntry);

        configurationProvider.setConfigValue(ConfigValues.AdUserPassword,
                adPasswordEntry, false);

        configurationProvider.setConfigValue(ConfigValues.LdapServers,
                ldapServersEntry);

        configurationProvider.setConfigValue(ConfigValues.AdUserId,
                adUserIdEntry);

        configurationProvider.setConfigValue(ConfigValues.LDAPSecurityAuthentication,
                authModeEntry);

        configurationProvider.setConfigValue(ConfigValues.DomainName,
                domainNameEntry);

        configurationProvider.setConfigValue(ConfigValues.LDAPProviderTypes,
                ldapProviderTypeEntry);
    }

    public void deleteDomain(String domainName, boolean forceDelete) throws ManageDomainsResult {

        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (!domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_DOESNT_EXIST_IN_CONFIGURATION, domainName);
        }

        //Prompt warning about last domain only if not "force delete", as using
        //the force delete option should remove with no confirmation/warning
        if (domainNameEntry.getDomainNames().size() == 1 && !forceDelete) {
            System.out.println(String.format(WARNING_ABOUT_TO_DELETE_LAST_DOMAIN, domainName));
        }

        if(!forceDelete && !confirmDeleteDomain(domainName)) {
            return;
        }

        domainNameEntry.removeValueForDomain(domainName);

        // Assuming we got here, we need to change the configuration of the others as well
        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserId = configurationProvider.getConfigValue(ConfigValues.AdUserId);
        String ldapProviderType = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);

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
        DomainsConfigurationEntry ldapProviderTypeEntry =
                new DomainsConfigurationEntry(ldapProviderType, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        adUserNameEntry.removeValueForDomain(domainName);
        adUserIdEntry.removeValueForDomain(domainName);
        adUserPasswordEntry.removeValueForDomain(domainName);
        authModeEntry.removeValueForDomain(domainName);
        ldapServersEntry.removeValueForDomain(domainName);
        ldapProviderTypeEntry.removeValueForDomain(domainName);

        // Update the configuration
        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry,
                ldapProviderTypeEntry);

        System.out.println(String.format(DELETE_DOMAIN_SUCCESS, domainName));
    }

    private boolean confirmDeleteDomain(String domainName) {
        String response = null;
        while (StringUtils.isBlank(response)
                || (!StringUtils.equalsIgnoreCase(response, "yes")
                 && !StringUtils.equalsIgnoreCase(response, "no"))) {
            System.out.println("Are you sure you like to delete domain "+domainName +" (yes/no) : ");
            response = System.console().readLine();
        }
        return response.equals("yes");
    }

    private void validate(CLIParser parser) throws ManageDomainsResult {

        if (parser.hasArg(Arguments.propertiesFile.name())) {
            if (parser.hasArg(Arguments.action.name())) {
                String action = parser.getArg(Arguments.action.name());
                ActionType actionType;
                try {
                    actionType = ActionType.valueOf(action);
                } catch (IllegalArgumentException ex) {
                    throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION,
                            action);
                }
                if (actionType.equals(ActionType.add)) {
                    requireArgs(parser, Arguments.domain, Arguments.user, Arguments.provider);
                    requireAtLeastOneArg(parser, Arguments.passwordFile, Arguments.interactive);
                    checkInvalidArgs(parser,
                            Arguments.forceDelete);
                } else if (actionType.equals(ActionType.edit)) {
                    requireArgs(parser, Arguments.domain);
                    checkInvalidArgs(parser,
                            Arguments.forceDelete);
                } else if (actionType.equals(ActionType.delete)) {
                    requireArgs(parser, Arguments.domain);
                    checkInvalidArgs(parser);
                } else if (actionType.equals(ActionType.validate)) {
                    checkInvalidArgs(parser,
                            Arguments.domain,
                            Arguments.user,
                            Arguments.passwordFile,
                            Arguments.interactive,
                            Arguments.forceDelete);
                } else if (actionType.equals(ActionType.list)) {
                    checkInvalidArgs(parser,
                            Arguments.domain,
                            Arguments.user,
                            Arguments.passwordFile,
                            Arguments.interactive,
                            Arguments.forceDelete);
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
        for (Arguments arg : args) {
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

        // check if the user has provided undefined arguments
        Set<String> arguments = new TreeSet<String>(parser.getArgs());
        for (Arguments arg : Arguments.values()) {
            arguments.remove(arg.name().toLowerCase());
        }
        if (arguments.size() > 0){
            throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ARGUMENT_FOR_COMMAND,
                    arguments.toString().replaceAll("\\[", "").replaceAll("\\]",""));
        }
    }

    private static void closeQuietly(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static void copyFile(String srcFilePath, String dstFilePath) throws IOException {
        InputStream in = new FileInputStream(srcFilePath);
        OutputStream out = new FileOutputStream(dstFilePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
