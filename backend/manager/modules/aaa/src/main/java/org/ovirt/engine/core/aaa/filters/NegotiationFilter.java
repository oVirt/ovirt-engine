package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.aaa.AuthType;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;

/**
 * This filter should be added to the {@code web.xml} file to the applications that want to use the authentication
 * mechanism implemented in this package.
 */
public class NegotiationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(NegotiationFilter.class);

    private static final String CAPABILITIES_PARAMETER = "capabilities";

    /**
     * In order to support several alternative authentication extension we
     * store their associated profiles in a stack inside the HTTP session,
     * this is the key for that stack.
     */
    private static final String STACK_ATTR = NegotiationFilter.class.getName() + ".stack";

    /**
     * The authentication profiles used to perform the authentication process.
     */
    private volatile List<AuthenticationProfile> profiles;
    private long caps = 0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String capsParam = filterConfig.getInitParameter(CAPABILITIES_PARAMETER);
        if (capsParam != null) {
            for (String nego : capsParam.trim().split(" *\\| *")) {
                try {
                    caps |= Authn.Capabilities.class.getField(nego).getLong(null);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
                    log.error("Error calculating authn capabilities while accessing constant  {}", nego);
                }
            }
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Lazily find all the profiles that support negotiation and store them reversed to simplify the creation of the
     * stacks of profiles later when processing requests.
     */
    private void findNegotiatingProfiles(ServletRequest req) {
        Collection<String> schemes = new ArrayList<String>();
        if (profiles == null) {
            synchronized (this) {
                if (profiles == null) {
                    schemes = new ArrayList<>();
                    profiles = new ArrayList<AuthenticationProfile>();

                    for (AuthenticationProfile profile : AuthenticationProfileRepository.getInstance().getProfiles()) {
                        if (profile != null) {
                            ExtMap authnContext = profile.getAuthn().getContext();
                            if ((authnContext.<Long> get(Authn.ContextKeys.CAPABILITIES).longValue() & caps) != 0) {
                                profiles.add(0, profile);
                                schemes.addAll(authnContext.<Collection<String>>get(Authn.ContextKeys.HTTP_AUTHENTICATION_SCHEME, Collections.<String>emptyList()));
                            }
                        }
                    }
                }
            }
        }
        ((HttpServletRequest) req).setAttribute(FiltersHelper.Constants.REQUEST_SCHEMES_KEY, schemes);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpreq = (HttpServletRequest)req;

        if (FiltersHelper.isAuthenticated(httpreq) || httpreq.getAttribute(FiltersHelper.Constants.REQUEST_AUTH_RECORD_KEY) != null) {
            chain.doFilter(req, rsp);
        } else {
            findNegotiatingProfiles(httpreq);
            HttpSession session = httpreq.getSession(false);
            Deque<String> stack = null;
            if (session != null) {
                stack = (Deque<String>)session.getAttribute(STACK_ATTR);
            }
            if (stack == null) {
                stack = new ArrayDeque<String>();
                for (AuthenticationProfile profile : profiles) {
                    stack.push(profile.getName());
                }
            }
            doAuth(httpreq, (HttpServletResponse) rsp, stack);
            if (!stack.isEmpty()) {
                httpreq.getSession(true).setAttribute(STACK_ATTR, stack);
            } else {
                if (session != null) {
                    session.removeAttribute(STACK_ATTR);
                }
                chain.doFilter(req, rsp);
            }
        }
    }

    private void doAuth(HttpServletRequest req, HttpServletResponse rsp, Deque<String> stack)
            throws IOException, ServletException {

        boolean stop = false;
        while (!stop && !stack.isEmpty()) {
            AuthenticationProfile profile =
                    AuthenticationProfileRepository.getInstance().getProfile(stack.peek());

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
                req.setAttribute(FiltersHelper.Constants.REQUEST_AUTH_RECORD_KEY,
                    output.<ExtMap> get(Authn.InvokeKeys.AUTH_RECORD));
                req.setAttribute(FiltersHelper.Constants.REQUEST_AUTH_TYPE_KEY,
                    AuthType.NEGOTIATION);
                req.setAttribute(FiltersHelper.Constants.REQUEST_PROFILE_KEY, profile.getName());
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
    }

}
