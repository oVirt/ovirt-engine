/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.tools;

import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ACTION_ADD;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ACTION_DELETE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ACTION_EDIT;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ACTION_LIST;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ACTION_VALIDATE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_ACTION;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_ADD_PERMISSIONS;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_CHANGE_PASSWORD_MSG;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_CONFIG_FILE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_DOMAIN;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_FORCE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_LDAP_SERVERS;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_PASSWORD_FILE;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_PROVIDER;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_REPORT;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_RESOLVE_KDC;
import static org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsArguments.ARG_USER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa.ReturnStatus;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa.SimpleAuthenticationCheck;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.KDCLocator;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.KerberosConfigCheck;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.KrbConfCreator;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapSRVLocator;

public class ManageDomains {

    private final String WARNING_ABOUT_TO_DELETE_LAST_DOMAIN =
            "WARNING: Domain %1$s is the last domain in the configuration. After deleting it you will have to either add another domain, or to use the internal admin user in order to login.";
    private final String INFO_ABOUT_NOT_ADDING_PERMISSIONS =
            "The domain %1$s has been added to the engine as an authentication source but no users from that domain"
            + " have been granted permissions within the oVirt Manager.%n"
            + "Users from this domain can be granted permissions by editing the domain using action edit and"
            +" specifying --add-permissions or from the Web administration interface logging in as admin@internal user.";

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
    private boolean useDnsLookup;

    private final static Logger log = Logger.getLogger(ManageDomains.class);

    private final ManageDomainsArguments args;

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

    public ManageDomains(ManageDomainsArguments args) {
        this.args = args;
    }

    public void init() throws ManageDomainsResult {

        try {
            utilityConfiguration = new ManageDomainsConfiguration(args.get(ARG_CONFIG_FILE));
        } catch (Exception e) {
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

    public static void exitOnError(ManageDomainsResult result) {
        if (!result.isSuccessful()) {
            log.error(result.getDetailedMessage());
            System.out.println(result.getDetailedMessage());
            System.exit(result.getExitCode());
        }
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is, Charset.forName("UTF-8").toString()).useDelimiter("\\A").next().replace("\n", "");
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

    public void createConfigurationProvider() throws ManageDomainsResult {
        String engineConfigProperties = createTempPropFile();
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
            String changePasswordUrl =
                    getConfigValue(engineConfigExecutable, engineConfigProperties, ConfigValues.ChangePasswordMsg);

            configurationProvider =
                    new ConfigurationProvider(adUserName,
                            adUserPassword,
                            domainName,
                            ldapSecurityAuthentication,
                            ldapServers,
                            adUserId,
                            ldapProviderTypes,
                            utilityConfiguration.getEngineConfigExecutablePath(),
                            engineConfigProperties, ldapPort, changePasswordUrl);

        } catch (Throwable e) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CURRENT_CONFIGURATION, e.getMessage());
        }
    }

    private void validateKdcServers(String ldapSecurityAuthentication, String domainName) throws ManageDomainsResult {
        KDCLocator locator = new KDCLocator();
        DnsSRVResult result = null;
        boolean foundServers = true;
        try {
            result = locator.getKdc(DnsSRVLocator.TCP, domainName);
            if (!foundSrvRecords(result)) {
                result = locator.getKdc(DnsSRVLocator.UDP, domainName);
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

    public void runCommand() throws ManageDomainsResult {
        String action = args.get(ARG_ACTION);

        if (ACTION_ADD.equals(action)) {
            addDomain();
        } else if (ACTION_EDIT.equals(action)) {
            editDomain();
        } else if (ACTION_DELETE.equals(action)) {
            deleteDomain();
        } else if (ACTION_VALIDATE.equals(action)) {
            validate();
        } else if (ACTION_LIST.equals(action)) {
            getConfiguration();
        }
    }

    protected String getChangePasswordMsg(boolean edit) throws ManageDomainsResult, UnsupportedEncodingException {
        if (!args.contains(ARG_CHANGE_PASSWORD_MSG)) {
            return null;
        }
        String emptyValueDescription = edit ? " (Not providing a value will cause the existing value to be reset)" : "";
        String changePasswordMsgStr =
                System.console()
                        .readLine("Please enter message or URL to appear when user tries to login with an expired password"
                                + emptyValueDescription + ":");
        if (changePasswordMsgStr != null
                && (changePasswordMsgStr.indexOf("http") == 0 || changePasswordMsgStr.indexOf("https") == 0)) {
            try {
                URL url = new URL(changePasswordMsgStr);
                log.debug("Validated that " + url + " is in correct format");
            } catch (MalformedURLException ex) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ARGUMENT_VALUE,
                        "The provided value begins with a URL prefix of either http or https. However this is not a valid URL");
            }
        }
        // As the message may contain characters like space, "," and ":" - it should be encoded
        return StringUtils.isNotEmpty(changePasswordMsgStr) ? URLEncoder.encode(changePasswordMsgStr, "UTF-8") : "";
    }

    private String getPasswordInput() throws ManageDomainsResult {
        String pass = null;

        if (args.contains(ARG_PASSWORD_FILE)) {
            try {
                pass = readPasswordFile(args.get(ARG_PASSWORD_FILE));
            } catch (Exception e) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_READING_PASSWORD_FILE, e.getMessage());
            }
        } else {
            pass = readInteractively("Enter password:", true);
        }

        validatePassword(pass);

        return pass;
    }

    private void validatePassword(String pass) throws ManageDomainsResult {
        if (StringUtils.isBlank(pass)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.EMPTY_PASSWORD);
        }
        if (StringUtils.containsAny(pass, ILLEGAL_PASSWORD_CHARACTERS)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.ILLEGAL_PASSWORD);
        }
    }

    private String readInteractively(String prompt, boolean isPassword) {
        String value = null;
        try {
            if (isPassword) {
                char[] pass = System.console().readPassword(prompt);
                if (pass != null && pass.length > 0) {
                    value = new String(pass);
                }
            } else {
                value = System.console().readLine(prompt);
            }
        } catch (Exception ex) {
            value = null;
        }
        return value;
    }

    private String readPasswordFile(String passwordFile) throws FileNotFoundException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(passwordFile), "UTF-8"))) {
            String readLine = bufferedReader.readLine();
            return readLine;
        }
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

        for (String domain : createDomainNameList(domainNameEntry.getDomainNames(), true)) {
            String authMode = getDomainAuthMode(domain);
            String userName = adUserNameEntry.getValueForDomain(domain);

            System.out.println("Domain: " + domain);
            System.out.println("\tUser name: " + userName);
        }
    }

    protected List<String> getLdapServers(String domainName) throws ManageDomainsResult {
        ArrayList<String> servers = new ArrayList<String>();
        String argValue = args.get(ARG_LDAP_SERVERS);
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
            for (int counter = 0; counter < ldapDnsResult.getNumOfValidAddresses(); counter++) {
                // In case the address provides a port, don't keep it, we currently assume only the port
                // defined at ConfigValues.ldapServerPort is being used.
                String[] addressParts = ldapDnsResult.getAddresses()[counter].split(":");
                servers.add(addressParts[0]);
            }
        } else {
            servers = new ArrayList<String>(Arrays.asList(argValue.split(",")));
            for (String server : servers) {
                try {
                    for (InetAddress ip : InetAddress.getAllByName(server)) {
                        ip.getCanonicalHostName();
                        log.debug(String.format(
                                "Successfuly resolved IP '%s' for server '%s'",
                                ip.getHostAddress(),
                                server));
                    }
                } catch (Exception ex) {
                    String msg = String.format(
                            "Cannot resolve LDAP server hostname '%s'.",
                            server);
                    log.warn(msg, ex);
                    System.err.println(msg);
                }
            }
        }
        return servers;
    }

    public void addDomain() throws ManageDomainsResult {
        String authMode = LdapAuthModeEnum.GSSAPI.name();
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        String domainName = args.get(ARG_DOMAIN);
        String userName = args.get(ARG_USER);
        if (domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_ALREADY_EXISTS_IN_CONFIGURATION, domainName);
        }
        List<String> ldapServers = getLdapServers(domainName);
        validateKdcServers(authMode, domainName);
        domainNameEntry.setValueForDomain(domainName, null);

        String currentAdUserNameEntry = configurationProvider.getConfigValue(ConfigValues.AdUserName);
        String currentAdUserPasswordEntry = configurationProvider.getConfigValue(ConfigValues.AdUserPassword);
        String currentAuthModeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPSecurityAuthentication);
        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);
        String currentLDAPProviderTypes = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);
        String ldapServerPort = configurationProvider.getConfigValue(ConfigValues.LDAPServerPort);
        String currentChangePasswordUrl = configurationProvider.getConfigValue(ConfigValues.ChangePasswordMsg);

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
        DomainsConfigurationEntry changePasswordUrlEntry =
                new DomainsConfigurationEntry(currentChangePasswordUrl, DOMAIN_SEPERATOR, VALUE_SEPERATOR);


        LdapProviderType ldapProviderType = args.getLdapProvider();
        if (ldapProviderType == null) {
            System.err.println("Provider typ was not provided. Use --providerType=<ldap_provider_type");
        } else {
            adUserNameEntry.setValueForDomain(domainName, userName);
            adUserPasswordEntry.setValueForDomain(domainName, getPasswordInput());
            authModeEntry.setValueForDomain(domainName, authMode);
            ldapProviderTypesEntry.setValueForDomain(domainName, ldapProviderType.name());
            if (args.contains(ARG_LDAP_SERVERS)) {
                setLdapServersPerDomain(domainName, ldapServersEntry, StringUtils.join(ldapServers, ","));
            }
            handleChangePasswordMsg(domainName, changePasswordUrlEntry, false);
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

            handleAddPermissions(domainName, userName, adUserIdEntry.getValueForDomain(domainName));

            // Update the configuration
            setConfigurationEntries(domainNameEntry,
                    adUserNameEntry,
                    adUserPasswordEntry,
                    authModeEntry,
                    ldapServersEntry,
                    adUserIdEntry,
                    ldapProviderTypesEntry, changePasswordUrlEntry);

            printSuccessMessage(domainName, "added");
        }
    }

    private void setLdapServersPerDomain(String domainName,
            DomainsConfigurationEntry ldapServersEntry,
            String ldapServersStr) {
        if (!StringUtils.isEmpty(ldapServersStr)) {
            // Replacing "," with ";" - from user perspective of the utility, passing comma delimited string makes more
            // sense and more natural
            // But "," is used as domain separate character when storing to DB.
            ldapServersStr = ldapServersStr.replace(',', ';');
            ldapServersEntry.setValueForDomain(domainName, ldapServersStr);
        }
    }

    private void printSuccessMessage(String domainName, String action) {
        if (args.contains(ARG_ADD_PERMISSIONS)) {
            System.out.print(String.format(SUCCESS_MESSAGE_FOR_ACTION_WITH_ADD_PERMISSIONS, "added", domainName));
        }
        System.out.println(SERVICE_RESTART_MESSAGE);
    }

    private void handleAddPermissions(String domainName, String userName, String userId) {
        if (args.contains(ARG_ADD_PERMISSIONS)) {
            updatePermissionsTable(userName, domainName, userId);
        } else
        if (!userHasPermissions(userName, domainName)) {
            System.out.println(String.format(INFO_ABOUT_NOT_ADDING_PERMISSIONS, domainName));
        }
    }

    private ManageDomainsResult updatePermissionsTable(String userName, String domainName,
            String adUserId) {
        try {
            daoImpl.updatePermissionsTable(adUserId, userName, domainName);
            return OK_RESULT;
        } catch (SQLException e) {
            return new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_APPLYING_CHANGES_IN_DATABASE,
                    e.getMessage());
        }
    }

    private boolean userHasPermissions(String userName, String domainName) {
        boolean result = false;
        try {
            result = daoImpl.getUserHasPermissions(userName, domainName);
        } catch (SQLException ex) {
            log.error("Error testing user permissions", ex);
        }
        return result;
    }

    public void editDomain() throws ManageDomainsResult {
        String authMode;
        String domainName = args.get(ARG_DOMAIN);
        authMode = getDomainAuthMode(domainName);
        validateKdcServers(authMode, domainName);
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        String userName  = args.get(ARG_USER);
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
        String currentAdUserIdEntry = configurationProvider.getConfigValue(ConfigValues.AdUserId);
        String currentLdapProviderTypeEntry = configurationProvider.getConfigValue(ConfigValues.LDAPProviderTypes);
        String ldapServerPort = configurationProvider.getConfigValue(ConfigValues.LDAPServerPort);
        String currentChangePasswordUrl = configurationProvider.getConfigValue(ConfigValues.ChangePasswordMsg);


        DomainsConfigurationEntry adUserNameEntry =
                new DomainsConfigurationEntry(currentAdUserNameEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserPasswordEntry =
                new PasswordDomainsConfigurationEntry(currentAdUserPasswordEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry authModeEntry =
                new DomainsConfigurationEntry(currentAuthModeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry adUserIdEntry =
                new DomainsConfigurationEntry(currentAdUserIdEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry ldapProviderTypeEntry =
                new DomainsConfigurationEntry(currentLdapProviderTypeEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);
        DomainsConfigurationEntry changePaswordUrlEntry =
                new DomainsConfigurationEntry(currentChangePasswordUrl, DOMAIN_SEPERATOR, VALUE_SEPERATOR);


        if (userName != null) {
            adUserNameEntry.setValueForDomain(domainName, userName);
        }
        String password = getPasswordInput();
        if (password != null) {
            adUserPasswordEntry.setValueForDomain(domainName, password);
        }

        String currentLdapServersEntry = configurationProvider.getConfigValue(ConfigValues.LdapServers);
        DomainsConfigurationEntry ldapServersEntry =
                new DomainsConfigurationEntry(currentLdapServersEntry, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        List<String> ldapServers = getLdapServers(domainName);
        // Set the the obtained LDAP servers (either from arguments or from a DNS SRV record query
        // only if the -ldapServers option is used.

        if (args.contains(ARG_LDAP_SERVERS)) {
            setLdapServersPerDomain(domainName, ldapServersEntry, StringUtils.join(ldapServers, ","));
        }
        LdapProviderType ldapProviderType = null;
        if (args.contains(ARG_PROVIDER)) {
            ldapProviderType = args.getLdapProvider();
        }
        if (ldapProviderType != null) {
            ldapProviderTypeEntry.setValueForDomain(domainName, ldapProviderType.name());
        }

        handleChangePasswordMsg(domainName, changePaswordUrlEntry, true);

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

        handleAddPermissions(domainName, userName, adUserIdEntry.getValueForDomain(domainName));

        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry,
                ldapProviderTypeEntry,
                changePaswordUrlEntry);

        printSuccessMessage(domainName, "edited");
    }

    private void handleChangePasswordMsg(String domainName,
            DomainsConfigurationEntry changePaswordUrlEntry,
            boolean edit)
            throws ManageDomainsResult {
        if (args.contains(ARG_CHANGE_PASSWORD_MSG)) {
            try {
                String changePasswordMsgStr = getChangePasswordMsg(edit);
                if (StringUtils.isNotBlank(changePasswordMsgStr)) {
                    changePaswordUrlEntry.setValueForDomain(domainName, changePasswordMsgStr);
                } else {
                    changePaswordUrlEntry.removeValueForDomain(domainName);
                }

            } catch (UnsupportedEncodingException e) {
                log.error("Error in encoding the change password message. ", e);
            }
        }
    }

    private void createKerberosConfiguration(DomainsConfigurationEntry gssapiDomains, Map<String, List<String>> ldapServersPerGSSAPIDomains) throws ManageDomainsResult {
        if (!gssapiDomains.isEntryEmpty()) {
            String gssapiDomainsString = gssapiDomains.getDomainsConfigurationEntry();

            KrbConfCreator krbConfCreator;
            try {
                log.info("Creating kerberos configuration for domain(s): " + gssapiDomainsString);
                useDnsLookup = utilityConfiguration.getUseDnsLookup();
                String domainRealmMappingFile = utilityConfiguration.getDomainRealmMappingFile();
                if (!args.contains(ARG_LDAP_SERVERS) && useDnsLookup
                        || args.contains(ARG_RESOLVE_KDC)) {
                    // Arguments do not contain a list of ldap servers, so the
                    // kerberos configuration should not be created according to it if
                    // useDnsLookup is set to true or resolve KDC argument was entered.
                    // In those cases the kdc and the domain_realm information will be resolved
                    // by DNS during kerberos negotiation.
                    ldapServersPerGSSAPIDomains = Collections.emptyMap();
                }
                krbConfCreator = new KrbConfCreator(gssapiDomainsString, useDnsLookup, ldapServersPerGSSAPIDomains, domainRealmMappingFile);
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
            Map<String, List<String>> ldapServersPerDomainMap,
            String kerberosConfigFile,
            String ldapServerPort,
            boolean isValidate,
            List<String> ldapServers) throws ManageDomainsResult {

        for (String domain : createDomainNameList(gssapiDomains.getDomainNames(), isValidate)) {
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
                    System.out.println("The configured user for domain " + domain + " is " + currUserName + "\n");
                }
                log.info("Successfully tested kerberos configuration for domain: " + domain);
            } catch (Exception e) {
                ManageDomainsResult result =
                        new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN,
                                new String[] { domain, e.getMessage() });
                if ((isValidate && args.contains(ARG_REPORT)) || ((domainName != null) && !domain.equals(domainName))) {
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
        Pair<ReturnStatus, String> simpleCheckResult =
                simpleAuthenticationCheck.printUserGuid(domain, userName, password, userGuid, ldapProviderType, ldapServers);
        ManageDomainsResult result = null;
        if (simpleCheckResult == null) {
            result =
                    new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN, new String[] {
                            domain, "Unexepcted error has occured during testing." });
        }
        else if (!simpleCheckResult.getFirst().equals(ReturnStatus.OK)) {
            System.err.println(simpleCheckResult.getSecond());
            result = new ManageDomainsResult(ManageDomainsResultEnum.FAILURE_WHILE_TESTING_DOMAIN,
                    new String[] { domain, simpleCheckResult.getFirst().getDetailedMessage() });
        } else {
            result = OK_RESULT;
        }
        log.info("Successfully tested domain " + domain);
        return result;
    }

    private void checkSimpleDomains(String domainName,
            DomainsConfigurationEntry users,
            DomainsConfigurationEntry passwords,
            DomainsConfigurationEntry simpleDomains,
            DomainsConfigurationEntry userIds,
            DomainsConfigurationEntry ldapProviderType,
            Map<String, List<String>>  ldapServersMapPerDomainMap,
            boolean isValidate) throws ManageDomainsResult {

        StringBuffer userGuid = new StringBuffer();
        for (String domain : createDomainNameList(simpleDomains.getDomainNames(), isValidate)) {
            List<String> domainLdapServers = ldapServersMapPerDomainMap.get(domain);
            ManageDomainsResult result = checkSimple(domain,
                    users.getValueForDomain(domain),
                    passwords.getValueForDomain(domain),
                    userGuid, LdapProviderType.valueOf(ldapProviderType.getValueForDomain(domain)), domainLdapServers);
            if (!result.isSuccessful()) {
                if (isValidate || ((domainName != null) && !domain.equals(domainName))) {
                    if (args.contains(ARG_REPORT)) {
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

        Map<String, List<String>> ldapServersPerSimpleDomains = new HashMap<String, List<String>>();
        Map<String, List<String>> ldapServersPerGSSAPIDomains = new HashMap<String, List<String>>();


        for (Entry<String, String> currLdapServerEntry: ldapServersEntry.getValues()) {
            if (gssapiDomains.contains(currLdapServerEntry.getKey())) {
                ldapServersPerGSSAPIDomains.put(currLdapServerEntry.getKey(),
                        new ArrayList<String>(Arrays.asList(currLdapServerEntry.getValue().split(";"))));
            } else {
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
            DomainsConfigurationEntry ldapProviderTypeEntry, DomainsConfigurationEntry changePasswordUrlEntry)
            throws ManageDomainsResult {
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

        if (args.contains(ARG_CHANGE_PASSWORD_MSG)) {
            configurationProvider.setConfigValue(ConfigValues.ChangePasswordMsg, changePasswordUrlEntry);
        }
    }

    public void deleteDomain() throws ManageDomainsResult {

        String domainName = args.get(ARG_DOMAIN);
        String currentDomains = configurationProvider.getConfigValue(ConfigValues.DomainName);
        DomainsConfigurationEntry domainNameEntry =
                new DomainsConfigurationEntry(currentDomains, DOMAIN_SEPERATOR, null);

        if (!domainNameEntry.doesDomainExist(domainName)) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.DOMAIN_DOESNT_EXIST_IN_CONFIGURATION, domainName);
        }

        //Prompt warning about last domain only if not "force delete", as using
        //the force delete option should remove with no confirmation/warning
        if (domainNameEntry.getDomainNames().size() == 1 && !args.contains(ARG_FORCE)) {
            System.out.println(String.format(WARNING_ABOUT_TO_DELETE_LAST_DOMAIN, domainName));
        }

        if(!args.contains(ARG_FORCE) && !confirmDeleteDomain(domainName)) {
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
        String changePasswordUrl = configurationProvider.getConfigValue(ConfigValues.ChangePasswordMsg);

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

        DomainsConfigurationEntry changePasswordUrlEntry =
                new DomainsConfigurationEntry(changePasswordUrl, DOMAIN_SEPERATOR, VALUE_SEPERATOR);

        adUserNameEntry.removeValueForDomain(domainName);
        adUserIdEntry.removeValueForDomain(domainName);
        adUserPasswordEntry.removeValueForDomain(domainName);
        authModeEntry.removeValueForDomain(domainName);
        ldapServersEntry.removeValueForDomain(domainName);
        ldapProviderTypeEntry.removeValueForDomain(domainName);
        changePasswordUrlEntry.removeValueForDomain(domainName);

        // Update the configuration
        setConfigurationEntries(domainNameEntry,
                adUserNameEntry,
                adUserPasswordEntry,
                authModeEntry,
                ldapServersEntry,
                adUserIdEntry,
                ldapProviderTypeEntry, changePasswordUrlEntry);

        System.out.println(String.format(DELETE_DOMAIN_SUCCESS, domainName));
    }

    private boolean confirmDeleteDomain(String domainName) {
        String response = null;
        while (StringUtils.isBlank(response)
                || (!StringUtils.equalsIgnoreCase(response, "yes")
                 && !StringUtils.equalsIgnoreCase(response, "no"))) {
            System.out.print("Are you sure you like to delete domain " + domainName + " (yes/no): ");
            response = System.console().readLine();
        }
        return response.equals("yes");
    }

    private static void copyFile(String srcFilePath, String dstFilePath) throws IOException {
        try (FileInputStream in = new FileInputStream(srcFilePath)) {
            try (FileOutputStream out = new FileOutputStream(dstFilePath)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                log.info("Failed deleting file " + file.getAbsolutePath() + ". Continuing anyway.");
            }
        }
    }

    /**
     * Creates temporary properties file for engine-config to work with configuration values that
     * are not exposed by standard {@code engine-config.properties} file
     *
     * @return absolute path of properties file
     */
    private String createTempPropFile() throws ManageDomainsResult {
        File propFile = null;
        try {
            propFile = File.createTempFile("engine-config", "properties");
            propFile.deleteOnExit();
        } catch (IOException ex) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.ERROR_CREATING_PROPERTIES_FILE,
                    ex.getMessage());
        }

        try (FileWriter fw = new FileWriter(propFile)) {
            fw.write(new StringBuilder()
                    .append(ConfigValues.AdUserName.name())
                    .append("=\n")
                    .append(ConfigValues.AdUserPassword.name())
                    .append(".type=CompositePassword\n")
                    .append(ConfigValues.LDAPSecurityAuthentication.name())
                    .append("=\n")
                    .append(ConfigValues.DomainName.name())
                    .append("=\n")
                    .append(ConfigValues.AdUserId.name())
                    .append("=\n")
                    .append(ConfigValues.LdapServers.name())
                    .append("=\n")
                    .append(ConfigValues.LDAPProviderTypes.name())
                    .append("=\n")
                    .append(ConfigValues.LDAPServerPort.name())
                    .append("=\n")
                    .append(ConfigValues.ChangePasswordMsg.name())
                    .append("=\n")
                    .toString());
            fw.flush();
        } catch (IOException ex) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.ERROR_CREATING_PROPERTIES_FILE,
                    ex.getMessage());
        }
        return propFile.getAbsolutePath();
    }

    /**
     * Creates list of domains names
     *
     * @param entries
     *            set of domain entries
     * @param sorted
     *            {@code true} if called domains should be sorted by name, otherwise {@code false}
     * @return list of domains names
     */
    private List<String> createDomainNameList(Set<String> entries, boolean sorted) {
        List<String> names = new ArrayList<>(entries);
        if (sorted) {
            Collections.sort(names);
        }
        return names;
    }
}
