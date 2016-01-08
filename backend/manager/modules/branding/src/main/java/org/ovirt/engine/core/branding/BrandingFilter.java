package org.ovirt.engine.core.branding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.ovirt.engine.core.utils.servlet.LocaleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The branding filter adds the appropriate attributes to the {@code HttpServletRequest} for the Servlet to be
 * brand-able. It adds the appropriate {@code BrandingTheme.ApplicationType} and the appropriate resource bundle based
 * on that application type.
 */
public class BrandingFilter implements Filter {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BrandingFilter.class);

    /**
     * A blank string.
     */
    private static final String BLANK = "";

    /**
     * web.xml context-param name to look up.
     */
    public static final String APPLICATION_NAME = "applicationName";

    /**
     * Branding theme attribute key.
     */
    static final String THEMES_KEY = "brandingStyle";

    /**
     * The base name of the branding bundle.
     */
    static final String BRANDING_BASE_NAME = "BrandingBundle";

    /**
     * The branding manager used to look up the style sheet.
     */
    private BrandingManager brandingManager;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        Locale userLocale = (Locale) request.getAttribute(LocaleFilter.LOCALE);
        if (userLocale == null) {
            log.warn("Detected Locale is null, setting to default '{}'", LocaleFilter.DEFAULT_LOCALE);
            userLocale = LocaleFilter.DEFAULT_LOCALE;
        }
        List<BrandingTheme> brandingThemes = brandingManager.getBrandingThemes();
        request.setAttribute(THEMES_KEY, brandingThemes);
        request.setAttribute(APPLICATION_NAME, request.getServletContext().getInitParameter(APPLICATION_NAME));
        // Load the appropriate resource bundle with the right locale.
        ResourceBundle bundle = ResourceBundle.getBundle(BRANDING_BASE_NAME, userLocale,
                BrandingResourceBundle.getBrandingControl(BLANK));
        // Pass the loaded resource bundle to the jsp.
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, new LocalizationContext(bundle, userLocale));
        chain.doFilter(request, response);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        init(filterConfig, BrandingManager.getInstance());
    }

    /**
     * Init with extra branding manager parameter.
     * @param filterConfig The filter config from web.xml
     * @param initBrandingManager The branding manager to use.
     * @throws ServletException When unable to initialize the servlet filter.
     */
    public void init(final FilterConfig filterConfig, final BrandingManager initBrandingManager)
            throws ServletException {
        this.brandingManager = initBrandingManager;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

}
