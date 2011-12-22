package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class DirectorySearcher {

    private static final Object DC_PREFIX = "DC=";
    private boolean baseDNExist = true;
    private boolean explicitAuth = false;
    private String explicitBaseDN;

    private static final LogCompat log = LogFactoryCompat.getLog(DirectorySearcher.class);

    private final LdapCredentials ldapCredentials;
    private Exception ex;
    private static final ExceptionHandler<LdapSearchExceptionHandlingResponse> handler =
            new LdapSearchExceptionHandler();

    public void setExplicitAuth(boolean explicitAuth) {
        this.explicitAuth = explicitAuth;
    }

    public boolean getExplicitAuth() {
        return explicitAuth;
    }

    public DirectorySearcher(LdapCredentials ldapCredentials) {
        this.ldapCredentials = ldapCredentials;
    }

    public Object FindOne(LdapQueryData ldapQueryData) {
        List<Object> userObjects = find(ldapQueryData, 1);

        if (userObjects == null || userObjects.size() == 0) {
            return null;
        }
        return userObjects.get(0);
    }

    public List FindAll(LdapQueryData ldapQueryData) {
        List returnValue = find(ldapQueryData, 0);

        if (returnValue == null) {
            returnValue = Collections.EMPTY_LIST;
        }
        return returnValue;
    }

    protected GetRootDSE createRootDSE(URI uri) {
        return new GetRootDSE(uri);
    }

    protected Domain getDomainObject(String domainName) {
        Domain domainObject = UsersDomainsCacheManagerService.getInstance().getDomain(domainName);
        return domainObject;
    }

    public List find(final LdapQueryData queryData, final long resultCount) {

        final String domainName = queryData.getDomain();

        final Domain domain = getDomainObject(domainName);
        if (domain == null) {
            log.errorFormat("Error in finding LDAP servers for domain {0}", domainName);
            return null;
        }

        List<URI> ldapServerURIs = domain.getLdapServers();
        if (log.isDebugEnabled()) {
            log.debug("Ldap server list ordered by highest score: " + StringUtils.join(ldapServerURIs, ", "));
        }
        List response = null;

        for (Iterator<URI> iterator = ldapServerURIs.iterator(); iterator.hasNext();) {
            URI ldapURI = iterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Using Ldap server " + ldapURI);
            }
            setException(null);
            FutureTask<List> searchTask = newSearchTask(queryData, resultCount, domainName, ldapURI);

            try {
                ThreadPoolUtil.execute(searchTask);
                response = searchTask.get(Config.<Integer> GetValue(ConfigValues.LDAPQueryTimeout), TimeUnit.SECONDS);
                domain.scoreLdapServer(ldapURI, Score.HIGH);
                return response; // No point in continuing to next LDAP server if we have success.
            } catch (Exception exception) {
                if (searchTask != null) {
                    searchTask.cancel(true);
                }
                LdapSearchExceptionHandlingResponse handlingResponse = handler.handle(exception);
                setException(handlingResponse.getTranslatedException());
                domain.scoreLdapServer(ldapURI, handlingResponse.getServerScore());
                log.errorFormat("Failed ldap search server {0} due to {1}. We {2} try the next server",
                        ldapURI,
                        handlingResponse.getTranslatedException(),
                        handlingResponse.isTryNextServer() ? "should" : "should not");
                if (!handlingResponse.isTryNextServer()) {
                    return Collections.emptyList();
                }
            }
        }
        return response;
    }

    private FutureTask<List> newSearchTask(final LdapQueryData queryData,
            final long resultCount,
            final String domainName,
            URI ldapURI) {
        final GetRootDSETask getRootDSETask = new GetRootDSETask(this, domainName, ldapURI);
        final PrepareLdapConnectionTask prepareLdapConnectionTask = new PrepareLdapConnectionTask(this, ldapCredentials, domainName, ldapURI);

        FutureTask<List> searchTask = new FutureTask<List>(new Callable<List>() {

            @Override
            public List call() throws Exception {
                getRootDSETask.call();
                final LdapQueryExecution queryExecution =
                        LdapQueryExecutionBuilderImpl.getInstance()
                                .build(getDomainObject(domainName).getLdapProviderType(), queryData);
                if (queryExecution.getBaseDN() != null && !queryExecution.getBaseDN().isEmpty()) {
                    setExplicitBaseDN(queryExecution.getBaseDN());
                }
                LDAPTemplateWrapper ldapTemplate = prepareLdapConnectionTask.call();
                if (ldapTemplate == null) {
                    return Collections.emptyList();
                }
                return new DirectorySearchTask(ldapTemplate, queryExecution, resultCount).call();
            }
        });
        return searchTask;
    }

    public void setException(Exception ex) {
        this.ex = ex;
    }

    public Exception getException() {
        return ex;
    }

    protected String getBaseDNForDomainForSimpleAuth(String domainName) {
        if (domainName == null) {
            return null;
        }

        StringBuilder dnSb = new StringBuilder();
        String[] parts = domainName.split("\\.");
        // format should be dc=part0,dc=part1,dc=part2,.....dc=part-(n-1)

        for (int counter = 0; counter < parts.length; counter++) {
            dnSb.append(DC_PREFIX).append(parts[counter]);
            if (counter < parts.length - 1) {
                dnSb.append(",");
            }
        }
        return dnSb.toString();

    }

    public void setExplicitBaseDN(String explicitBaseDN) {
        this.explicitBaseDN = explicitBaseDN;
    }

    public String getExplicitBaseDN() {
        return explicitBaseDN;
    }

    public boolean isBaseDNExist() {
        return baseDNExist;
    }

    public void setBaseDNExist(boolean baseDNExist) {
        this.baseDNExist = baseDNExist;
    }
}
