package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.serverordering.LdapServersOrderingAlgorithmFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class DirectorySearcher {

    private boolean baseDNExist = true;
    private boolean explicitAuth = false;
    private String explicitBaseDN;

    private static final Log log = LogFactory.getLog(DirectorySearcher.class);

    private final LdapCredentials ldapCredentials;
    private Exception ex;
    private static final ExceptionHandler<LdapSearchExceptionHandlingResponse, LdapCredentials> handler =
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

    public Object findOne(LdapQueryData ldapQueryData) {
        List<?> userObjects = find(ldapQueryData, 1);

        if (userObjects == null || userObjects.size() == 0) {
            return null;
        }
        return userObjects.get(0);
    }

    public List<?> findAll(LdapQueryData ldapQueryData) {
        List<?> returnValue = find(ldapQueryData, 0);

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

    public List<?> find(final LdapQueryData queryData, final long resultCount) {

        final String domainName = queryData.getDomain();

        final Domain domain = getDomainObject(domainName);
        if (domain == null) {
            log.errorFormat("Error in finding LDAP servers for domain {0} using user {1}", domainName, ldapCredentials.getUserName());
            return null;
        }

        List<URI> ldapServerURIs = domain.getLdapServers();
        List<URI> editableLdapServerURIs = new ArrayList<>(ldapServerURIs);
        if (log.isDebugEnabled()) {
            log.debug("Ldap server list: " + StringUtils.join(ldapServerURIs, ", "));
        }
        List<?> response = null;

        for (Iterator<URI> iterator = ldapServerURIs.iterator(); iterator.hasNext();) {
            URI ldapURI = iterator.next();
            try {
                response = findAndOrderServers(queryData, ldapURI, domainName, resultCount, editableLdapServerURIs);
                if (response != null) {
                    break;
                }
            } catch (Exception ex) {
                return null;
            }
        }
        domain.setLdapServers(editableLdapServerURIs);
        return response;
    }

    private List<?> findAndOrderServers(LdapQueryData queryData,
            URI ldapURI,
            String domainName,
            long resultCount,
            List<URI> modifiedLdapServersURIs) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Using Ldap server " + ldapURI);
        }
        try {
            setException(null);
            GetRootDSETask getRootDSETask = new GetRootDSETask(this, domainName, ldapURI);
            PrepareLdapConnectionTask prepareLdapConnectionTask =
                    new PrepareLdapConnectionTask(this, ldapCredentials, domainName, ldapURI);
            getRootDSETask.call(); // TODO: Not really async Can throw exception
            LdapQueryExecution queryExecution =
                            LdapQueryExecutionBuilderImpl.getInstance()
                                    .build(getDomainObject(domainName).getLdapProviderType(), queryData);
            if (queryExecution.getBaseDN() != null && !queryExecution.getBaseDN().isEmpty()) {
                setExplicitBaseDN(queryExecution.getBaseDN());
            }

            log.debug("find() : LDAP filter = " + queryExecution.getFilter() +
                      ", baseDN = " + queryExecution.getBaseDN() +
                      ", explicitBaseDN = " + explicitBaseDN + ", domain = " + queryExecution.getDomain() );

            LDAPTemplateWrapper ldapTemplate = prepareLdapConnectionTask.call();
            if (ldapTemplate == null) {
                return Collections.emptyList();
            }
            return new DirectorySearchTask(ldapTemplate, queryExecution, resultCount).call();
        } catch (Exception exception) {
            LdapSearchExceptionHandlingResponse handlingResponse = handler.handle(exception, ldapCredentials);
            Exception translatedException = handlingResponse.getTranslatedException();
            setException(translatedException);
            LdapServersOrderingAlgorithmFactory.getInstance().getOrderingAlgorithm(handlingResponse.getOrderingAlgorithm()).reorder(ldapURI, modifiedLdapServersURIs);
            log.errorFormat("Failed ldap search server {0} using user {1} due to {2}. We {3} try the next server",
                    ldapURI,
                    ldapCredentials.getUserName(),
                    LdapBrokerUtils.getFriendlyExceptionMessage(translatedException),
                    handlingResponse.isTryNextServer() ? "should" : "should not");
            log.debugFormat("Failed ldap search server {0} using user {1} due to {2}. We {3} try the next server",
                    ldapURI,
                    ldapCredentials.getUserName(),
                    translatedException,
                    handlingResponse.isTryNextServer() ? "should" : "should not");
            if (!handlingResponse.isTryNextServer()) {
                throw new Exception();
            }
        }
        return null;
    }

    public void setException(Exception ex) {
        this.ex = ex;
    }

    public Exception getException() {
        return ex;
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
