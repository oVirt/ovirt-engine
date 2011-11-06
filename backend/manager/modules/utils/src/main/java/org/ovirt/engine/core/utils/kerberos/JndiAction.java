package org.ovirt.engine.core.utils.kerberos;

import java.net.URI;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.ldap.LdapSRVLocator;
import org.ovirt.engine.core.ldap.RootDSEData;
import org.ovirt.engine.core.utils.ipa.RHDSUserContextMapper;

/**
 * JAAS Privileged action to be run when KerbersUtil successfully authenticates. This action performs ldap query to
 * retrieve information on the authenticated user and prints the object GUID of that user.
 */
public class JndiAction implements PrivilegedAction {

    private String userName;
    private String domainName;
    private LdapProviderType ldapProviderType = LdapProviderType.activeDirectory;
    private StringBuffer userGuid;
    private final static Logger log = Logger.getLogger(JndiAction.class);

    public JndiAction(String userName, String domainName, StringBuffer userGuid) {
        this.userName = userName;
        this.domainName = domainName;
        this.ldapProviderType = LdapProviderType.activeDirectory;
        this.userGuid = userGuid;
    }

    @Override
    public Object run() {
        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "objectGUID");
        env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");

        // Send an SRV record DNS query to retrieve all the LDAP servers in the domain
        LdapSRVLocator locator = new LdapSRVLocator();
        DnsSRVResult ldapDnsResult;
        try {
            ldapDnsResult = locator.getLdapServers(domainName);
        } catch (Exception ex) {
            return KerberosUtils.convertDNSException(ex);
        }

        DirContext ctx = null;

        String currentLdapServer = null;

        if (ldapDnsResult == null || ldapDnsResult.getNumOfValidAddresses() == 0) {
            return AuthenticationResult.CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN;
        }

        // Goes over all the retrieved LDAP servers
        for (int counter = 0; counter < ldapDnsResult.getNumOfValidAddresses(); counter++) {
            String address = ldapDnsResult.getAddresses()[counter];
            try {
                // Constructs an LDAP url in a format of ldap://hostname:port (based on the data in the SRV record
                // This URL is not enough in order to query for user - as for querying users, we should also provide a
                // base dn, for example: ldap://hostname:389/DC=abc,DC=com . However, this URL (ldap:hostname:port)
                // suffices for
                // getting the rootDSE information, which includes the baseDN.
                URI uri = locator.constructURI("LDAP", address);
                env.put(Context.PROVIDER_URL, uri.toString());
                ctx = new InitialDirContext(env);

                // Get the base DN from rootDSE
                String domainDN = getDomainDN(ctx);
                if (domainDN != null) {

                    // Append the base DN to the ldap URL in order to construct a full ldap URL (in form of
                    // ldap:hostname:port/baseDN ) to query for the user
                    StringBuilder ldapQueryPath = new StringBuilder(uri.toString());
                    ldapQueryPath.append("/").append(domainDN);
                    SearchControls controls = new SearchControls();
                    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    // Adding all the three attributes possible, as RHDS doesn't return the nsUniqueId by default
                    controls.setReturningAttributes(new String[]{"nsUniqueId", "ipaUniqueId","objectGuid"});
                    // Added this in order to prevent a warning saying: "the returning obj flag wasn't set, setting it to true"
                    controls.setReturningObjFlag(true);
                    currentLdapServer = ldapQueryPath.toString();
                    env.put(Context.PROVIDER_URL, currentLdapServer);

                    // Run the LDAP query to get the user
                    ctx = new InitialDirContext(env);
                    NamingEnumeration<SearchResult> answer = executeQuery(ctx, controls, prepareQuery());

                    while (answer.hasMoreElements()) {
                        // Print the objectGUID for the user
                        userGuid.append(guidFromResults(answer.next()));
                        log.debug("User guid is: " + userGuid.toString());
                        return AuthenticationResult.OK;
                    }

                    System.out.println("No user in Directory was found for " + userName
                            + ". Trying next LDAP server in list");
                } else {
                    System.out.println(InstallerConstants.ERROR_PREFIX
                            + " Failed to query rootDSE in order to get the baseDN. Could not query for user "
                            + userName + " in domain" + domainName);
                }
            } catch (CommunicationException ex) {
                System.out.println("Cannot connect to LDAP URL: " + currentLdapServer
                        + ". Trying next LDAP server in list (if exists)");
            } catch (AuthenticationException ex) {
                ex.printStackTrace();
                AuthenticationResult result = AuthenticationResult.OTHER;
                KerberosReturnCodeParser parser = new KerberosReturnCodeParser();
                result = parser.parse(ex.toString());
                String errorMsg = result.getDetailedMessage().replace("Authentication Failed", "LDAP query Failed");
                System.out.println(InstallerConstants.ERROR_PREFIX + errorMsg);
            } catch (Exception ex) {
                System.out.println("General error has occured" + ex.getMessage());
                ex.printStackTrace();
                break;
            } finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (NamingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        } // end of loop on addresses

        return AuthenticationResult.NO_USER_INFORMATION_WAS_FOUND_FOR_USER;

    }

    private String guidFromResults(SearchResult sr) throws NamingException {
        String guidString = "";

        if (ldapProviderType.equals(LdapProviderType.ipa)) {
            String ipaUniqueId = (String) sr.getAttributes().get("ipaUniqueId").get();
            guidString += ipaUniqueId;
        } else if (ldapProviderType.equals(LdapProviderType.rhds)) {
            String nsUniqueId = (String) sr.getAttributes().get("nsUniqueId").get();
            guidString += RHDSUserContextMapper.getGuidFromNsUniqueId(nsUniqueId);
        } else {
            Object objectGuid = sr.getAttributes().get("objectGUID").get();
            byte[] guid = (byte[]) objectGuid;
            guidString += ((new org.ovirt.engine.core.compat.Guid(guid, false)).toString());
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

    private String getDomainDN(DirContext ctx) throws NamingException {
        RootDSEData rootDSEData = new RootDSEData(ctx);
        ldapProviderType = rootDSEData.getLdapProviderType();
        return rootDSEData.getDomainDN();
    }

}
