package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

/**
 * This defines the pre processing operation to be carried out on the filter of an LDAP query issued by the search
 * mechanism
 */
public interface LdapFilterSearchEnginePreProcessor {
    String preProcess(String filter);
}
