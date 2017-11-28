package org.ovirt.engine.core.utils.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves locale dependent documents. It takes the current selected locale
 * and finds the appropriate file based on that locale and the file requested and
 * returns it the browser.
 */
public class DocsServlet extends FileServlet {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(FileServlet.class);

    private static final long serialVersionUID = 3804716423059474163L;

    public static final String REFERER = "Referer";
    public static final String LANG_PAGE_SHOWN = "langPageShown";

    private static final String ENGLISH_HREF = "englishHref";
    private static final String LOCALE_DOCS_MISSING = "localeDocsMissingURI";

    private String localeDocsMissing;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        // Let the parent do its work:
        super.init(config);
        localeDocsMissing = config.getInitParameter(LOCALE_DOCS_MISSING);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Locate the requested file:
        File originalFile = ServletUtils.makeFileFromSanePath(request.getPathInfo(), base);
        Locale locale = getLocaleFromRequest(request);
        File file = determineActualFile(request, locale);
        file = checkForIndex(request, response, file, request.getPathInfo());
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (!response.isCommitted()) { //If the response is committed, we have already redirected.
            boolean languagePageShown = isLangPageShown(request);
            if (!file.equals(originalFile)
                    && !file.getAbsolutePath().equals(replaceLocaleWithOtherLocale(originalFile.getAbsolutePath(),
                            locale, locale))) {
                //We determined that we are going to redirect the user to the English version URI.
                String redirect = getServletContext().getContextPath() + request.getServletPath()
                        + replaceLocaleWithUSLocale(request.getPathInfo(), locale);
                if (!languagePageShown) {
                    setLangPageShown(request, response, true);
                    request.setAttribute(LocaleFilter.LOCALE, locale);
                    request.setAttribute(ENGLISH_HREF, redirect);
                    final ServletContext forwardContext = getServletContext();
                    if (forwardContext != null) {
                        final RequestDispatcher dispatcher = forwardContext.getRequestDispatcher(localeDocsMissing);
                        if (dispatcher != null) {
                            dispatcher.forward(request, response);
                        } else {
                            log.error("Unable to determine dispatcher");
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "unable to determine dispatcher");
                        }
                    } else {
                        log.error("Unable to determine forwarding context");
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "unable to determine forward context");
                    }
                } else {
                    //Redirect to English version of the document
                    response.sendRedirect(redirect);
                }
            } else {
                // Send the content of the file:
                // type is the default MIME type of the Servlet(passed in through WebInit parameter).
                ServletUtils.sendFile(request, response, file, type);
            }
        }
    }

    private boolean isLangPageShown(HttpServletRequest request) {
        boolean result = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (LANG_PAGE_SHOWN.equalsIgnoreCase(cookie.getName())) {
                    result = Boolean.parseBoolean(cookie.getValue());
                    break;
                }
            }
        }
        return result;
    }

    private void setLangPageShown(HttpServletRequest request, HttpServletResponse response, boolean value) {
        Cookie cookie = new Cookie(LANG_PAGE_SHOWN, Boolean.toString(value));
        cookie.setSecure("https".equalsIgnoreCase(request.getScheme()));
        // Scope this cookie to the (root) application context URL
        cookie.setPath(getServletContext().getContextPath());
        cookie.setHttpOnly(true);
        // Don't set max age, i.e. let this be a session cookie
        response.addCookie(cookie);
    }

    /**
     * Determine the actual file based on the passed in {@code HttpServletRequest} path and
     * the passed in {@code Locale}. If the Locale is invalid or cannot be found, use the
     * default (EN_US) locale.
     * Returns null if the file cannot be found.
     *
     * @param request The {@code HttpServletRequest} containing the path.
     * @param locale The {@code Locale} to search for.
     * @return A {@code File} object pointing to the requested file.
     */
    protected File determineActualFile(final HttpServletRequest request, Locale locale) {
        // webapp asks for files in xx_XX locale, but the directory has a - not a _ . Convert.
        File file = ServletUtils.makeFileFromSanePath(replaceLocaleWithOtherLocale(request.getPathInfo(), locale, locale), base);

        // Check if file is found. If not found go ahead and try and look up the English US locale version.
        if (file != null && !ServletUtils.canReadFile(file)) {
            file = ServletUtils.makeFileFromSanePath(replaceLocaleWithUSLocale(request.getPathInfo(), locale), base);
        }
        return file;
    }

    private String replaceLocaleWithUSLocale(String originalString, Locale locale) {
        return replaceLocaleWithOtherLocale(originalString != null ? originalString : "", //$NON-NLS-1$
                locale, Locale.US);
    }

    private String replaceLocaleWithOtherLocale(String originalString, Locale searchLocale, Locale targetLocale) {
        //Create regex to match either the toString() or toLanguageTag() version of the locale
        //For US Locale this means: /en\-US|/en_US
        //For Brazil this means: /pt\-BR|/pt_BR
        //For Japan this means: /ja|/ja (yes I know its the same).
        String regex = "/"+ searchLocale.toLanguageTag().replaceAll("-", "\\\\-") + "|/" + searchLocale.toString();
        //This will match for instance '/pt-BR/something' and turn it into '/en-US/something', but
        //it will also match '/pt_BR/something' and turn it into '/en-US/something' if targetLocale is en_US.
        return originalString.replaceAll(regex, "/" + targetLocale.toLanguageTag());
    }
    /**
     * Determines the locale based on the request passed in. It will first try to determine the locale
     * from the referer URI passed. If it can't determine the Locale, it will attempt to use the pathinfo
     * of the request. If that fails it defaults back to the US locale.
     * @param request The request to use to determine the locale.
     * @return A {@code Locale}
     */
    protected Locale getLocaleFromRequest(final HttpServletRequest request) {
        String localeString = getLocaleStringFromReferer(request);
        //Unable to determine locale string from referer (preferred way)
        if (localeString == null) {
            //Note this fails if the is something like /menu.css
            localeString = getLocaleStringFromPath(request.getPathInfo());
        }
        //Validate that the locale string is valid.
        Locale locale = LocaleUtils.getLocaleFromString(localeString, true);
        return locale;
    }

    /**
     * Get the locale string from the path.
     * Assumption the path format is the following:
     * /&lt;locale&gt;/stuff
     * @param path The path to get the locale from.
     * @return The locale string stripped from the path. If the base +
     * the found locale is a file and not a directory, return null as the locale is a filename.
     */
    protected String getLocaleStringFromPath(final String path) {
        String result = null;
        if (path != null) {
            if (!path.startsWith("/")) {
                log.warn("Path should start with a '/'");
                return null;
            }
            //Attempt to determine locale from path info.
            String[] pathElements = path.substring(1).split("/");
            File localeFile = new File(base, pathElements[0]);
            //Check to make sure the file doesn't exist, and if it does, that it is a directory.
            //This excludes anything like /docs/menu.css
            if (!localeFile.exists() || localeFile.isDirectory()) {
                result = pathElements[0];
            }
        }
        return result;
    }

    /**
     * Attempt to determine the locale from the referer URL. The referer must have a parameter that looks like this:<br>
     * ?locale=&lt;locale string&gt;<br>
     * for instance the following will return 'fr'<br>
     * http://127.0.0.1:8700/webadmin/webadmin/WebAdmin.html?locale=fr<br>
     * if there is no referer or the referer does not have a 'locale' parameter described, the result will be null.<br>
     * @param request The {@code HttpServletRequest} that has the referer header.
     * @return The referer parameter locale, null otherwise
     */
    protected String getLocaleStringFromReferer(final HttpServletRequest request) {
        // Determine the local passed in. To do this check the referer
        final URI refererURL;
        String result = null;
        try {
            String referer = request.getHeader(REFERER);
            if (referer != null) {
                refererURL = new URI(referer);
                String query = refererURL.getQuery();
                if (query != null) {
                    String[] parameters = query.split("&");
                    for (int i = 0; i < parameters.length; i++) {
                        String[] keyValues = parameters[i].split("=");
                        if (LocaleFilter.LOCALE.equalsIgnoreCase(keyValues[0])) {
                            result = keyValues[1];
                            break;
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            log.error("Unable to determine referer URI", e);
        }
        return result;
    }
}
