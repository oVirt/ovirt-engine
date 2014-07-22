package org.ovirt.engine.extensions.aaa.builtin.tools;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.cli.ArgumentBuilder;
import org.ovirt.engine.core.uutils.cli.ExtendedCliParser;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

/**
 * Class for command line arguments parsing and validation
 */
public class ManageDomainsArguments {
    /**
     * Path for help properties
     */
    private static final String HELP_PROPERTIES = "/manage-domains-help.properties";

    /**
     * Action
     */
    public static final String ARG_ACTION = "--action";

    /**
     * Add action
     */
    public static final String ACTION_ADD = "add";

    /**
     * Edit action
     */
    public static final String ACTION_EDIT = "edit";

    /**
     * Delete action
     */
    public static final String ACTION_DELETE = "delete";

    /**
     * List action
     */
    public static final String ACTION_LIST = "list";

    /**
     * Validate action
     */
    public static final String ACTION_VALIDATE = "validate";

    /**
     * Add permissions
     */
    public static final String ARG_ADD_PERMISSIONS = "--add-permissions";

    /**
     * Change password message
     */
    public static final String ARG_CHANGE_PASSWORD_MSG = "--change-password-msg";

    /**
     * Config file
     */
    public static final String ARG_CONFIG_FILE = "--config-file";

    /**
     * Domain
     */
    public static final String ARG_DOMAIN = "--domain";

    /**
     * Force delete
     */
    public static final String ARG_FORCE = "--force";

    /**
     * Help
     */
    public static final String ARG_HELP = "--help";

    /**
     * LDAP servers
     */
    public static final String ARG_LDAP_SERVERS = "--ldap-servers";

    /**
     * Log file
     */
    public static final String ARG_LOG_FILE = "--log-file";

    /**
     * Log level
     */
    public static final String ARG_LOG_LEVEL = "--log-level";

    /**
     * Log4j config file
     */
    public static final String ARG_LOG4J_CONFIG = "--log4j-config";

    /**
     * Password file
     */
    public static final String ARG_PASSWORD_FILE = "--password-file";

    /**
     * Provider
     */
    public static final String ARG_PROVIDER = "--provider";

    /**
     * Provider - Active Directory
     */
    private static final String PROV_AD = "ad";

    /**
     * Provider - Free IPA
     */
    private static final String PROV_IPA = "ipa";

    /**
     * Provider - Red Hat Directory Servers
     */
    private static final String PROV_RHDS = "rhds";

    /**
     * Provider - IBM Tivoli Directory Server
     */
    private static final String PROV_ITDS = "itds";

    /**
     * Provider - OpenLDAP
     */
    private static final String PROV_OLDAP = "oldap";

    /**
     * Report
     */
    public static final String ARG_REPORT = "--report";

    /**
     * Resolve KDC servers (don't suppose they are the same LDAP servers)
     */
    public static final String ARG_RESOLVE_KDC = "--resolve-kdc";

    /**
     * Username
     */
    public static final String ARG_USER = "--user";

    /**
     * Map with parsed argument and their values
     */
    private Map<String, String> argMap;

    /**
     * Returns set of required args for specified action
     */
    private Set<String> getRequiredArgs(String action) {
        Set<String> result = new LinkedHashSet<>();

        if (ACTION_ADD.equals(action)) {
            result.add(ARG_DOMAIN);
            result.add(ARG_PROVIDER);
            result.add(ARG_USER);

        } else if (ACTION_EDIT.equals(action)) {
            result.add(ARG_DOMAIN);

        } else if (ACTION_DELETE.equals(action)) {
            result.add(ARG_DOMAIN);
        }

        return result;
    }

    /**
     * Configures argument parser and returns its instance
     *
     * @param action specified action
     */
    private ExtendedCliParser initParser(String action) {
        ExtendedCliParser parser = new ExtendedCliParser();

        parser.addArg(new ArgumentBuilder()
                .longName(ARG_CONFIG_FILE)
                .valueRequired(true)
                .build());

        parser.addArg(new ArgumentBuilder()
                .longName(ARG_LOG_FILE)
                .valueRequired(true)
                .build());

        parser.addArg(new ArgumentBuilder()
                .longName(ARG_LOG_LEVEL)
                .valueRequired(true)
                .build());

        parser.addArg(new ArgumentBuilder()
                .longName(ARG_LOG4J_CONFIG)
                .valueRequired(true)
                .build());

        if (ACTION_ADD.equals(action) || ACTION_EDIT.equals(action)) {
            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_DOMAIN)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_PROVIDER)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_USER)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_ADD_PERMISSIONS)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_LDAP_SERVERS)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_RESOLVE_KDC)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_PASSWORD_FILE)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_CHANGE_PASSWORD_MSG)
                    .build());

        } else if (ACTION_DELETE.equals(action)) {
            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_DOMAIN)
                    .valueRequired(true)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_FORCE)
                    .build());

            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_PASSWORD_FILE)
                    .valueRequired(true)
                    .build());

        } else if (ACTION_VALIDATE.equals(action)) {
            parser.addArg(new ArgumentBuilder()
                    .longName(ARG_REPORT)
                    .build());

        }

        return parser;
    }

    /**
     * Returns value of specified argument
     *
     * @param argName
     *            name of specified argument
     */
    public String get(String argName) {
        return argMap.get(argName);
    }

    /**
     * Returns {@code true} if specified argument has been entered, otherwise {@code false}
     *
     * @param argName
     *            name of specified argument
     */
    public boolean contains(String argName) {
        return argMap.containsKey(argName);
    }

    /**
     * Parses specified arguments and validates them
     *
     * @param args
     *            specified arguments
     */
    public void parse(String[] args) throws ManageDomainsResult {
        if (args.length < 1 || ARG_HELP.equals(args[0])) {
            // print help
            argMap = new HashMap<>();
            argMap.put(ARG_HELP, null);
            return;
        }

        if (!isValidAction(args[0])) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ACTION, args[0]);
        }

        if (args.length > 1) {
            // entered more args than just action, parse them
            try {
                ExtendedCliParser parser = initParser(args[0]);
                argMap = parser.parse(args, 1, args.length);
            } catch (IllegalArgumentException ex) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.ARGUMENT_PARSING_ERROR, ex.getMessage());
            }
        } else {
            argMap = new HashMap<>();
        }

        argMap.put(ARG_ACTION, args[0]);

        // check that all required args are present
        for (String arg : getRequiredArgs(get(ARG_ACTION))) {
            if (!argMap.containsKey(arg)) {
                throw new ManageDomainsResult(
                        ManageDomainsResultEnum.ARGUMENT_IS_REQUIRED,
                        arg);
            }
        }

        convertDomain();
        convertProvider();
        validateProvider();

        // set default config file if user one was not entered
        if (!contains(ARG_CONFIG_FILE)) {
            argMap.put(ARG_CONFIG_FILE, getDefaultConfigPath());
        }
    }

    /**
     * Return {@code true} if valid action has been specified, otherwise {@code false}
     *
     * @param action
     *            specified action
     */
    private boolean isValidAction(String action) {
        return action != null
                && (ACTION_ADD.equals(action)
                        || ACTION_EDIT.equals(action)
                        || ACTION_DELETE.equals(action)
                        || ACTION_LIST.equals(action)
                        || ACTION_VALIDATE.equals(action));
    }

    /**
     * Converts string provider value to match name of {@link LdapProviderType} enum
     */
    private void convertProvider() {
        if (contains(ARG_PROVIDER)) {
            String providerStr = get(ARG_PROVIDER).toLowerCase();
            if (PROV_AD.equals(providerStr)) {
                providerStr = LdapProviderType.activeDirectory.name();
            } else if (PROV_OLDAP.equals(providerStr)) {
                providerStr = LdapProviderType.openLdap.name();
            }
            argMap.put(ARG_PROVIDER, providerStr);
        }
    }

    /**
     * Converts string domain value to lower case
     */
    private void convertDomain() {
        if (contains(ARG_DOMAIN)) {
            String domainStr = get(ARG_DOMAIN);
            if (domainStr != null) {
                argMap.put(ARG_DOMAIN, domainStr.toLowerCase());
            }
        }
    }

    /**
     * Validates provider specified as an argument
     *
     * @throws ManageDomainsResult
     *             if provider is not valid
     */
    private void validateProvider() throws ManageDomainsResult {
        if (contains(ARG_PROVIDER)) {
            try {
                LdapProviderType.valueOfIgnoreCase(get(ARG_PROVIDER));
            } catch (IllegalArgumentException | NullPointerException ex) {
                throw new ManageDomainsResult(ManageDomainsResultEnum.INVALID_ARGUMENT_VALUE,
                        String.format("Invalid provider, valid providers are: '%s'.",
                            StringUtils.join(
                                    new String[] { PROV_AD, PROV_IPA, PROV_RHDS, PROV_ITDS, PROV_OLDAP },
                                    ", ")));
            }
        }
    }

    /**
     * Returns default path to configuration file
     *
     */
    private String getDefaultConfigPath() throws ManageDomainsResult {
        try {
            return new File(
                    EngineLocalConfig.getInstance().getEtcDir(),
                    "engine-manage-domains/engine-manage-domains.conf")
                    .getAbsolutePath();
        } catch (Exception ex) {
            throw new ManageDomainsResult(ManageDomainsResultEnum.FAILED_READING_CONFIGURATION,
                    ex.getMessage());
        }
    }

    /**
     * Converts LDAP provider entered as an argument into {@link LdapProviderType} and returns it
     *
     * @return instance of {@link LdapProviderType} or {@code null} if provider was not entered as an argument
     */
    public LdapProviderType getLdapProvider() {
        LdapProviderType provider = null;
        if (contains(ARG_PROVIDER)) {
            provider = LdapProviderType.valueOfIgnoreCase(get(ARG_PROVIDER));
        }
        return provider;
    }

    /**
     * Prints help to stdout
     */
    public void printHelp() {
        Properties helpProp = new Properties();
        try (InputStream is = getClass().getResourceAsStream(HELP_PROPERTIES)) {
            helpProp.load(is);
        } catch (Exception ex) {
            System.out.println("Error reading help content");
        }
        if (!helpProp.isEmpty()) {
            System.out.println(helpProp.getProperty("help.usage"));
            System.out.println(helpProp.getProperty("help.actions"));
            System.out.println(helpProp.getProperty("help.options"));
        }
    }
}
