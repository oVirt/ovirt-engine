package org.ovirt.engine.core.sso.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ovirt.engine.core.sso.utils.AuthenticationUtils;
import org.ovirt.engine.core.sso.utils.DBUtils;
import org.ovirt.engine.core.sso.utils.NegotiateAuthUtils;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOContext;
import org.ovirt.engine.core.sso.utils.SSOExtensionsManager;
import org.ovirt.engine.core.sso.utils.SSOLocalConfig;

public class SSOContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        SSOLocalConfig localConfig = new SSOLocalConfig();

        SSOContext ssoContext = new SSOContext();
        ssoContext.setSsoExtensionsManager(new SSOExtensionsManager(localConfig));
        ssoContext.init(localConfig);
        ssoContext.setSsoClientRegistry(DBUtils.getAllSsoClientsInfo());
        ssoContext.setScopeDependencies(DBUtils.getAllSsoScopeDependencies());
        ssoContext.setSsoProfiles(AuthenticationUtils.getAvailableProfiles(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfilesSupportingPasswd(AuthenticationUtils.getAvailableProfilesSupportingPasswd(ssoContext.getSsoExtensionsManager()));
        ssoContext.setNegotiateAuthUtils(new NegotiateAuthUtils(ssoContext.getProfiles()));
        ctx.setAttribute(SSOConstants.OVIRT_SSO_CONTEXT, ssoContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // empty
    }
}
