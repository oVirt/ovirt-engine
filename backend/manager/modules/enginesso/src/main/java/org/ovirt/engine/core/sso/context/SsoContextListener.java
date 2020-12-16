package org.ovirt.engine.core.sso.context;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.db.SsoDao;
import org.ovirt.engine.core.sso.service.AuthenticationService;
import org.ovirt.engine.core.sso.service.LocalizationService;
import org.ovirt.engine.core.sso.service.NegotiateAuthService;
import org.ovirt.engine.core.sso.service.SsoClientsRegistry;
import org.ovirt.engine.core.sso.service.SsoExtensionsManager;
import org.ovirt.engine.core.sso.utils.SsoLocalConfig;

public class SsoContextListener implements ServletContextListener {

    @Inject
    private Instance<SsoClientsRegistry> ssoClientRegistry;

    @Inject
    private Instance<SsoDao> ssoDao;

    @Inject
    private Instance<SsoLocalConfig> ssoLocalConfig;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        SsoLocalConfig localConfig = ssoLocalConfig.get();

        SsoContext ssoContext = new SsoContext();
        ssoContext.setSsoExtensionsManager(new SsoExtensionsManager(localConfig));
        ssoContext.init(localConfig);
        ssoContext.setSsoClientRegistry(ssoClientRegistry.get());
        ssoContext.setScopeDependencies(ssoDao.get().getAllSsoScopeDependencies());
        ssoContext.setSsoDefaultProfile(AuthenticationService.getDefaultProfile(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfiles(AuthenticationService.getAvailableProfiles(ssoContext.getSsoExtensionsManager()));
        // required in login.jsp
        ssoContext.setSsoProfilesSupportingPasswd(
                AuthenticationService.getAvailableProfilesSupportingPasswd(ssoContext.getSsoExtensionsManager()));
        ssoContext.setSsoProfilesSupportingPasswdChange(
                AuthenticationService.getAvailableProfilesSupportingPasswdChange(ssoContext.getSsoExtensionsManager()));
        ssoContext.setNegotiateAuthUtils(new NegotiateAuthService(ssoContext.getProfiles()));
        ssoContext.setLocalizationUtils(new LocalizationService(SsoConstants.APP_MESSAGE_FILENAME));

        try (InputStream in = new FileInputStream(localConfig.getPKIEngineCert().getAbsoluteFile())) {
            ssoContext.setEngineCertificate(CertificateFactory.getInstance("X.509").generateCertificate(in));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to load engine certificate.");
        }

        ctx.setAttribute(SsoConstants.OVIRT_SSO_CONTEXT, ssoContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // empty
    }
}
