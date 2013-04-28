package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This Servlet serves the splash page to allow users to select either web admin or user portal.
 */
@WebServlet(value = "/ovirt-engine")
public class SplashServlet extends HttpServlet {
    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(SplashServlet.class);

    /**
     * Generated UID.
     */
    private static final long serialVersionUID = 8289914264581273721L;

    /**
     * The request attribute keys describing the available locales.
     */
    private static final String LOCALE_KEYS = "localeKeys";

    /**
     * The extension of the properties file containing the available languages.
     */
    private static final String LANGUAGES = ".languages"; //the resource bundle name

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        Locale userLocale = (Locale) request.getAttribute(LocaleFilter.LOCALE);
        log.info("Detected Locale: " + userLocale.toLanguageTag());
        request.setAttribute(LOCALE_KEYS, getLocaleKeys());
        setCookie(response, userLocale);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp");
        response.setContentType("text/html;charset=UTF-8");
        if (dispatcher != null) {
            dispatcher.include(request, response);
        }
    }

    /**
     * Add the {@code Locale} cookie to the response.
     * @param response The {@code HttpServletResponse}
     * @param userLocale The {@code Locale} to put in the cookie.
     */
    private void setCookie(final HttpServletResponse response, final Locale userLocale) {
        //Detected locale doesn't match the default locale, set a cookie.
        Cookie cookie = new Cookie(LocaleFilter.LOCALE, userLocale.toString());
        cookie.setMaxAge(Integer.MAX_VALUE); //Doesn't expire.
        response.addCookie(cookie);
    }

    /**
     * Get a list of available locales.
     * @return A {@code List} of Locale strings.
     */
    private List<String> getLocaleKeys() {
        ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getPackage().getName() + LANGUAGES, Locale.US);
        List<String> keys = Collections.list(bundle.getKeys());
        //Can't just return the unsorted list, it won't match the GWT output.
        //Apparently GWT does a sort before showing the list as well, which
        //works out nicely for me.
        Collections.sort(keys);
        return keys;
    }
}
