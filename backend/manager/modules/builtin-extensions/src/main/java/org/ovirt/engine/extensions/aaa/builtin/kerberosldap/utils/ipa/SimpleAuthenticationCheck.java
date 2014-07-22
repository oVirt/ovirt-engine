package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class SimpleAuthenticationCheck {
    private static String INVALID_CREDENTIALS_ERROR_CODE = "49";

    public enum Arguments {
        domain,
        user,
        password,
        ldapProviderType
    }


    private String getLdapUrl(String ldapServer) {
        return "ldap://" + ldapServer;
    }


    public Pair<ReturnStatus, String> printUserGuid(String domain,
            String username,
            String password,
            StringBuffer userGuid, LdapProviderType ldapProviderType, List<String> ldapServers) {

        Pair<ReturnStatus, String> status = null;
        for (String ldapServerUrl : ldapServers) {
            status = checkSimpleLdapServer(domain, username, password, userGuid, ldapProviderType, ldapServerUrl);
            if (status.getFirst().getExitCode() == ReturnStatus.OK.getExitCode()) {
                return status;
            }
        }
        return status;
    }

    private Pair<ReturnStatus, String> checkSimpleLdapServer(String domain,
            String username,
            String password,
            StringBuffer userGuid,
            LdapProviderType ldapProviderType,
            String ldapServerUrl) {
        LdapContextSource contextSource = getContextSource(domain, ldapProviderType, username, password, ldapServerUrl);
        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            return new Pair(ReturnStatus.LDAP_CONTEXT_FAILURE, ERROR_PREFIX + "Failed setting LDAP context for domain " + domain);
        }

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        String query = "";
        ContextMapper contextMapper;

        if (ldapProviderType.equals(LdapProviderType.ipa)) {
            query = "(&(objectClass=posixAccount)(objectClass=krbPrincipalAux)(uid=" + username + "))";
            contextMapper = new IPAUserContextMapper();
            // AD
        } else if (ldapProviderType.equals(LdapProviderType.activeDirectory)) {
            contextMapper = new ADUserContextMapper();
            // ITDS
        } else if (ldapProviderType.equals(LdapProviderType.itds)) {
            query = "(&(objectClass=person)(uid=" + username + "))";
            contextMapper = new ITDSUserContextMapper();
            // RHDS
        } else {
            query = "(&(objectClass=person)(uid=" + username + "))";
            contextMapper = new RHDSUserContextMapper();
        }

        try {
            List searchResult =
                    ldapTemplate.search("", query, contextMapper);
            if (searchResult == null) {
                return new Pair(ReturnStatus.CANNOT_QUERY_USER, ERROR_PREFIX + "Cannot query user " + username + " from domain " + domain);
            } else {
                userGuid.append((String) searchResult.get(0));
            }
        } catch (AuthenticationException authEx) {
            return authenticationReturnStatus(authEx, username, domain);
        } catch (Exception ex) {
            return new Pair(ReturnStatus.CANNOT_QUERY_USER, ERROR_PREFIX + "Cannot query user " + username + " from domain " + domain
                    + ", details: " + ex.getMessage());
        }

        return new Pair(ReturnStatus.OK, "");
    }

    /***
     * Returns the ReturnStatus according to the given AuthenticationException. Either INVALID_CREDENTIALS if this is
     * the case, or the general CANNOT_AUTHENTICATE_USER otherwise.
     *
     * @param authEx
     */
    private Pair<ReturnStatus, String> authenticationReturnStatus(AuthenticationException authEx, String userName, String domain) {
        ReturnStatus returnStatus = ReturnStatus.CANNOT_AUTHENTICATE_USER;
        String authExMessage = authEx.getMessage();

        // Using contains() since the AuthenticationException does not have an error code property
        String msg = null;
        if (authExMessage != null && authExMessage.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
            msg = ERROR_PREFIX + "Invalid credentials for " + userName + " and domain " + domain
                    + ", details: " + authEx.getMessage();
            returnStatus = ReturnStatus.INVALID_CREDENTIALS;
        } else {
            msg = ERROR_PREFIX + "Cannot authenticate user " + userName + " to domain " + domain
                    + ", details: " + authEx.getMessage();
        }
        return new Pair(returnStatus, msg);
    }

    private static String domainToDN(String domain) {

        String returnValue = "dc=" + domain.replaceAll("\\.", ",dc=");

        return returnValue;
    }

    private LdapContextSource getContextSource(String domain,
            LdapProviderType ldapProviderType,
            String username,
            String password,
            String ldapServer) {
        LdapContextSource context = new LdapContextSource();

        String ldapBaseDn = domainToDN(domain);
        StringBuilder ldapUserDn = new StringBuilder();

        if (ldapProviderType.equals(LdapProviderType.ipa)) {
            ldapUserDn.append("uid=").append(username).append(",cn=Users").append(",cn=Accounts,");
        } else if (ldapProviderType.equals(LdapProviderType.rhds)) {
            ldapUserDn.append("uid=").append(username).append(",ou=People");
        } else if (ldapProviderType.equals(LdapProviderType.itds)) {
            ldapUserDn.append("uid=").append(username);
        } else {
            ldapUserDn.append("CN=").append(username).append(",CN=Users,");
        }

        ldapUserDn.append(ldapBaseDn);

        context.setUrl(getLdapUrl(ldapServer));
        if (!ldapProviderType.equals(LdapProviderType.itds)) {
            context.setBase(ldapBaseDn);
        } else {
            context.setAnonymousReadOnly(true);
        }
        context.setUserDn(ldapUserDn.toString());
        context.setPassword(password);
        context.setReferral("follow");
        Map<String, String> baseEnvironmentProperties = new HashMap<String, String>();
        // objectGUID - for AD
        baseEnvironmentProperties.put("java.naming.ldap.attributes.binary", "objectGUID");
        context.setBaseEnvironmentProperties(baseEnvironmentProperties);
        return context;
    }

    private static DirContext getDirContext(String ldapServer) throws NamingException {
        Hashtable env = new Hashtable(11);
        env.put(Context.SECURITY_AUTHENTICATION, "SIMPLE");
        env.put(Context.SECURITY_PRINCIPAL, "");
        env.put(Context.SECURITY_CREDENTIALS, "");
        env.put(Context.REFERRAL, "follow");

        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapServer.toString());

        return new InitialDirContext(env);
    }

}
