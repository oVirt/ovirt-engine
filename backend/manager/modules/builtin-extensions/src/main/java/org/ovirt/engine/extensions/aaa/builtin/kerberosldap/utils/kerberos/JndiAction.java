package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import java.net.URI;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.sasl.SaslException;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa.RHDSUserContextMapper;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapSRVLocator;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.RootDSEData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAAS Privileged action to be run when KerbersUtil successfully authenticates. This action performs ldap query to
 * retrieve information on the authenticated user and prints the object GUID of that user.
 */
public class JndiAction implements PrivilegedAction {

    private String userName;
    private final String domainName;
    private final LdapProviderType ldapProviderType;
    private final StringBuffer userGuid;
    private List<String> ldapServers;
    private final String defaultLdapServerPort;
    private final static Logger log = LoggerFactory.getLogger(JndiAction.class);

    public JndiAction(String userName, String domainName, StringBuffer userGuid, LdapProviderType ldapProviderType, List<String> ldapServers, String defaultLdapServerPort) {
        this.userName = userName;
        this.domainName = domainName;
        this.ldapProviderType = ldapProviderType;
        this.userGuid = userGuid;
        this.ldapServers = ldapServers;
        this.defaultLdapServerPort = defaultLdapServerPort;
    }

    @Override
    public Object run() {
        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "objectGUID");
        env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
        env.put("javax.security.sasl.qop", "auth-conf");

        // Send an SRV record DNS query to retrieve all the LDAP servers in the domain
        LdapSRVLocator locator = new LdapSRVLocator();
        if (ldapServers == null ) {
            try {
                ldapServers = locator.getServersList(locator.getLdapServers(domainName));
            } catch (Exception ex) {
                return KerberosUtils.convertDNSException(ex);
            }
        }


        String currentLdapServer = null;

        if (ldapServers == null || ldapServers.size() == 0) {
            return AuthenticationResult.CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN;
        }

        // Goes over all the retrieved LDAP servers
        for (String address : ldapServers) {
            DirContext ctx = null;
            try {
                // Constructs an LDAP url in a format of ldap://hostname:port (based on the data in the SRV record
                // This URL is not enough in order to query for user - as for querying users, we should also provide a
                // base dn, for example: ldap://hostname:389/DC=abc,DC=com . However, this URL (ldap:hostname:port)
                // suffices for
                // getting the rootDSE information, which includes the baseDN.
                URI uri = locator.constructURI("LDAP", address, defaultLdapServerPort);

                try {
                    // Get the base DN from rootDSE
                    env.put(Context.PROVIDER_URL, uri.toString());

                    String domainDN = getDomainDN(uri.toString());
                    if (domainDN != null) {

                        // Append the base DN to the ldap URL in order to construct a full ldap URL (in form of
                        // ldap:hostname:port/baseDN ) to query for the user
                        StringBuilder ldapQueryPath = new StringBuilder(uri.toString());
                        ldapQueryPath.append("/").append(domainDN);
                        SearchControls controls = new SearchControls();
                        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                        // Adding all the three attributes possible, as RHDS doesn't return the nsUniqueId by default
                        controls.setReturningAttributes(new String[] { "nsUniqueId", "ipaUniqueId", "objectGuid",
                                "uniqueIdentifier", "entryuuid" });
                        // Added this in order to prevent a warning saying:
                        // "the returning obj flag wasn't set, setting it to true"
                        controls.setReturningObjFlag(true);
                        currentLdapServer = ldapQueryPath.toString();
                        env.put(Context.PROVIDER_URL, currentLdapServer);

                        // Run the LDAP query to get the user
                        ctx = new InitialDirContext(env);
                        NamingEnumeration<SearchResult> answer = executeQuery(ctx, controls, prepareQuery());

                        if (answer.hasMoreElements()) {
                            // Print the objectGUID for the user as well as URI and query path
                            String guid = guidFromResults(answer.next());
                            if (guid != null) {
                                userGuid.append(guid);
                                logQueryContext(userGuid.toString(), uri.toString(), currentLdapServer);
                                return AuthenticationResult.OK;
                            }
                        }
                        // Print user GUID and another logging info only if it was not printed previously already
                        logQueryContext(userGuid.toString(), uri.toString(), currentLdapServer);
                        System.out.println("No user in Directory was found for " + userName
                                + ". Trying next LDAP server in list");
                    } else {
                        System.out.println(InstallerConstants.ERROR_PREFIX
                                + " Failed to query rootDSE in order to get the baseDN. Could not query for user "
                                + userName + " in domain " + domainName);
                    }
                } finally {
                    if (ctx != null) {
                        ctx.close();
                    }
                }
            } catch (CommunicationException ex) {
                handleCommunicationException(currentLdapServer, address);
            } catch (AuthenticationException ex) {
                handleAuthenticationException(ex);
            } catch (NegativeArraySizeException ex) {
                log.error("Internal Kerberos error.", ex);
                return AuthenticationResult.INTERNAL_KERBEROS_ERROR;
            } catch (Exception ex) {
                handleGeneralException(ex);
                break;
            }
        } // end of loop on addresses

        return AuthenticationResult.NO_USER_INFORMATION_WAS_FOUND_FOR_USER;

    }

    protected void handleGeneralException(Exception ex) {
        System.out.println("General error has occured" + ex.getMessage());
        ex.printStackTrace();
    }

    protected void handleAuthenticationException(AuthenticationException ex) {
        AuthenticationResult result = AuthenticationResult.OTHER;
        KerberosReturnCodeParser parser = new KerberosReturnCodeParser();
        result = parser.parse(ex.toString());
        String errorMsg = result.getDetailedMessage().replace("Authentication Failed", "LDAP query Failed");
        System.out.println(InstallerConstants.ERROR_PREFIX + errorMsg);
        String krbLoginModuleErrorMsg = ex.getMessage();
        if (ex.getRootCause() instanceof SaslException) {
            SaslException saslException = (SaslException)ex.getRootCause();
            krbLoginModuleErrorMsg = saslException.getMessage();
        }
        log.error("Error during login to kerberos. Detailed information is: {}", krbLoginModuleErrorMsg);
        log.debug("Error during login to kerberos. Detailed information is: ", ex);
    }

    protected void handleCommunicationException(String currentLdapServer, String address) {
        String communicationFailureReason = null;
        if (currentLdapServer != null) {
            communicationFailureReason = "Cannot connect to LDAP URL: " + currentLdapServer;
        } else {
            if (address != null) {
                communicationFailureReason = "Cannot connect to LDAP server " + address;
            } else {
                communicationFailureReason =
                        "Error in connectiong to LDAP server. LDAP server URL could not be obtained";
            }
        }
        System.out.println(communicationFailureReason
                + ". Trying next LDAP server in list (if exists)");
    }

    private String guidFromResults(SearchResult sr) throws NamingException {
        String guidString = "";

        try {
            if (ldapProviderType.equals(LdapProviderType.ipa)) {
                String ipaUniqueId = (String) sr.getAttributes().get("ipaUniqueId").get();
                guidString += ipaUniqueId;
            } else if (ldapProviderType.equals(LdapProviderType.rhds)) {
                String nsUniqueId = (String) sr.getAttributes().get("nsUniqueId").get();
                guidString += RHDSUserContextMapper.getGuidFromNsUniqueId(nsUniqueId);
            } else if (ldapProviderType.equals(LdapProviderType.itds)) {
                String uniqueId = (String) sr.getAttributes().get("uniqueIdentifier").get();
                guidString += uniqueId;
            } else if (ldapProviderType.equals(LdapProviderType.openLdap)) {
                String uniqueId = (String) sr.getAttributes().get("entryUUID").get();
                guidString += uniqueId;
            } else {
                Object objectGuid = sr.getAttributes().get("objectGUID").get();
                byte[] guid = (byte[]) objectGuid;
                guidString += new Guid(guid, false).toString();
            }
        } catch (NullPointerException ne) {
            System.out.println("LDAP connection successful. But no guid found");
            guidString = null;
        }
        return guidString;
    }

    private String prepareQuery() {
        String query;
        if (ldapProviderType.equals(LdapProviderType.ipa)) {
            userName = userName.split("@")[0];
            query = "(&(objectClass=posixAccount)(objectClass=krbPrincipalAux)(uid=" + userName + "))";
        } else if (ldapProviderType.equals(LdapProviderType.rhds)) {
            userName = userName.split("@")[0];
            query = "(&(objectClass=person)(uid=" + userName + "))";
        } else if (ldapProviderType.equals(LdapProviderType.itds)) {
            userName = userName.split("@")[0];
            query = "(&(objectClass=person)(uid=" + userName + "))";
        } else if (ldapProviderType.equals(LdapProviderType.openLdap)) {
            userName = userName.split("@")[0];
            query = "(uid=" + userName + ")";
        }
        else {
            StringBuilder queryBase = new StringBuilder("(&(sAMAccountType=805306368)(");
            if (userName.contains("@")) {
                queryBase.append("userPrincipalName=" + userName);
            } else {
                if (userName.length() > 20) {
                    queryBase.append("userPrincipalName=")
                            .append(userName)
                            .append("@")
                            .append(domainName.toUpperCase());
                } else {
                    queryBase.append("sAMAccountName=").append(userName);
                }
            }
            query = queryBase.append("))").toString();
        }
        return query;
    }

    private NamingEnumeration<SearchResult> executeQuery(DirContext ctx, SearchControls controls, String query)
            throws NamingException {
        NamingEnumeration<SearchResult> answer = ctx.search("", query, controls);
        return answer;
    }

    private String getDomainDN(String url) throws NamingException {
        RootDSEData rootDSEData = new RootDSEData(url);
        return rootDSEData.getDomainDN();
    }


    private void logQueryContext(String actualUserGuid, String actualUri, String actualCurrentLdapServer) {
        // Log all information about query used for authentication
        log.debug("User guid is: {}", actualUserGuid);
        log.debug("URI is: {}", actualUri);
        log.debug("Complete query path is: {}", actualCurrentLdapServer);
    }
}
