package org.ovirt.engine.core.jboss_auth_plugin;

import java.io.IOException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.domain.management.plugin.AbstractPlugIn;
import org.jboss.as.domain.management.plugin.Identity;
import org.jboss.as.domain.management.plugin.ValidatePasswordCredential;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class OvirtAuthPlugIn extends AbstractPlugIn {

    private static final String scope = "ovirt-app-api";

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
                        String token = null;
                        String engineSessionId = null;
                        boolean loginSucceeded = true;
                        try {
                            Map<String, Object> jsonResponse = SsoOAuthServiceUtils.loginWithPassword(
                                    username, new String(chars), scope);
                            FiltersHelper.isStatusOk(jsonResponse);
                            token = (String) jsonResponse.get("access_token");
                            engineSessionId = SsoUtils.createUserSession(null,
                                    FiltersHelper.getPayloadForToken(token),
                                    false);
                        } catch (Exception e) {
                            loginSucceeded = false;
                        }
                        try {
                            return loginSucceeded
                                    && engineSessionId != null
                                    && backend.runQuery(
                                    VdcQueryType.IsUserApplicationContainerManager,
                                    new VdcQueryParametersBase(engineSessionId)
                            ).getSucceeded();
                        } finally {
                            if (token != null) {
                                SsoOAuthServiceUtils.revoke(token, "");
                            }
                        }
                    }
                };
            }
        };
    }
}
