package org.ovirt.engine.core;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;

public class WelcomeContextListener implements ServletContextListener {

    private static final String moduleScope = "ovirt-app-admin ovirt-app-portal ovirt-ext=auth:sequence-priority=%s";
    private static final String switchUserScope = "ovirt-app-admin ovirt-app-portal ovirt-ext=auth:sequence-priority=~I";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            String engineUri = EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_URI);
            String authSequence = EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_AUTH_SEQUENCE_welcome");
            event.getServletContext().setAttribute("engine_url", engineUri);
            event.getServletContext().setAttribute("sso_logout_url",
                    new URLBuilder(engineUri, WelcomeUtils.LOGOUT_URI).build());
            event.getServletContext().setAttribute("sso_login_url", WelcomeUtils.getLoginUrl(
                    engineUri,
                    String.format(moduleScope, authSequence)));
            event.getServletContext().setAttribute("sso_switch_user_url", String.format("%s%s?%s=%s",
                    engineUri,
                    WelcomeUtils.SWITCH_USER_URI,
                    WelcomeUtils.SCOPE,
                    URLEncoder.encode(switchUserScope, StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to initialize Welcome Context", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // empty
    }
}
