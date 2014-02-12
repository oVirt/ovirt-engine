package org.ovirt.engine.core.aaa;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

/**
 * This filter should be added to the {@code web.xml} file to the applications that want to use the authentication
 * mechanism implemented in this package.
 */
public class AuthenticationFilter implements Filter {
    /**
     * The authentication profiles used to perform the authentication process.
     */
     private volatile List<AuthenticationProfile> profiles;

    /**
     * We store a boolean flag in the HTTP session that indicates if the user has been already authenticated, this is
     * the key for that flag.
     */
    private static final String AUTHENTICATED_ATTR = AuthenticationFilter.class.getName() + ".authenticated";

    /**
     * When a user has been authenticated we store its login name in the HTTP session, this is the key for that name.
     */
    private static final String NAME_ATTR = AuthenticationFilter.class.getName() + ".name";

    /**
     * In order to support several alternative authenticators we store their names in a stack inside the HTTP session,
     * this is the key for that stack.
     */
    private static final String STACK_ATTR = AuthenticationFilter.class.getName() + ".stack";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /**
     * Lazily find all the profiles that support negotiation and store them reversed to simplify the creation of the
     * stacks of profiles later when processing requests.
     */
    private void findNegotiatingProfiles() {
        if (profiles == null) {
            synchronized(this) {
                if (profiles == null) {
                    profiles = new ArrayList<AuthenticationProfile>(1);
                    for (AuthenticationProfile profile : AuthenticationProfileManager.getInstance().getProfiles()) {
                        if (profile != null) {
                            Authenticator authenticator = profile.getAuthenticator();
                            if (authenticator instanceof NegotiatingAuthenticator) {
                                profiles.add(0, profile);
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        // If there are no authentication profiles supporting negotiation then we don't do anything, as there is no
        // authentication to perform:
        findNegotiatingProfiles();
        if (profiles.isEmpty()) {
            chain.doFilter(req, rsp);
            return;
        }

        // Perform the authentication:
        doFilter((HttpServletRequest) req, (HttpServletResponse) rsp, chain);
    }

    private void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain)
            throws IOException, ServletException {
        // If the user has been previously authenticated in this session then we don't need to do it again, but we do
        // need to replace the principal with the name of the authenticated entity:
        HttpSession session = req.getSession();
        Boolean authenticated = (Boolean) session.getAttribute(AUTHENTICATED_ATTR);
        if (authenticated != null && authenticated) {
            String name = (String) session.getAttribute(NAME_ATTR);
            req = new AuthenticatedRequestWrapper(req, name);
            chain.doFilter(req, rsp);
            return;
        }

        // We need to remember which of the profiles was managing the negotiation with the client, so we store a stack
        // of the available authenticators in the session:
        @SuppressWarnings("unchecked")
        Deque<String> stack = (Deque<String>) session.getAttribute(STACK_ATTR);
        if (stack == null) {
            stack = new ArrayDeque<String>();
            for (AuthenticationProfile profile : profiles) {
                stack.push(profile.getName());
            }
            session.setAttribute(STACK_ATTR, stack);
        }

        while (!stack.isEmpty()) {
            // Resume the negotiation with the profile at the top of the stack:
            AuthenticationProfile profile = AuthenticationProfileManager.getInstance().getProfile(stack.peek());
            if (profile == null) {
                session.invalidate();
                rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            NegotiatingAuthenticator authenticator = (NegotiatingAuthenticator) profile.getAuthenticator();
            NegotiationResult result = authenticator.negotiate(req, rsp);

            // If the negotiation isn't finished then we assume that the response has been populated by the
            // authenticator and we just let the container sent it back to the client:
            if (result == null) {
                return;
            }

            // If the negotiation is finished and authentication succeeded then we have to remember in the session that
            // the user has been authenticated and its login name, also we need to clean the stack of authenticators and
            // replace the request with a wrapper that contains the user name returned by the authenticator:
            if (result.isAuthenticated()) {
                String name = new StringBuilder(result.getName())
                    .append("@")
                    .append(profile.getName())
                    .toString();
                session.setAttribute(AUTHENTICATED_ATTR, true);
                session.setAttribute(NAME_ATTR, name);
                session.removeAttribute(STACK_ATTR);
                req = new AuthenticatedRequestWrapper(req, name);
                chain.doFilter(req, rsp);
                return;
            }

            // The negotiation finished, but the authentication failed, so we need to clear the current authenticator
            // from the stack and try with the next one:
            stack.pop();
            session.setAttribute(STACK_ATTR, stack);
        }

        // If we are here then there are no more authenticators to try so we need to invalidate the session and reject
        // the request:
        session.invalidate();
        rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
