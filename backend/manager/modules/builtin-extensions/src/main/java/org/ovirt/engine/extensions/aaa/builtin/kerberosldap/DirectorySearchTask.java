package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextMapperCallbackHandler;

public class DirectorySearchTask implements Callable<List<?>> {

    private final LDAPTemplateWrapper ldapTemplate;
    private final long resultcount;
    private final LdapQueryExecution queryExecution;
    private Properties configuration;

    public DirectorySearchTask(Properties configuration, LDAPTemplateWrapper ldapTemplate,
            LdapQueryExecution queryExecution, long resultCount) {
        this.ldapTemplate = ldapTemplate;
        this.queryExecution = queryExecution;
        this.resultcount = resultCount;
        this.configuration = configuration;

    }

    @Override
    public List<?> call() throws Exception {

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
        controls.setTimeLimit(Integer.parseInt(configuration.getProperty("config.LDAPQueryTimeout")) * 1000);

        ldapTemplate.search("",
                queryExecution.getFilter(),
                queryExecution.getDisplayFilter(),
                controls,
                cmCallback);
        return cmCallback.getList();
    }
}
