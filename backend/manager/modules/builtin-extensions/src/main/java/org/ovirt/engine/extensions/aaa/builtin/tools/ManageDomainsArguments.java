package org.ovirt.engine.extensions.aaa.builtin.tools;

/**
 * Class for command line arguments parsing and validation
 */
public class ManageDomainsArguments {
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
     * Change password message
     */
    public static final String ARG_CHANGE_PASSWORD_MSG = "change-password-msg";

    /**
     * Config file
     */
    public static final String ARG_CONFIG_FILE = "config-file";

    /**
     * Domain
     */
    public static final String ARG_DOMAIN = "domain";

    /**
     * Force delete
     */
    public static final String ARG_FORCE = "force";

    /**
     * Help
     */
    public static final String ARG_HELP = "help";

    /**
     * LDAP servers
     */
    public static final String ARG_LDAP_SERVERS = "ldap-servers";

    /**
     * Log file
     */
    public static final String ARG_LOG_FILE = "log-file";

    /**
     * Log level
     */
    public static final String ARG_LOG_LEVEL = "log-level";

    /**
     * Password file
     */
    public static final String ARG_PASSWORD_FILE = "password-file";

    /**
     * Provider
     */
    public static final String ARG_PROVIDER = "provider";

    /**
     * Report
     */
    public static final String ARG_REPORT = "report";

    /**
     * Resolve KDC servers (don't suppose they are the same LDAP servers)
     */
    public static final String ARG_RESOLVE_KDC = "resolve-kdc";

    /**
     * Username
     */
    public static final String ARG_USER = "user";

    /**
     * Provider - Active Directory
     */
    public static final String PROV_AD = "ad";

    /**
     * Provider - OpenLDAP
     */
    public static final String PROV_OLDAP = "oldap";
}
