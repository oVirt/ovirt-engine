package org.ovirt.engine.core.bll.adbroker;

public class NoOpLdapFilterSearchEnginePreProcessor implements LdapFilterSearchEnginePreProcessor {

    @Override
    public String preProcess(String filter) {
        return filter;
    }

}
