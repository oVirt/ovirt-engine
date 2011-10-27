package org.ovirt.engine.core.bll.adbroker;

public class LdapQueryInfo {

    private String baseDN;
    private String filter;

    public LdapQueryInfo(String baseDN,String filter) {
        this.baseDN = baseDN;
        this.filter = filter;
    }

    public LdapQueryInfo() {
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}
