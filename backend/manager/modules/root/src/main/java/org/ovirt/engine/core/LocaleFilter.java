package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.LocaleUtils;

/**
 * This filter attempts to detect the locale of the user based on the following criteria.
 * <ol>
 *   <li>The existence of a locale parameter</li>
 *   <li>If no parameter, check for a locale cookie</li>
 *   <li>If no cookie, check the accept headers of the request</li>
 *   <li>If no headers, then default to the US locale</li>
 * </ol>
 */
@WebFilter(filterName = "LocaleFilter")
public class LocaleFilter implements Filter {
    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(LocaleFilter.class);

    /**
     * Constant for parameter and cookie name.
     */
    public static final String LOCALE = "locale";

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        request.setAttribute(LOCALE, determineLocale((HttpServletRequest) request));
        chain.doFilter(request, response);
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
        //Step 1.
        Locale locale = LocaleUtils.getLocaleFromString(request.getParameter(LOCALE));
        //Step 2.
        if (locale == null) { //No locale parameter.
            locale = getLocaleFromCookies(request.getCookies());
        }
        //Step 3.
        if (locale == null) { //No selected locale in cookies.
            locale = request.getLocale();
        }
        //Step 4.
        if (locale == null) { //No accept headers.
            locale = Locale.US;
        }
        log.debug("Filter determined locale to be: " + locale.toLanguageTag());
        return locale;
    }

    /**
     * Loop over the cookies to determine if the locale is set there.
     * @param cookies The list of {@Cookie}s.
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

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        //Do nothing
    }

    @Override
    public void destroy() {
        //Do nothing
    }
}
