package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.BrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapUserPasswordBaseParameters;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

@NonTransactiveCommandAttribute
public class LoginUserCommand<T extends LoginUserParameters> extends LoginBaseCommand<T> {
    public LoginUserCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected UserAuthenticationResult authenticateUser() {
        // We are using the getLoginDomain method in order to get the real domain, in case of logging in with a UPN
        // as in that case the domain we get is what chosen by the client, but the real domain is the one determined by
        // the UPN
        String loginDomain = BrokerUtils.getLoginDomain(getParameters().getUserName(), getDomain());
        LdapBroker adFactory =
                LdapFactory.getInstance(loginDomain);
        if (adFactory == null) {
            log.error("No LdapBrokerImpl can be retrieved.");
            return new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATION_WRONG_AUTHENTICATION_METHOD);
        }
        LdapReturnValueBase adReturnValue = adFactory.RunAdAction(AdActionType.AuthenticateUser,
                new LdapUserPasswordBaseParameters(loginDomain, getParameters().getUserName(), getUserPassword()));
        UserAuthenticationResult authResult = (UserAuthenticationResult) adReturnValue.getReturnValue();
        return authResult;
    }
}
