package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.CommunicationException;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Helper class for AD issues
 *
 */
public class LdapBrokerUtils {

    private static final Log log = LogFactory.getLog(LdapBrokerUtils.class);


    /**
     * This method should parse a string in the following format: CN=groupname,OU=ouSub,OU=ouMain,DC=qumranet,DC=com to
     * the following format qumranet.com/ouMain/ouSub/groupname it should also handle '\,' and '\=' as ',' and '='.
     *
     * @param ldapname
     * @return
     */
    public static String generateGroupDisplayValue(String ldapname) {
        if (ldapname == null) {
            return "";
        }
        LdapName name;
        try {
            name = new LdapName(ldapname);
        } catch (InvalidNameException e) {
            // fail to generate a nice display value. Retuning the String we got.
            return ldapname;
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder domainName = new StringBuilder();

        List<Rdn> rdns = name.getRdns();
        for (Rdn rdn : rdns) {
            String type = rdn.getType();
            String val = (String) rdn.getValue();
            if (type.equalsIgnoreCase("dc")) {
                if (domainName.length() > 0) {
                    domainName.insert(0, ".");
                }
                domainName.insert(0, val);
                continue;
            } else {
                sb.append("/" + val);
            }
        }
        // remove the first "." character.
        sb.delete(0, 1);
        sb.append("@").append(domainName);
        return sb.toString();
    }


    /**
     * Performs a query on a group by using its DN as baseDN to perform an object-scope search (in order to optimize the
     * search
     *
     * @param loginName
     *            login of AD user to perform the query with
     * @param password
     *            password of AD user to perform the query with
     * @param domainName
     *            domain of LDAP server to perform the query against
     * @param queryData
     *            object that contain query information (query filter + base DN)
     * @return list of results
     */
    public static List<GroupSearchResult> performGroupQuery(Properties configuration, String loginName,
            String password,
            String domainName,
            LdapQueryData queryData) {

        LdapCredentials ldapCredentials =
                new LdapCredentials(LdapBrokerUtils.modifyLoginNameForKerberos(loginName, domainName, configuration),
                        password);
        DirectorySearcher directorySearcher =
                new DirectorySearcher(configuration, ldapCredentials);

        try {
            List<GroupSearchResult> searchResults = (List<GroupSearchResult>) directorySearcher.findAll(queryData);

            return searchResults;
        } catch (DomainNotConfiguredException ex) {
            log.errorFormat("User {0} from domain {1} is a member of a group from {2} which is not configured. Please use the manage domains utility if you wish to add this domain.",
                    loginName,
                    domainName,
                    queryData.getDomain());
            return null;
        }

    }

    public static String hadleNameEscaping(String name) {
        return StringUtils.countMatches(name, "\\") == 1 ? name.replace("\\", "\\\\\\") : name;
    }

    public static String modifyLoginNameForKerberos(String loginName, String domain, Properties configuration) {
        String[] parts = loginName.split("[@]");

        LDAPSecurityAuthentication securityAuthentication =
                LDAPSecurityAuthentication.valueOf(configuration.getProperty("config.LDAPSecurityAuthentication"));
        boolean isKerberosAuth = securityAuthentication.equals(LDAPSecurityAuthentication.GSSAPI);

        // if loginName is not in format of user@domain
        if (parts.length != 2) {

            // when Kerberos is the auth mechanism we must use UPN to otherwise
            // the default REALM, as confugured in krb5.conf will be picked
            return isKerberosAuth ? loginName + "@" + domain.toUpperCase() : loginName;
        }

        // In case the login name is in format of user@domain, it should be
        // transformed to user@realm - realm is a capitalized version of fully
        // qualified domain name

        StringBuilder result = new StringBuilder();
        result.append(parts[0]);
        if (isKerberosAuth) {
            String realm = parts[1].toUpperCase();
            result.append("@").append(realm);
        }
        return result.toString();
    }

    public static String getGroupDomain(String ldapname) {
        if (ldapname == null) {
            return "";
        }
        LdapName name;
        try {
            name = new LdapName(ldapname);
        } catch (InvalidNameException e) {
            // fail to generate a nice display value. Retuning the String we got.
            return ldapname;
        }

        StringBuilder sb = new StringBuilder();

        List<Rdn> rdns = name.getRdns();
        for (Rdn rdn : rdns) {
            String type = rdn.getType();
            String val = (String) rdn.getValue();
            if (type.equalsIgnoreCase("dc")) {
                sb.insert(0, "." + val);
                continue;
            }
        }
        // remove the first "." character.
        sb.delete(0, 1);
        return sb.toString();
    }

    public static String getGuidFromNsUniqueId(String nsUniqueId) {
        // 12345678-12345678-12345678-12345678 -->
        // 12345678-1234-5678-1234-567812345678
        StringBuilder sb = new StringBuilder();
        sb.append(nsUniqueId.substring(0, 13))
                .append("-")
                .append(nsUniqueId.substring(13, 22))
                .append("-")
                .append(nsUniqueId.substring(22, 26))
                .append(nsUniqueId.substring(27, 35));
        return sb.toString();
    }

    /**
     * set ldap configuration with ConfigValue data
     *
     * @param env hashtable of parameters for ldap configuration.
     * this method adds to hashtable specific ldap configuration.
     */
    public static void addLdapConfigValues(Properties config, Hashtable<String, String> env) {
        env.put("com.sun.jndi.ldap.read.timeout",
                Long.toString(Long.parseLong(config.getProperty("config.LDAPQueryTimeout")) * 1000));
        env.put("com.sun.jndi.ldap.connect.timeout",
                Long.toString(Long.parseLong(config.getProperty("config.LDAPConnectTimeout")) * 1000));
    }

    /**
     * Gets a string representing a friendly version for possible
     * exceptions that are thrown during LDAP queries
     * @param th throwable object to get friendly string representation for
     * @return friendly version if possible, or the original exception message if not
     */
    public static String getFriendlyExceptionMessage(Throwable th) {
        Throwable ex = ExceptionUtils.getRootCause(th);
        //Root cause should return the real root cause of the exception
        //in chain of Exception wrapping
        //If it fails to return it, the friendly version should be
        //checked for type of the passed throwable
        if (ex == null) {
            ex = th;
        }
        if (ex instanceof SocketTimeoutException) {
            return "connection timeout";
        }
        if (ex instanceof CommunicationException) {
            return "communication error";
        }
        return th.getMessage();
    }

}
