package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Locale;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

/**
 * This Servlet serves the welcome page to allow users to select either web admin or user portal.
 */
public class WelcomeServlet extends HttpServlet {
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
     * Back-end bean for database access.
     */
    private transient BackendLocal backend;

    /**
     * The branding manager.
     */
    private transient BrandingManager brandingManager;

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

    /**
     * Init with the branding manager as a parameter.
     * @param brandingManager The branding manager.
     */
    void init(final BrandingManager brandingManager) {
        this.brandingManager = brandingManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        request.setAttribute(LOCALE_KEYS, LocaleFilter.getLocaleKeys());
        String oVirtVersion = backend.runPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion,
                        ConfigCommon.defaultConfigurationVersion)).getReturnValue();
        request.setAttribute(VERSION, oVirtVersion != null ? oVirtVersion : "myVersion");
        request.setAttribute(SECTIONS, brandingManager
                .getWelcomeSections((Locale) request.getAttribute(LocaleFilter.LOCALE)));
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp");
        response.setContentType("text/html;charset=UTF-8");
        if (dispatcher != null) {
            dispatcher.include(request, response);
        }
    }
}
