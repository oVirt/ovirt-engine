package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.DependsOn;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.DbUserCacheManager;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.dbbroker.generic.DomainsPasswordMap;
import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.ldap.LdapSRVLocator;
import org.ovirt.engine.core.utils.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.core.utils.kerberos.KerberosUtils;


// Here we use a Singleton bean
// The @Startup annotation is to make sure the bean is initialized on startup.
// @ConcurrencyManagement - we use bean managed concurrency:
// Singletons that use bean-managed concurrency allow full concurrent access to all the
// business and timeout methods in the singleton.
// The developer of the singleton is responsible for ensuring that the state of the singleton is synchronized across all clients.
// The @DependsOn annotation is in order to make sure it is started after the stated beans are initialized
@Singleton
@Startup
@DependsOn({"Backend","Scheduler","KerberosManager"})
@Local(UsersDomainsCacheManager.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class UsersDomainsCacheManagerService implements UsersDomainsCacheManager {

    private static Log log = LogFactory.getLog(UsersDomainsCacheManagerService.class);
    private Map<String, Domain> domainsByName = new HashMap<String, Domain>();
    private Map<String, ConcurrentHashMap<String, UserDomainInfo>> domainsUsersInfoByUserNameAndDomainName =
            new HashMap<String, ConcurrentHashMap<String, UserDomainInfo>>();
    private Map<String, ConcurrentHashMap<String, LdapGroup>> groupsPerDomain =
            new HashMap<String, ConcurrentHashMap<String, LdapGroup>>();
    private Map<String, URI> ldapServerPerDomain = new HashMap<String, URI>();
    private final String DEFAULT_SECURITY_AUTHENTICATION_KEY = "default";
    private Map<String, LDAPSecurityAuthentication> ldapSecurityAuthenticationPerDomain =
            new HashMap<String, LDAPSecurityAuthentication>();
    private Map<String, String> userPerDomain = new HashMap<String, String>();
    private Map<String, String> passwordPerDomain = new HashMap<String, String>();

    @Override
    public void addDomain(Domain domain) {
        domainsByName.put(domain.getName(), domain);
    }

    private void fillLdapServersMap() {
        String ldapServerPerDomainEntry = Config.<String> GetValue(ConfigValues.LdapServers);
        if (!ldapServerPerDomainEntry.isEmpty()) {
            String[] domainServerPairs = ldapServerPerDomainEntry.split(",");
            int ldapPort = Config.<Integer> GetValue(ConfigValues.LDAPServerPort);

            for (String domainServerPair : domainServerPairs) {
                String[] parts = domainServerPair.split(":");
                String domain = parts[0].trim().toLowerCase();
                URI ldapURI;

                try {
                    ldapURI = new URI("ldap://" + parts[1].trim() + ":" + ldapPort);
                    ldapServerPerDomain.put(domain, ldapURI);
                } catch (URISyntaxException e) {
                    log.errorFormat("Failed constructing LDAP server URL for domain {0}", domain);
                }
            }
        }
    }

    // This code is the exact code as in SysprepHandler, until we have a suitable location that them both can
    // use
    // Note that every change in one will probably require the same change in the other
    private void fillUsersMap() {
        String userPerDomainEntry = Config.<String> GetValue(ConfigValues.AdUserName);
        if (!userPerDomainEntry.isEmpty()) {
            String[] domainUserPairs = userPerDomainEntry.split(",");

            for (String domainUserPair : domainUserPairs) {
                String[] parts = domainUserPair.split(":");
                String domain = parts[0].trim().toLowerCase();
                String userName = parts[1].trim();

                userPerDomain.put(domain, userName);
            }
        }
    }

    private void fillPasswordsMap() {
        passwordPerDomain = Config.<DomainsPasswordMap> GetValue(ConfigValues.AdUserPassword);
    }

    private void fillLdapSecurityAuthenticationMap() {

        String ldapSecurityAuthEntry = Config.<String> GetValue(ConfigValues.LDAPSecurityAuthentication);
        if (!ldapSecurityAuthEntry.isEmpty()) {
            String[] ldapSecurityPairs = ldapSecurityAuthEntry.split(",");

            for (String ldapSecurityPair : ldapSecurityPairs) {
                String[] parts = ldapSecurityPair.split(":");
                String domain = parts[0].trim().toLowerCase();
                String authModeStr = parts[1].trim().toUpperCase();
                LDAPSecurityAuthentication authMode =
                        EnumUtils.valueOf(LDAPSecurityAuthentication.class, authModeStr, true);

                ldapSecurityAuthenticationPerDomain.put(domain, authMode);
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        create();
    }

    /**
     * This method is called upon the bean creation as part
     * of the management Service bean lifecycle.
     */
    public void create() {

        log.info("Start initializing " + getClass().getSimpleName());
        String authMethod = Config.<String> GetValue(ConfigValues.AuthenticationMethod);
        if (!authMethod.equalsIgnoreCase("LDAP")) {
            return;
        }
        List<String> domains = LdapBrokerUtils.getDomainsList(true);
        Map<String, LdapProviderType> domainLDAPProviders = parseLDAPProviders();
        fillLdapServersMap();
        fillLdapSecurityAuthenticationMap();
        fillUsersMap();
        fillPasswordsMap();

        for (String domainName : domains) {
            domainName = domainName.toLowerCase();
            domainsUsersInfoByUserNameAndDomainName.put(domainName, new ConcurrentHashMap<String, UserDomainInfo>());
            Domain domain = new Domain(domainName);
            domain.setLdapProviderType(domainLDAPProviders.get(domainName) == null ? LdapProviderType.general
                    : domainLDAPProviders.get(domainName));
            domain.setLdapSecurityAuthentication(getDomainSecurityAuthentication(domainName));
            domainsByName.put(domainName, domain);
            domain.setUserName(userPerDomain.get(domainName));
            domain.setPassword(passwordPerDomain.get(domainName));
            // Each domain has LDAP servers that one of them should be used to
            // perform an LDAP query against the domain
            obtainLDAPServersForDomain(domain);
            groupsPerDomain.put(domain.getName(), new ConcurrentHashMap<String, LdapGroup>());
        }

        DbUserCacheManager.getInstance().init();
        log.info("Finished initializing " + getClass().getSimpleName());

    }

    /**
     * map domain -> LdapProviderType according to ConfigValue LDAPProviderTypes.<br>
     * expected config value form is "example.com:ActiveDirectory, example.net:RHDS" and so on. null values are ignored
     * e.g. "example.com: "
     * @return map of domainName -> LdapProviderType
     * @see {@link #ConfigValues.LDAPProviderTypes} and {@link LdapProviderType}
     */
    protected Map<String, LdapProviderType> parseLDAPProviders() {
        Map<String, LdapProviderType> domainProviderTypes = new HashMap<String, LdapProviderType>();
        for (String pair : StringUtils.split(Config.<String> GetValue(ConfigValues.LDAPProviderTypes), ",")) {
            if (pair != null) {
                String[] split = StringUtils.split(pair, ":");
                if (split != null && split[0] != null && split[1] != null) {
                    domainProviderTypes.put(split[0], LdapProviderType.valueOf(LdapProviderType.class, split[1]));
                }
            }
        }
        return domainProviderTypes;
    }

    private LDAPSecurityAuthentication getDomainSecurityAuthentication(String domainName) {

        LDAPSecurityAuthentication securityAuthentication = ldapSecurityAuthenticationPerDomain.get(domainName);

        if (securityAuthentication != null) {
            return securityAuthentication;
        } else {
            securityAuthentication = ldapSecurityAuthenticationPerDomain.get(DEFAULT_SECURITY_AUTHENTICATION_KEY);
            if (securityAuthentication != null) {
                return securityAuthentication;
            } else {
                return LDAPSecurityAuthentication.GSSAPI;
            }
        }
    }

    private void obtainLDAPServersForDomain(Domain domain) {

        URI ldapServerURI = ldapServerPerDomain.get(domain.getName());

        if (ldapServerURI != null) {
            domain.addLDAPServer(ldapServerURI);
            return;
        }

        LdapSRVLocator locator = new LdapSRVLocator();
        DnsSRVResult results;
        try {
            results = locator.getLdapServers(domain.getName());
            if (results == null || results.getNumOfValidAddresses() == 0) {
                log.warnFormat("Error in getting LDAP servers for domain {0}. Constructing an LDAP URL based on domain name",
                        domain.getName());
                constructLDAPUrlOnDNSFailure(domain);
                return;
            }
            for (int counter = 0; counter < results.getNumOfValidAddresses(); counter++) {
                String address = results.getAddresses()[counter];
                try {
                    URI ldapURI = locator.constructURI("LDAP", address);
                    domain.addLDAPServer(ldapURI);
                } catch (URISyntaxException e) {
                    log.errorFormat("Error in getting LDAP url based on srv record for address {0}", address);
                }
            }
        } catch (Exception ex) {

            AuthenticationResult result = KerberosUtils.convertDNSException(ex);
            log.warnFormat("Error in getting LDAP servers for domain {0}: {1}. Constructing an LDAP URL based on domain name",
                           domain.getName(),
                    result.getDetailedMessage());
            constructLDAPUrlOnDNSFailure(domain);
        }

    }

    private void constructLDAPUrlOnDNSFailure(Domain domain) {
        int ldapPort = Config.<Integer> GetValue(ConfigValues.LDAPServerPort);
        StringBuilder ldapURL = new StringBuilder();
        ldapURL.append("ldap://").append(domain.getName()).append(":").append(ldapPort);
        try {
            URI uri = new URI(ldapURL.toString());
            domain.addLDAPServer(uri);

        } catch (URISyntaxException e) {
            log.error("Failed constructing LDAP server URL for domain ");
        }
    }

    public static UsersDomainsCacheManager getInstance() {
        return EjbUtils.<UsersDomainsCacheManager> findBean(BeanType.USERS_DOMAINS_CACHE, BeanProxyType.LOCAL);
    }

    @Override
    public UserDomainInfo associateUserWithDomain(String userName, String domainName) {
        domainName = domainName.toLowerCase();
        UserDomainInfo userDomainInfo = new UserDomainInfo(userName, domainName);

        ConcurrentHashMap<String, UserDomainInfo> usersDomainInfoByDomainName =
                domainsUsersInfoByUserNameAndDomainName.get(domainName);

        // FOr the given user name, put the user domain info in the map only if
        // absent - this is atomic, and prevents to threads from putting several
        // entries for same user
        usersDomainInfoByDomainName.putIfAbsent(userName, userDomainInfo);
        return usersDomainInfoByDomainName.get(userName);
    }

    @Override
    public UserDomainInfo getUserDomainInfo(String userName, String domainName) {
        domainName = domainName.toLowerCase();
        ConcurrentHashMap<String, UserDomainInfo> usersDomainInfoByDomainName =
                domainsUsersInfoByUserNameAndDomainName.get(domainName);
        return usersDomainInfoByDomainName.get(userName);
    }

    @Override
    public Domain getDomain(String domainName) {
        domainName = domainName.toLowerCase();
        return domainsByName.get(domainName);
    }

    @Override
    public void removeUserDomainInfo(String userName, String domainName) {
        domainName = domainName.toLowerCase();
        ConcurrentHashMap<String, UserDomainInfo> usersDomainInfoByDomainName =
                domainsUsersInfoByUserNameAndDomainName.get(domainName);
        usersDomainInfoByDomainName.remove(userName);

    }

    @Override
    public void removeDomain(String domainName) {
        domainName = domainName.toLowerCase();
        domainsByName.remove(domainName);
    }
}
