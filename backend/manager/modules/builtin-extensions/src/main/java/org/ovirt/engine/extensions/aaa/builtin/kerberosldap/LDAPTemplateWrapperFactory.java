package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Factory for getting ldap template wrapper according to the configuration
 *
 *
 */
public class LDAPTemplateWrapperFactory {

    private static HashMap<LDAPSecurityAuthentication, Class<? extends LDAPTemplateWrapper>> classesOfLDAPTemplateWrappers =
            new HashMap<LDAPSecurityAuthentication, Class<? extends LDAPTemplateWrapper>>();
    private static final Logger log = LoggerFactory.getLogger(LDAPTemplateWrapperFactory.class);

    static {
        registerClass(LDAPSecurityAuthentication.GSSAPI, GSSAPILdapTemplateWrapper.class);
        registerClass(LDAPSecurityAuthentication.SIMPLE, SimpleLdapTemplateWrapper.class);

    }

    private static void registerClass(LDAPSecurityAuthentication enumVal,
                                      Class<? extends LDAPTemplateWrapper> matchingClass) {

        classesOfLDAPTemplateWrappers.put(enumVal, matchingClass);

    }

    public static LDAPTemplateWrapper getLDAPTemplateWrapper(Properties configuration, LdapContextSource contextSource, String userName,
                                                             String password, String domain) {

        LDAPSecurityAuthentication ldapSecurityAuthentication =
                LDAPSecurityAuthentication.valueOf(configuration.getProperty("config.LDAPSecurityAuthentication"));
        try {
            Class<? extends LDAPTemplateWrapper> wrapperClass =
                    classesOfLDAPTemplateWrappers.get(ldapSecurityAuthentication);
            Constructor<? extends LDAPTemplateWrapper> constructor = wrapperClass.getConstructor(
                    Properties.class, LdapContextSource.class, String.class, String.class, String.class);
            return constructor.newInstance(configuration, contextSource, userName, password, domain);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException |
                InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error("Failed to get LDAPTemplateWrapper for security authentication {}", ldapSecurityAuthentication);
            return null;
        }
    }
}
