package org.ovirt.engine.core.bll.adbroker;

/**
 * This defines the pre processing operation to be carried out on the filter of an LDAP query issued by the search
 * mechanism
 */
public interface LdapFilterSearchEnginePreProcessor {
    String preProcess(String filter);
}
