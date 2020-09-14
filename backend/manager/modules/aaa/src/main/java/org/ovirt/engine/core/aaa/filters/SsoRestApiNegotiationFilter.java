package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoRestApiNegotiationFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String scope = "ovirt-app-api ovirt-ext=token:login-on-behalf";

    /**
     * In order to support several alternative authentication extension we
     * store their associated profiles in a stack inside the HTTP session,
     * this is the key for that stack.
     */
    private static final String STACK_ATTR = SsoRestApiNegotiationFilter.class.getName() + ".stack";

    private volatile Collection<String> schemes;
    private volatile List<AuthenticationProfile> profiles;
    private long caps = 0;

    @Override
    public void init(FilterConfig filterConfig) {
        caps |= Authn.Capabilities.AUTHENTICATE_NEGOTIATE_INTERACTIVE | Authn.Capabilities.AUTHENTICATE_NEGOTIATE_NON_INTERACTIVE;

        AuthenticationProfileRepository.getInstance().addObserver((o, arg) -> cacheNegotiatingProfiles()
        );
        cacheNegotiatingProfiles();
    }

    private synchronized void cacheNegotiatingProfiles() {
        schemes = new ArrayList<>();
        profiles = new ArrayList<>();

        for (AuthenticationProfile profile : AuthenticationProfileRepository.getInstance().getProfiles()) {
            ExtMap authnContext = profile.getAuthn().getContext();
            if ((authnContext.<Long> get(Authn.ContextKeys.CAPABILITIES).longValue() & caps) != 0) {
                profiles.add(profile);
                schemes.addAll(authnContext.<Collection<String>>get(Authn.ContextKeys.HTTP_AUTHENTICATION_SCHEME, Collections.<String>emptyList()));
            }
        }

        Collections.sort(profiles, Comparator.comparing(AuthenticationProfile::getNegotiationPriority));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Entered SsoRestApiNegotiationFilter");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if ((FiltersHelper.isAuthenticated(req) && FiltersHelper.isSessionValid((HttpServletRequest) request)) ||
                !EngineLocalConfig.getInstance().getBoolean("ENGINE_RESTAPI_NEGO")) {
            log.debug("SsoRestApiNegotiationFilter Not performing Negotiate Auth");
            chain.doFilter(request, response);
        } else {
            log.debug("SsoRestApiNegotiationFilter performing Negotiate Auth");
            try {
                req.setAttribute(FiltersHelper.Constants.REQUEST_SCHEMES_KEY, schemes);
                HttpSession session = req.getSession(false);
                Deque<AuthenticationProfile> stack = null;
                if (session != null) {
                    stack = (Deque<AuthenticationProfile>) session.getAttribute(STACK_ATTR);
                }
                if (stack == null) {
                    stack = new ArrayDeque<>();
                    stack.addAll(profiles);
                }
                AuthResult authResult = doAuth(req, resp, stack);

                if (!stack.isEmpty()) {
                    req.getSession(true).setAttribute(STACK_ATTR, stack);
                } else {
                    if (session != null) {
                        session.removeAttribute(STACK_ATTR);
                    }
                    if (authResult.username != null) {
                        log.debug("SsoRestApiNegotiationFilter invoking SsoAuthServiceUtils.loginOnBehalf for : {}", authResult.username);
                        Map<String, Object> jsonResponse =
                                SsoOAuthServiceUtils.loginOnBehalf(
                                        req,
                                        authResult.username,
                                        scope,
                                        authResult.authRecord);
                        FiltersHelper.isStatusOk(jsonResponse);
                        log.debug("SsoRestApiNegotiationFilter creating user session");
                        SsoUtils.createUserSession(req, FiltersHelper.getPayloadForToken(
                                (String) jsonResponse.get("access_token")), false);
                    }
                    chain.doFilter(req, resp);
                }
            } catch (Exception e) {
                req.setAttribute(
                        SessionConstants.SSO_AUTHENTICATION_ERR_MSG,
                        e.getMessage());
                log.error("Cannot authenticate using External Authentication: {}", e.getMessage());
                log.debug("Cannot authenticate using External Authentication", e);
                chain.doFilter(req, resp);
            }
        }
    }

    private AuthResult doAuth(HttpServletRequest req, HttpServletResponse rsp, Deque<AuthenticationProfile> stack) {
        AuthResult authResult = new AuthResult();
        log.debug("Performing external authentication");

        boolean stop = false;
        while (!stop && !stack.isEmpty()) {
            AuthenticationProfile profile = stack.peek();

            ExtMap output = profile.getAuthn().invoke(
                    new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authn.InvokeCommands.AUTHENTICATE_NEGOTIATE
                    ).mput(
                            Authn.InvokeKeys.HTTP_SERVLET_REQUEST,
                            req
                    ).mput(
                            Authn.InvokeKeys.HTTP_SERVLET_RESPONSE,
                            rsp
                    )
            );

            switch (output.<Integer> get(Authn.InvokeKeys.RESULT)) {
                case Authn.AuthResult.SUCCESS:
                    ExtMap authRecord = output.get(Authn.InvokeKeys.AUTH_RECORD);
                    authResult.authRecord = authRecord;
                    authResult.username = String.format("%s@%s", authRecord.get(Authn.AuthRecord.PRINCIPAL), profile.getName());
                    stack.clear();
                    break;

                case Authn.AuthResult.NEGOTIATION_UNAUTHORIZED:
                    stack.pop();
                    break;

                case Authn.AuthResult.NEGOTIATION_INCOMPLETE:
                    stop = true;
                    break;

                default:
                    log.error("Unexpected authentication result. AuthResult code is {}",
                            output.<Integer> get(Authn.InvokeKeys.RESULT));
                    stack.pop();
                    break;
            }
        }
        log.debug("External Authentication result: {}", StringUtils.isNotEmpty(authResult.username));
        return authResult;
    }

    @Override
    public void destroy() {
        // empty
    }

    static class AuthResult {
        String username;
        ExtMap authRecord;
    }
}
