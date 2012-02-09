package org.ovirt.engine.core.bll.adbroker;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Factory for getting ldap template wrapper according to the configuration
 *
 *
 */
public class LDAPTemplateWrapperFactory {

    private static HashMap<LDAPSecurityAuthentication, Class<? extends LDAPTemplateWrapper>> classesOfLDAPTemplateWrappers =
            new HashMap<LDAPSecurityAuthentication, Class<? extends LDAPTemplateWrapper>>();
    private static LogCompat log = LogFactoryCompat.getLog(LDAPTemplateWrapperFactory.class);

    static {
        registerClass(LDAPSecurityAuthentication.GSSAPI, GSSAPILdapTemplateWrapper.class);
        registerClass(LDAPSecurityAuthentication.SIMPLE, SimpleLdapTemplateWrapper.class);

    }

    private static void registerClass(LDAPSecurityAuthentication enumVal,
                                      Class<? extends LDAPTemplateWrapper> matchingClass) {

        classesOfLDAPTemplateWrappers.put(enumVal, matchingClass);

    }

    public static LDAPTemplateWrapper getLDAPTemplateWrapper(LdapContextSource contextSource, String userName,
                                                             String password, String domain) {

        Domain requestedDomain = UsersDomainsCacheManagerService.getInstance().getDomain(domain);

        if (requestedDomain == null) {
            throw new DomainNotConfiguredException(domain);
        }
        LDAPSecurityAuthentication ldapSecurityAuthentication =
                UsersDomainsCacheManagerService.getInstance().getDomain(domain).getLdapSecurityAuthentication();
        try {
            Class<? extends LDAPTemplateWrapper> wrapperClass =
                    classesOfLDAPTemplateWrappers.get(ldapSecurityAuthentication);
            Constructor<? extends LDAPTemplateWrapper> constructor = wrapperClass.getConstructor(
                    LdapContextSource.class, String.class, String.class, String.class);
            return constructor.newInstance(contextSource, userName, password, domain);
        } catch (Exception ex) {
            log.error("Failed to get LDAPTemplateWrapper for security authentication "
                    + ldapSecurityAuthentication.toString());
            return null;
        }
    }
}
