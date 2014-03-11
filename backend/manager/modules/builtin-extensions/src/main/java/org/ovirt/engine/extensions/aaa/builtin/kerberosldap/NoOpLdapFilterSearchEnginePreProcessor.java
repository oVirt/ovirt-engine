package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class NoOpLdapFilterSearchEnginePreProcessor implements LdapFilterSearchEnginePreProcessor {

    @Override
    public String preProcess(String filter) {
        return filter;
    }

}
