package org.ovirt.engine.core.aaa.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;
    private static final Logger log = LoggerFactory.getLogger(SsoLoginServlet.class);

    private String postActionUrl;
    private String authSequencePriorityPropertyName;

    @Override
    public void init() throws ServletException {
        postActionUrl = getServletContext().getInitParameter("post-action-url");
        if (postActionUrl == null) {
            throw new RuntimeException("No post-action-url init parameter specified for SsoLoginServlet.");
        }
        authSequencePriorityPropertyName = getServletContext().getInitParameter("auth-seq-priority-property-name");
        if (postActionUrl == null) {
            throw new RuntimeException("No auth-seq-priority-property-name init parameter specified for SsoLoginServlet.");
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String state = SsoUtils.createUniqueStateInSessionIfNotExists(request);
        String scope = String.format("ovirt-app-admin ovirt-app-portal ovirt-ext=auth:sequence-priority=%s",
                EngineLocalConfig.getInstance().getProperty(authSequencePriorityPropertyName));
        request.getSession(true).setAttribute("app_url", request.getParameter("app_url"));
        String redirectUri = String.format("%s://%s:%s%s",
                request.getScheme(),
                FiltersHelper.getRedirectUriServerName(request.getServerName()),
                request.getServerPort(),
                postActionUrl);

        response.sendRedirect(new URLBuilder(EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_AUTH_URL"), "/oauth/authorize")
                .addParameter("client_id", EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_CLIENT_ID"))
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", redirectUri)
                .addParameter("scope", scope)
                .addParameter("state", state).build());
    }

}
