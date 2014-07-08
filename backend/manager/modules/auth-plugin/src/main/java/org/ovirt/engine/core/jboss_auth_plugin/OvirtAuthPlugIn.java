package org.ovirt.engine.core.jboss_auth_plugin;

import java.io.IOException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.domain.management.plugin.AbstractPlugIn;
import org.jboss.as.domain.management.plugin.Identity;
import org.jboss.as.domain.management.plugin.ValidatePasswordCredential;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class OvirtAuthPlugIn extends AbstractPlugIn {

    public void init(Map<String, String> configuration, Map<String, Object> sharedState) throws IOException {
        this.configuration = configuration;
        // This will allow an AuthorizationPlugIn to delegate back to this instance.
        sharedState.put(OvirtAuthPlugIn.class.getName(), this);
    }

    @Override
    public Identity loadIdentity(final String username, String realm) throws IOException {
        return new Identity<ValidatePasswordCredential>() {
            @Override
            public String getUserName() {
                return username;
            }

            @Override
            public ValidatePasswordCredential getCredential() {
                return new ValidatePasswordCredential() {
                    @Override
                    public boolean validatePassword(char[] chars) {
                        BackendLocal backend;
                        try {
                            backend = (BackendLocal) new InitialContext().lookup(
                                            "java:global/engine/bll/Backend!" + BackendLocal.class.getName());
                        } catch (NamingException e) {
                            throw new RuntimeException("Can't communicate with the backend API");
                        }
                        LoginUserParameters loginParams =
                                new LoginUserParameters(
                                        username.substring(username.lastIndexOf('@') + 1),
                                        username.substring(0, username.lastIndexOf('@')),
                                        new String(chars));
                        loginParams.setActionType(VdcActionType.LoginAdminUser);
                        VdcLoginReturnValueBase login = (VdcLoginReturnValueBase) backend.login(loginParams);
                        return login.getSucceeded()
                                && backend.runQuery(
                                        VdcQueryType.isUserApplicationContainerManager,
                                        new VdcQueryParametersBase(login.getSessionId())
                                        ).getSucceeded();
                    }
                };
            }
        };
    }
}
