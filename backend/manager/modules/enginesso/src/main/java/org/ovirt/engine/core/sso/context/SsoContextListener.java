package org.ovirt.engine.core.sso.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ovirt.engine.core.sso.utils.AuthenticationUtils;
import org.ovirt.engine.core.sso.utils.DBUtils;
import org.ovirt.engine.core.sso.utils.LocalizationUtils;
import org.ovirt.engine.core.sso.utils.NegotiateAuthUtils;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoContext;
import org.ovirt.engine.core.sso.utils.SsoExtensionsManager;
import org.ovirt.engine.core.sso.utils.SsoLocalConfig;

public class SsoContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        SsoLocalConfig localConfig = new SsoLocalConfig();

        SsoContext ssoContext = new SsoContext();
        ssoContext.setSsoExtensionsManager(new SsoExtensionsManager(localConfig));
        ssoContext.init(localConfig);
        ssoContext.setSsoClientRegistry(DBUtils.getAllSsoClientsInfo());
        ssoContext.setScopeDependencies(DBUtils.getAllSsoScopeDependencies());
        ssoContext.setSsoDefaultProfile(AuthenticationUtils.getDefaultProfile(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfiles(AuthenticationUtils.getAvailableProfiles(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfilesSupportingPasswd(
                AuthenticationUtils.getAvailableProfilesSupportingPasswd(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfilesSupportingPasswdChange(
                AuthenticationUtils.getAvailableProfilesSupportingPasswdChange(ssoContext.getSsoExtensionsManager()));
        ssoContext.setNegotiateAuthUtils(new NegotiateAuthUtils(ssoContext.getProfiles()));
        ssoContext.setLocalizationUtils(new LocalizationUtils(SsoConstants.APP_MESSAGE_FILENAME));
        ctx.setAttribute(SsoConstants.OVIRT_SSO_CONTEXT, ssoContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // empty
    }
}
