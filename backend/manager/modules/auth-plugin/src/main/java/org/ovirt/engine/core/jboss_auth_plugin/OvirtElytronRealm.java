package org.ovirt.engine.core.jboss_auth_plugin;

import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * Elytron SecurityRealm implementation for oVirt Engine management interface authentication.
 *
 * This realm validates credentials against oVirt's OAuth SSO system and checks
 * that the user has the SUPER_USER role (IsUserApplicationContainerManager).
 *
 * Replaces the legacy OvirtAuthPlugIn.
 */
public class OvirtElytronRealm implements SecurityRealm {

    private static final Logger log = LoggerFactory.getLogger(OvirtElytronRealm.class);
    private static final String SCOPE = "ovirt-app-api";

    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        log.debug("getRealmIdentity called for principal: {}", principal.getName());
        return new OvirtRealmIdentity(principal.getName());
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                    String algorithmName,
                                                    AlgorithmParameterSpec parameterSpec)
            throws RealmUnavailableException {
        // We don't provide credentials, only verify them
        return SupportLevel.UNSUPPORTED;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                  String algorithmName)
            throws RealmUnavailableException {
        if (PasswordGuessEvidence.class.isAssignableFrom(evidenceType)) {
            return SupportLevel.SUPPORTED;
        }
        return SupportLevel.UNSUPPORTED;
    }

    /**
     * RealmIdentity implementation that performs oVirt SSO authentication.
     */
    private static class OvirtRealmIdentity implements RealmIdentity {

        private final String username;

        OvirtRealmIdentity(String username) {
            this.username = username;
        }

        @Override
        public Principal getRealmIdentityPrincipal() {
            return new Principal() {
                @Override
                public String getName() {
                    return username;
                }
            };
        }

        @Override
        public boolean exists() throws RealmUnavailableException {
            // We can't efficiently check existence without verifying credentials,
            // so we return true and let verifyEvidence determine actual validity
            return true;
        }

        @Override
        public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
            if (!(evidence instanceof PasswordGuessEvidence)) {
                log.debug("Unsupported evidence type for user '{}': {}", username, evidence.getClass().getName());
                return false;
            }

            log.debug("Verifying password evidence for user '{}'", username);
            char[] password = ((PasswordGuessEvidence) evidence).getGuess();

            BackendLocal backend;
            try {
                backend = (BackendLocal) new InitialContext().lookup(
                    "java:global/engine/bll/Backend!" + BackendLocal.class.getName());
            } catch (NamingException e) {
                log.error("Failed to lookup backend for user '{}': {}", username, e.getMessage());
                throw new RealmUnavailableException("Can't communicate with the backend API", e);
            }

            String token = null;
            String engineSessionId = null;
            boolean loginSucceeded = true;

            try {
                log.trace("Calling SSO loginWithPassword for user '{}'", username);
                Map<String, Object> jsonResponse = SsoOAuthServiceUtils.loginWithPassword(
                    username, new String(password), SCOPE);
                FiltersHelper.isStatusOk(jsonResponse);
                token = (String) jsonResponse.get("access_token");
                log.trace("SSO token obtained for user '{}'", username);
                engineSessionId = SsoUtils.createUserSession(null,
                    FiltersHelper.getPayloadForToken(token), false);
                log.trace("Engine session created for user '{}'", username);
            } catch (Exception e) {
                log.debug("SSO authentication failed for user '{}': {}", username, e.getMessage());
                loginSucceeded = false;
            }

            try {
                if (!loginSucceeded || engineSessionId == null) {
                    log.debug("Authentication failed for user '{}': loginSucceeded={}, hasSession={}",
                            username, loginSucceeded, engineSessionId != null);
                    return false;
                }

                boolean isManager = backend.runQuery(
                        QueryType.IsUserApplicationContainerManager,
                        new QueryParametersBase(engineSessionId)
                ).getSucceeded();

                if (isManager) {
                    log.debug("Authentication successful for user '{}'", username);
                } else {
                    log.debug("User '{}' authenticated but is not an application container manager", username);
                }
                return isManager;
            } finally {
                if (token != null) {
                    log.trace("Revoking SSO token for user '{}'", username);
                    SsoOAuthServiceUtils.revoke(token, "");
                }
            }
        }

        @Override
        public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
                                                        String algorithmName,
                                                        AlgorithmParameterSpec parameterSpec)
                throws RealmUnavailableException {
            return SupportLevel.UNSUPPORTED;
        }

        @Override
        public <C extends Credential> C getCredential(Class<C> credentialType)
                throws RealmUnavailableException {
            return null;
        }

        @Override
        public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
                                                      String algorithmName)
                throws RealmUnavailableException {
            if (PasswordGuessEvidence.class.isAssignableFrom(evidenceType)) {
                return SupportLevel.SUPPORTED;
            }
            return SupportLevel.UNSUPPORTED;
        }
    }
}
