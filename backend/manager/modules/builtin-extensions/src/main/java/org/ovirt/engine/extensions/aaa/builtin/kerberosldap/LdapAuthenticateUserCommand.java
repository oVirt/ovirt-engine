package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;


import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class LdapAuthenticateUserCommand extends LdapBrokerCommandBase {

    public LdapAuthenticateUserCommand(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
    }

    protected void initCredentials(String domain) {

    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        log.debug("Executing LdapAuthenticateUserCommand");

        directorySearcher.setExplicitAuth(true);
        LdapUser user = null;
        LdapQueryData queryData = new LdapQueryDataImpl();

        String loginName = getLoginName();
        getParameters().getOutputMap().put(Authn.InvokeKeys.PRINCIPAL, loginName);

        if (getLoginName().contains("@")) { // the user name is UPN use 'User
            // Principal Name' search
            queryData.setLdapQueryType(LdapQueryType.getUserByPrincipalName);
            // The domain in the UPN must overwrite the domain field. Discrepancies between the UPN domain and
            // the domain may lead failure in Kerberos queries
            String[] loginNameParts = getLoginName().split("@");
            String principalName = constructPrincipalName(loginNameParts[0], loginNameParts[1]);
            String domain = loginNameParts[1].toLowerCase();
            queryData.setFilterParameters(new Object[] { principalName });
            queryData.setDomain(domain);
            setDomain(domain);
            setAuthenticationDomain(domain);
        } else {
            // the user name is NT format use 'SAM Account Name' search
            setAuthenticationDomain(getDomain());
            queryData.setDomain(getDomain());
            queryData.setLdapQueryType(LdapQueryType.getUserByName);
            queryData.setFilterParameters(new Object[] { getLoginName() });
        }
        Object searchResult = directorySearcher.findOne(queryData);


        if (searchResult == null) {
            log.errorFormat("Failed authenticating user: {0} to domain {1}. Ldap Query Type is {2}",
                    getLoginName(),
                    getAuthenticationDomain(),
                    queryData.getLdapQueryType().name());
            setSucceeded(false);
            Exception ex = directorySearcher.getException();
            handleDirectorySearcherException(ex);
        } else {
            user = populateUserData((LdapUser) searchResult, getAuthenticationDomain());
            if (user != null) {
                getParameters().getOutputMap().mput(
                        Authn.InvokeKeys.RESULT,
                        Authn.AuthResult.SUCCESS
                        ).mput(
                                Authn.InvokeKeys.AUTH_RECORD,
                                new ExtMap().mput(
                                        Authn.AuthRecord.PRINCIPAL,
                                        loginName
                                        ));
                setSucceeded(true);
            } else {
                log.errorFormat("Failed authenticating. Domain is {0}. User is {1}. The user doesn't have a UPN",
                        getAuthenticationDomain(),
                        getLoginName());
                getParameters().getOutputMap().put(Authn.InvokeKeys.RESULT, Authn.AuthResult.CREDENTIALS_INCORRECT);
            }
        }

        if (!getSucceeded()) {
            getParameters().getOutputMap().put(Authn.InvokeKeys.RESULT, Authn.AuthResult.GENERAL_ERROR);
        }
    }

    private void handleDirectorySearcherException(Exception ex) {
        if (ex instanceof AuthenticationResultException) {
            AuthenticationResultException authResultException = (AuthenticationResultException) ex;
            AuthenticationResult result = authResultException.getResult();
            if (result == null) {
                result = AuthenticationResult.OTHER;
            }
            log.error(result.getDetailedMessage());
            getParameters().getOutputMap().put(Authn.InvokeKeys.RESULT, resultsMap.get(result));
        }
    }

    @Override
    protected void handleRootDSEFailure(DirectorySearcher directorySearcher) {
        Exception ex = directorySearcher.getException();
        handleDirectorySearcherException(ex);
    }

    private String constructPrincipalName(String username, String domain) {
        return username + '@' + domain.toUpperCase();
    }

    private static final Log log = LogFactory.getLog(LdapAuthenticateUserCommand.class);
}
