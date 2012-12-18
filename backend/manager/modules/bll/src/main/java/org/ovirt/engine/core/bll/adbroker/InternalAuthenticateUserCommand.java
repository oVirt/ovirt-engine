package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapUser;


public class InternalAuthenticateUserCommand extends InternalBrokerCommandBase {
    public InternalAuthenticateUserCommand(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
    }

    public String getUPNForUser(String userName, String domain) {
        String UPN = userName;
        if (!userName.contains("@")) {
            UPN = userName + '@' + domain;
        }
        return UPN;
    }

    public String getUserNameForUPN(String UPN) {
        String userName = UPN;
        if (userName.contains("@")) {
            userName = userName.split("@")[0];
        }
        return userName;
    }

    @Override
    protected void ExecuteQuery() {
        String userName = getParameters().getLoginName();
        String password = getParameters().getPassword();
        String domain = BrokerUtils.getLoginDomain(userName, getDomain());
        String userUPN = getUPNForUser(userName, domain);
        userName = getUserNameForUPN(userUPN);
        UserAuthenticationResult result = InternalBrokerUtils.authenticate(userName, password, domain);

        setSucceeded(result.isSuccessful());

        if (result.isSuccessful()) {
            LdapUser user = InternalBrokerUtils.getUserByUPN(userUPN);
            UserAuthenticationResult authResult = new UserAuthenticationResult(user);
            setReturnValue(authResult);
        } else {
            setReturnValue(result);
        }
    }

}
