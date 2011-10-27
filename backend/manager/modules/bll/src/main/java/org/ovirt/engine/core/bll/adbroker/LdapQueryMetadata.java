package org.ovirt.engine.core.bll.adbroker;

import org.springframework.ldap.core.ContextMapper;

public interface LdapQueryMetadata {

    public String getFilter();

    public void setFilter(String filter);

    public String getBaseDN();

    public void setBaseDN(String baseDN);

    public ContextMapper getContextMapper();

    public void setContextMapper(ContextMapper contextMapper);

    public int getSearchScope();

    public void setSearchScope(int searchScope);

    public String[] getReturningAttributes();

    public void setReturningAttributes(String[] returningAttributes);

    public LdapGuidEncoder getLdapGuidEncoder();

    public void setLdapGuidEncoder(LdapGuidEncoder ldapGuidEncoder);

    public LdapQueryFormatter<LdapQueryExecution> getFormatter();

    public LdapQueryData getQueryData();

    public void setQueryData(LdapQueryData queryData);

}
