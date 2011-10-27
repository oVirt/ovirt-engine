package org.ovirt.engine.core.itests.ldap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ldap.core.support.LdapContextSource;

public class ContextSourceFactory {

    public static LdapContextSource getIPAContextSource(Map<String, String> ldapConfiguration) {
        LdapContextSource context = new LdapContextSource();

        String ipaLdapServer = ldapConfiguration.get("TEST_IPA_LDAP_SERVER");
        String ipaLdapBaseDn = ldapConfiguration.get("TEST_IPA_LDAP_BASE_DN");
        String ipaLdapUserDn = ldapConfiguration.get("TEST_IPA_LDAP_USER_DN");
        String ipaLdapPassword = ldapConfiguration.get("TEST_IPA_LDAP_PASSWORD");

        context.setUrl("ldap://" + ipaLdapServer + ":389");
        context.setBase(ipaLdapBaseDn);
        context.setUserDn(ipaLdapUserDn);
        context.setPassword(ipaLdapPassword);
        return context;
    }

    public static LdapContextSource getADContextSource(Map<String, String> ldapConfiguration) {
        LdapContextSource context = new LdapContextSource();

        String adLdapServer = ldapConfiguration.get("TEST_AD_LDAP_SERVER");
        String adLdapBaseDn = ldapConfiguration.get("TEST_AD_LDAP_BASE_DN");
        String adLdapUserDn = ldapConfiguration.get("TEST_AD_LDAP_USER_DN");
        String adLdapPassword = ldapConfiguration.get("TEST_AD_LDAP_PASSWORD");

        context.setUrl("ldap://" + adLdapServer + ":389");
        context.setBase(adLdapBaseDn);
        context.setUserDn(adLdapUserDn);
        context.setPassword(adLdapPassword);
        context.setReferral("follow");
        Map<String, String> baseEnvironmentProperties = new HashMap<String, String>();
        // objectGUID
        baseEnvironmentProperties.put("java.naming.ldap.attributes.binary", "objectGUID");
        context.setBaseEnvironmentProperties(baseEnvironmentProperties);
        return context;
    }

}
