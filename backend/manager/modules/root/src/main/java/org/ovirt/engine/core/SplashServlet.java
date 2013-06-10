package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;

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

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
        ServletException {
        Locale userLocale = (Locale) request.getAttribute(LocaleFilter.LOCALE);
        log.info("Detected Locale: " + userLocale.toLanguageTag());
        request.setAttribute(LOCALE_KEYS, LocaleFilter.getLocaleKeys());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/ovirt-engine.jsp");
        response.setContentType("text/html;charset=UTF-8");
        if (dispatcher != null) {
            dispatcher.include(request, response);
        }
    }

}
