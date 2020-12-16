package org.ovirt.engine.core.sso.service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Mapping;
import org.ovirt.engine.core.sso.api.AuthResult;
import org.ovirt.engine.core.sso.api.AuthenticationProfile;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NegotiateAuthService {
    public static final String STACK_ATTR = NegotiateAuthService.class.getName() + ".stack";
    public static final String REQUEST_SCHEMES_KEY = "request_schemes";
    private static Logger log = LoggerFactory.getLogger(NegotiateAuthService.class);
    private Set<String> schemes;
    private List<AuthenticationProfile> profiles;
    private long caps = 0;

    public NegotiateAuthService(final Collection<AuthenticationProfile> availableProfiles) {
        caps |= Authn.Capabilities.AUTHENTICATE_NEGOTIATE_INTERACTIVE |
                Authn.Capabilities.AUTHENTICATE_NEGOTIATE_NON_INTERACTIVE;

        cacheNegotiatingProfiles(availableProfiles);
    }

    private void cacheNegotiatingProfiles(final Collection<AuthenticationProfile> availableProfiles) {
        schemes = new HashSet<>();
        profiles = new ArrayList<>();

        for (AuthenticationProfile profile : availableProfiles) {
            ExtMap authnContext = profile.getAuthn().getContext();
            if ((authnContext.<Long> get(Authn.ContextKeys.CAPABILITIES).longValue() & caps) != 0) {
                profiles.add(profile);
                schemes.addAll(authnContext.<Collection<String>> get(Authn.ContextKeys.HTTP_AUTHENTICATION_SCHEME,
                        Collections.<String> emptyList()));
            }
        }

        Collections.sort(profiles, Comparator.comparing(AuthenticationProfile::getNegotiationPriority));
    }

    public AuthResult doAuth(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Deque<AuthenticationProfile> stack =
                (Deque<AuthenticationProfile>) request.getAttribute(NegotiateAuthService.STACK_ATTR);
        request.getSession(true).setAttribute(REQUEST_SCHEMES_KEY, schemes);
        if (stack == null) {
            stack = new ArrayDeque<>();
            stack.addAll(getProfiles());
        }

        AuthResult retVal = doAuth(request, response, stack);

        if (!stack.isEmpty()) {
            request.setAttribute(NegotiateAuthService.STACK_ATTR, stack);
        } else if (retVal.getToken() != null) {
            request.removeAttribute(NegotiateAuthService.STACK_ATTR);
        }

        return retVal;
    }

    private AuthResult doAuth(HttpServletRequest req, HttpServletResponse rsp, Deque<AuthenticationProfile> stack)
            throws IOException, ServletException {
        log.debug("Performing external authentication");
        AuthResult retVal = new AuthResult(Authn.AuthResult.NEGOTIATION_UNAUTHORIZED);
        String token = null;
        boolean stop = false;
        try {
            while (!stop && !stack.isEmpty()) {
                AuthenticationProfile profile = stack.peek();

                ExtMap output = profile.getAuthn()
                        .invoke(
                                new ExtMap().mput(
                                        Base.InvokeKeys.COMMAND,
                                        Authn.InvokeCommands.AUTHENTICATE_NEGOTIATE)
                                        .mput(
                                                Authn.InvokeKeys.HTTP_SERVLET_REQUEST,
                                                req)
                                        .mput(
                                                Authn.InvokeKeys.HTTP_SERVLET_RESPONSE,
                                                rsp));

                retVal.setStatus(output.<Integer> get(Authn.InvokeKeys.RESULT));

                switch (output.<Integer> get(Authn.InvokeKeys.RESULT)) {
                case Authn.AuthResult.SUCCESS:
                    try {
                        ExtMap authRecord = output.get(Authn.InvokeKeys.AUTH_RECORD);
                        if (profile.getMapper() != null) {
                            authRecord = profile.getMapper()
                                    .invoke(
                                            new ExtMap().mput(
                                                    Base.InvokeKeys.COMMAND,
                                                    Mapping.InvokeCommands.MAP_AUTH_RECORD)
                                                    .mput(
                                                            Authn.InvokeKeys.AUTH_RECORD,
                                                            authRecord),
                                            true)
                                    .get(
                                            Authn.InvokeKeys.AUTH_RECORD,
                                            authRecord);
                        }
                        ExtMap input = new ExtMap().mput(
                                Base.InvokeKeys.COMMAND,
                                Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD)
                                .mput(
                                        Authn.InvokeKeys.AUTH_RECORD,
                                        authRecord)
                                .mput(
                                        Authz.InvokeKeys.QUERY_FLAGS,
                                        Authz.QueryFlags.RESOLVE_GROUPS | Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE);
                        if (SsoService.getSsoContext(req)
                                .getSsoLocalConfig()
                                .getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO")) {
                            input.put(Authz.InvokeKeys.HTTP_SERVLET_REQUEST, req);
                        }
                        ExtMap outputMap = profile.getAuthz().invoke(input);
                        token = SsoService.getTokenFromHeader(req);
                        SsoSession ssoSession = SsoService.persistAuthInfoInContextWithToken(req,
                                token,
                                null,
                                profile.getName(),
                                authRecord,
                                outputMap.get(Authz.InvokeKeys.PRINCIPAL_RECORD));
                        log.info("User {}@{} with profile [{}] successfully logged in with scopes : {} ",
                                SsoService.getUserId(outputMap.get(Authz.InvokeKeys.PRINCIPAL_RECORD)),
                                profile.getAuthzName(),
                                profile.getName(),
                                ssoSession.getScope());
                        token = (String) req.getAttribute(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN);
                        stack.clear();
                    } catch (Exception e) {
                        log.debug("Cannot fetch principal, trying other authn extension.");
                        stack.pop();
                    }
                    break;

                case Authn.AuthResult.NEGOTIATION_UNAUTHORIZED:
                    stack.pop();
                    break;

                case Authn.AuthResult.NEGOTIATION_INCOMPLETE:
                    stop = true;
                    break;

                default:
                    log.error("Unexpected authentication result. AuthResult code: {}",
                            output.<Integer> get(Authn.InvokeKeys.RESULT));
                    stack.pop();
                    break;
                }
            }
            if (!stack.isEmpty()) {
                req.getSession(true).setAttribute(STACK_ATTR, stack);
            } else {
                req.getSession(true).removeAttribute(STACK_ATTR);
            }
        } catch (Exception ex) {
            log.error("External Authentication Failed: {}", ex.getMessage());
            log.debug("External Authentication Failed", ex);
            token = null;
        }
        log.debug("External Authentication result: {}", StringUtils.isNotEmpty(token));
        retVal.setToken(token);
        return retVal;
    }

    public List<AuthenticationProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }
}
