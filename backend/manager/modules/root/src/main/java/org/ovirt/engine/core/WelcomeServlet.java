package org.ovirt.engine.core;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.branding.BrandingManager;
import org.ovirt.engine.core.utils.branding.BrandingTheme;
import org.ovirt.engine.core.utils.branding.BrandingWelcomeResourceBundle;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

/**
 * This Servlet serves the welcome page to allow users to select either web admin or user portal.
 */
@WebServlet(value = "/ovirt-engine")
public class WelcomeServlet extends HttpServlet {
    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(WelcomeServlet.class);

    /**
     * Generated UID.
     */
    private static final long serialVersionUID = 8289914264581273721L;

    /**
     * The request attribute keys describing the available locales.
     */
    private static final String LOCALE_KEYS = "localeKeys";

    /**
     * The request attribute containing the section map.
     */
    private static final String SECTIONS = "sections";

    /**
     * The request attribute containing the version.
     */
    private static final String VERSION = "version";

    /**
     * Branding theme attribute key.
     */
    private static final String THEMES_KEY = "brandingStyle";

    /**
     * Application type attribute key.
     */
    private static final String APPLICATION_TYPE = "applicationType";

    /**
     * Back-end bean for database access.
     */
    private BackendLocal backend;

    private BrandingManager brandingManager;

    /**
     * Setter for the {@code BackendLocal} bean.
     * @param backendLocal The bean
     */
    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(final BackendLocal backendLocal) {
        this.backend = backendLocal;
    }

    @Override
    public void init() {
        init(BrandingManager.getInstance());
    }

    void init(BrandingManager brandingManager) {
        this.brandingManager = brandingManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        Locale userLocale = (Locale) request.getAttribute(LocaleFilter.LOCALE);
        log.info("Detected Locale: " + userLocale.toLanguageTag());
        request.setAttribute(LOCALE_KEYS, LocaleFilter.getLocaleKeys());
        List<BrandingTheme> brandingThemes = brandingManager.getBrandingThemes();
        request.setAttribute(THEMES_KEY, brandingThemes);
        request.setAttribute(APPLICATION_TYPE, BrandingTheme.ApplicationType.WELCOME);
        String oVirtVersion = (String) backend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion,
                        ConfigCommon.defaultConfigurationVersion)).getReturnValue();
        request.setAttribute(VERSION, oVirtVersion != null ? oVirtVersion : "myVersion");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp");
        response.setContentType("text/html;charset=UTF-8");
        // Load the appropriate resource bundle with the right locale.
        ResourceBundle bundle = ResourceBundle.getBundle("BrandingBundle", userLocale,
                BrandingWelcomeResourceBundle.getBrandingControl());
        // Pass the loaded resource bundle to the jsp.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(bundle, userLocale));
        request.setAttribute(SECTIONS, brandingManager.getWelcomeSections(userLocale));
        if (dispatcher != null) {
            dispatcher.include(request, response);
        }
    }
}
