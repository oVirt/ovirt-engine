package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.LocaleUtils;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter attempts to detect the locale of the user based on the following criteria.
 * <ol>
 *   <li>The existence of a locale parameter</li>
 *   <li>If no parameter, check for a locale cookie</li>
 *   <li>If no cookie, check the accept headers of the request</li>
 *   <li>If no headers, then default to the US locale</li>
 * </ol>
 */
public class LocaleFilter implements Filter {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LocaleFilter.class);

    /**
     * Constant for parameter and cookie name.
     */
    public static final String LOCALE = "locale";

    /**
     * The root of a path.
     */
    public static final String ROOT_PATH = "/";

    /**
     * The default locale.
     */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * The extension of the properties file containing the available languages.
     */
    private static final String LANGUAGES_FILE = "languages.properties"; // the property file name

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        Locale locale = determineLocale((HttpServletRequest) request);
        request.setAttribute(LOCALE, locale);
        setCookie((HttpServletRequest) request, (HttpServletResponse) response, locale);
        chain.doFilter(request, response);
    }

    /**
     * Add the {@code Locale} cookie to the response.
     * @param request The {@code HttpServletRequest}
     * @param response The {@code HttpServletResponse}
     * @param userLocale The {@code Locale} to put in the cookie.
     */
    private void setCookie(final HttpServletRequest request, final HttpServletResponse response, final Locale userLocale) {
        // Detected locale doesn't match the default locale, set a cookie.
        Cookie cookie = new Cookie(LocaleFilter.LOCALE, userLocale.toString());
        cookie.setSecure("https".equalsIgnoreCase(request.getScheme()));
        cookie.setPath(ROOT_PATH);
        cookie.setMaxAge(Integer.MAX_VALUE); // Doesn't expire.
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Check for locale in 4 steps:
     * <ol>
     *   <li>If no cookies exist, then check for a locale parameter and use that.</li>
     *   <li>Check if the cookie exists which selects the locale.</li>
     *   <li>If no locale parameter exists, check the accept headers.</li>
     *   <li>Default to the US locale</li>
     * </ol>
     * @param request The {@code HttpServletRequest}
     * @return The determined {@code Locale}
     */
    private Locale determineLocale(final HttpServletRequest request) {
        // Step 1.
        Locale locale = LocaleUtils.getLocaleFromString(request.getParameter(LOCALE));
        // Step 2.
        if (locale == null) { // No locale parameter.
            locale = getLocaleFromCookies(request.getCookies());
        }
        // Step 3.
        if (locale == null) { // No selected locale in cookies.
            locale = request.getLocale();
        }
        // Step 4.
        if (locale == null) { // No accept headers.
            locale = DEFAULT_LOCALE;
        }
        Locale resolvedLocale = lookupSupportedLocale(locale, getLocaleKeys());
        log.debug("Incoming locale '{}'. Filter determined locale to be '{}'",
                locale.toLanguageTag(), resolvedLocale.toLanguageTag());
        return resolvedLocale;
    }

    /**
     * Loop over the cookies to determine if the locale is set there.
     * @param cookies The list of {@code Cookie}s.
     * @return The {@code Locale} if a cookie is found, null otherwise.
     */
    private Locale getLocaleFromCookies(final Cookie[] cookies) {
        Locale locale = null;
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (LOCALE.equalsIgnoreCase(cookie.getName())) {
                    locale = LocaleUtils.getLocaleFromString(cookie.getValue());
                    break;
                }
            }
        }
        return locale;
    }

    /**
     * Get a list of available locales.
     * @return A {@code List} of Locale strings.
     */
    public static List<String> getLocaleKeys() {
        Properties languages = getLanguageProperties();
        @SuppressWarnings("unchecked")
        List<String> keys = (List<String>) Collections.list(languages.propertyNames());
        // Can't just return the unsorted list, it won't match the GWT output.
        // Apparently GWT does a sort before showing the list as well, which
        // works out nicely for me.
        Collections.sort(keys);
        return keys;
    }

    /**
     * Load the language property file into a Properties object.
     * @return The {@code Properties} object.
     */
    private static Properties getLanguageProperties() {
        Properties prop = new Properties();
        try {
            prop = ResourceUtils.loadProperties(LocaleFilter.class, LANGUAGES_FILE);
        } catch (IOException e) {
            log.error("Unable to load supported langauges file", e);
        }
        return prop;
    }

    /**
     *
     * @param filteredLocale The locale the filter determined.
     * @param supportedLocales The list of supported locale strings.
     * @return The supported locale based on the passed in filteredLocale and list of supported locales.
     */
    private Locale lookupSupportedLocale(final Locale filteredLocale, final List<String> supportedLocales) {
        for (String supportedLocale: supportedLocales) {
            Locale locale = LocaleUtils.getLocaleFromString(supportedLocale);
            @SuppressWarnings("unchecked")
            List<Locale> localeLookupList = org.apache.commons.lang.LocaleUtils.localeLookupList(locale);
            for (Locale lookupLocale: localeLookupList) {
                if (lookupLocale.equals(filteredLocale)) {
                    // Matched on one of the locales. Find the supported one in the list.
                    return locale;
                }
            }
        }
        return DEFAULT_LOCALE;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    public static Locale getLocaleFromRequest(final HttpServletRequest request) {
        Locale locale = (Locale) request.getAttribute(LOCALE);
        if (locale == null) {
            log.error("no locale in request -- code problem -- check LocaleFilter configuration. Defaulting to US");
            locale = DEFAULT_LOCALE;
        }
        return locale;
    }

}
