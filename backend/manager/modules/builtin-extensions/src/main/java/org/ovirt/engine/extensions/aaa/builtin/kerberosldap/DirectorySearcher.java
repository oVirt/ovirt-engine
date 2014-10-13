package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering.LdapServersOrderingAlgorithmFactory;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class DirectorySearcher {

    private boolean baseDNExist = true;
    private String explicitBaseDN;
    private Properties configuration;

    private static final Logger log = LoggerFactory.getLogger(DirectorySearcher.class);

    private final LdapCredentials ldapCredentials;
    private Exception ex;
    private static final ExceptionHandler<LdapSearchExceptionHandlingResponse, LdapCredentials> handler =
            new LdapSearchExceptionHandler();


    public DirectorySearcher(Properties configuration, LdapCredentials ldapCredentials) {
        this.configuration = configuration;
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

    protected GetRootDSE createRootDSE(String uri) {
        return new GetRootDSE(configuration, uri);
    }

    public List<?> find(final LdapQueryData queryData, final long resultCount) {
        final String domainName = queryData.getDomain();
        Utils.refreshLdapServers(configuration, domainName);
        List<String> ldapServerURIs =
                Arrays.asList(configuration.getProperty("config.LdapServers").split(";"));
        List<String> editableLdapServerURIs = new ArrayList<>(ldapServerURIs);
        log.debug("Ldap server list: {}", ldapServerURIs);
        List<?> response = null;

        for (Iterator<String> iterator = ldapServerURIs.iterator(); iterator.hasNext();) {
            String ldapURI = iterator.next();
            try {
                response = findAndOrderServers(queryData, ldapURI, domainName, resultCount, editableLdapServerURIs);
                if (response != null) {
                    break;
                }
            } catch (Exception ex) {
                return null;
            }
        }
        configuration.setProperty("config.LdapServers", StringUtils.join(editableLdapServerURIs, ";"));
        return response;
    }

    private List<?> findAndOrderServers(LdapQueryData queryData,
            String ldapURI,
            String domainName,
            long resultCount,
            List<String> modifiedLdapServersURIs) throws Exception {
        log.debug("Using Ldap server {}", ldapURI);
        try {
            setException(null);
            GetRootDSETask getRootDSETask = new GetRootDSETask(configuration, this, domainName, ldapURI);
            PrepareLdapConnectionTask prepareLdapConnectionTask =
                    new PrepareLdapConnectionTask(configuration, this, ldapCredentials, domainName, ldapURI);
            getRootDSETask.call(); // TODO: Not really async Can throw exception
            LdapQueryExecution queryExecution =
                            LdapQueryExecutionBuilderImpl.getInstance()
                            .build(LdapProviderType.valueOf(configuration.getProperty("config.LDAPProviderTypes")),
                                    queryData);
            if (queryExecution.getBaseDN() != null && !queryExecution.getBaseDN().isEmpty()) {
                setExplicitBaseDN(queryExecution.getBaseDN());
            }

            log.debug("find() : LDAP filter='{}', baseDN='{}', explicitBaseDN='{}', domain='{}'",
                    queryExecution.getFilter(),
                    queryExecution.getBaseDN(),
                    explicitBaseDN,
                    queryExecution.getDomain()
            );

            LDAPTemplateWrapper ldapTemplate = prepareLdapConnectionTask.call();
            if (ldapTemplate == null) {
                return Collections.emptyList();
            }
            return new DirectorySearchTask(configuration, ldapTemplate, queryExecution, resultCount).call();
        } catch (Exception exception) {
            LdapSearchExceptionHandlingResponse handlingResponse = handler.handle(exception, ldapCredentials);
            Exception translatedException = handlingResponse.getTranslatedException();
            setException(translatedException);
            LdapServersOrderingAlgorithmFactory.getInstance().getOrderingAlgorithm(handlingResponse.getOrderingAlgorithm()).reorder(ldapURI, modifiedLdapServersURIs);
            log.error("Failed ldap search server {} using user {} due to {}. We {} try the next server",
                    ldapURI,
                    ldapCredentials.getUserName(),
                    LdapBrokerUtils.getFriendlyExceptionMessage(translatedException),
                    handlingResponse.isTryNextServer() ? "should" : "should not");
            log.debug("Failed ldap search server {} using user {} due to {}. We {} try the next server",
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
