package org.ovirt.engine.core.bll.adbroker;

import java.util.List;
import java.util.concurrent.Callable;

import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextMapperCallbackHandler;

public class DirectorySearchTask implements Callable<List> {

    private final LDAPTemplateWrapper ldapTemplate;
    private final long resultcount;
    private final LdapQueryExecution queryExecution;

    public DirectorySearchTask(LDAPTemplateWrapper ldapTemplate,
            LdapQueryExecution queryExecution, long resultCount) {
        this.ldapTemplate = ldapTemplate;
        this.queryExecution = queryExecution;
        this.resultcount = resultCount;

    }

    @Override
    public List call() throws Exception {

        ContextMapperCallbackHandler cmCallback =
                new NotNullContextMapperCallbackHandler(queryExecution.getContextMapper());
        SearchControls controls = new SearchControls();

        if (queryExecution.getReturningAttributes() != null) {
            controls.setReturningAttributes(queryExecution.getReturningAttributes());
        }
        controls.setSearchScope(queryExecution.getSearchScope());
        controls.setCountLimit(resultcount);
        // Added this in order to prevent a warning saying: "the returning obj flag wasn't set, setting it to true"
        controls.setReturningObjFlag(true);

        ldapTemplate.search("",
                queryExecution.getFilter(),
                queryExecution.getDisplayFilter(),
                controls,
                cmCallback);
        return cmCallback.getList();
    }
}
