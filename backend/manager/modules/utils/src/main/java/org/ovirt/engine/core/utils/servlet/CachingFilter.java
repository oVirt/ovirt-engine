package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Controls caching of application resources via HTTP response headers.
 * <p>
 * This filter works with following types of resources:
 * <ul>
 * <li>resources intended to be cached forever on the client ({@code cache} init-param)
 * <li>resources which always need to be checked for changes ({@code no-cache} init-param)
 * <li>resources not intended to be cached on the client at all ({@code no-store} init-param)
 * </ul>
 * <p>
 * Following table outlines cache control headers used with supported init-params:
 *
 * <blockquote>
 * <table border="1" cellpadding="5" cellspacing="0">
 * <thead>
 * <tr>
 * <th>Init-param</th>
 * <th>Cache control headers</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@code cache}</td>
 * <td>
 * <p>
 * <ul>
 * <li>{@code Expires: <nowPlusOneYear>}
 * <li>{@code Cache-Control: max-age=<oneYear>, public}
 * <li>{@code Pragma: <emptyString>}
 * <li>allow setting conditional download headers: {@code Etag}, {@code Last-Modified}
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>{@code no-cache}</td>
 * <td>
 * <p>
 * <ul>
 * <li>{@code Expires: <nowMinusOneDay>}
 * <li>{@code Cache-Control: no-cache}
 * <li>{@code Pragma: no-cache}
 * <li>allow setting conditional download headers: {@code Etag}, {@code Last-Modified}
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>{@code no-store}</td>
 * <td>
 * <p>
 * <ul>
 * <li>{@code Expires: <nowMinusOneDay>}
 * <li>{@code Cache-Control: no-cache, no-store, must-revalidate}
 * <li>{@code Pragma: no-cache}
 * <li>prevent setting conditional download headers: {@code Etag}, {@code Last-Modified}
 * </ul>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * Aside from conditional download headers, this filter prevents further modification of {@code Expires},
 * {@code Cache-Control} and {@code Pragma} headers via response wrapper.
 */
public class CachingFilter implements Filter {

    protected static final String CACHE_INIT_PARAM = "cache"; //$NON-NLS-1$
    protected static final String NO_CACHE_INIT_PARAM = "no-cache"; //$NON-NLS-1$
    protected static final String NO_STORE_INIT_PARAM = "no-store"; //$NON-NLS-1$

    protected static final String CACHE_CONTROL_HEADER = "Cache-Control"; //$NON-NLS-1$
    protected static final String EXPIRES_HEADER = "Expires"; //$NON-NLS-1$
    protected static final String PRAGMA_HEADER = "Pragma"; //$NON-NLS-1$
    protected static final String ETAG_HEADER = "Etag"; //$NON-NLS-1$
    protected static final String LAST_MODIFIED_HEADER = "Last-Modified"; //$NON-NLS-1$

    protected static final String CACHE_YEAR = "max-age=31556926, public"; //$NON-NLS-1$
    protected static final String NO_CACHE = "no-cache"; //$NON-NLS-1$
    protected static final String NO_STORE = "no-cache, no-store, must-revalidate"; //$NON-NLS-1$
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z"; //$NON-NLS-1$
    private static final String GMT = "GMT"; //$NON-NLS-1$

    private Pattern cachePattern;
    private Pattern noCachePattern;
    private Pattern noStorePattern;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (cacheFilterPatternMatches(httpRequest) && !noCacheFilterPatternMatches(httpRequest)
                && !noStoreFilterPatternMatches(httpRequest)) {
            httpResponse.setHeader(EXPIRES_HEADER, getNowPlusYearHttpDate(Calendar.getInstance()));
            httpResponse.setHeader(CACHE_CONTROL_HEADER, CACHE_YEAR);
            httpResponse.setHeader(PRAGMA_HEADER, EMPTY_STRING);
            httpResponse = getCacheHeaderResponseWrapper(httpResponse, false);
        } else if (noCacheFilterPatternMatches(httpRequest)) {
            httpResponse.setHeader(EXPIRES_HEADER, getYesterdayHttpDate(Calendar.getInstance()));
            httpResponse.setHeader(CACHE_CONTROL_HEADER, NO_CACHE);
            httpResponse.setHeader(PRAGMA_HEADER, NO_CACHE);
            httpResponse = getCacheHeaderResponseWrapper(httpResponse, false);
        } else if (noStoreFilterPatternMatches(httpRequest)) {
            httpResponse.setHeader(EXPIRES_HEADER, getYesterdayHttpDate(Calendar.getInstance()));
            httpResponse.setHeader(CACHE_CONTROL_HEADER, NO_STORE);
            httpResponse.setHeader(PRAGMA_HEADER, NO_CACHE);
            httpResponse = getCacheHeaderResponseWrapper(httpResponse, true);
        }

        chain.doFilter(request, httpResponse);
    }

    protected String getNowPlusYearHttpDate(Calendar calendar) {
        // Add a year to now.
        calendar.add(Calendar.YEAR, 1);
        // Format in the correct format.
        return formatUsLocaleGMTZone(calendar);
    }

    protected String getYesterdayHttpDate(Calendar calendar) {
        // Subtract a day, so it is in the past.
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        // Format in the correct format.
        return formatUsLocaleGMTZone(calendar);
    }

    /**
     * Format the US locale in the GMT timezone.
     * @param calendar The {@code Calender} object to format.
     * @return A {@code String} containing the formatted date.
     */
    private String formatUsLocaleGMTZone(final Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(HTTP_DATE_FORMAT,
                Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
        return dateFormat.format(calendar.getTime());
    }

    private HttpServletResponseWrapper getCacheHeaderResponseWrapper(final HttpServletResponse httpResponse,
            final boolean preventConditionalDownloadHeaders) {
        return new HttpServletResponseWrapper(httpResponse) {
            @Override
            public void setHeader(String name, String value) {
                // Prevent setting Expires, Cache-Control and Pragma headers.
                if (EXPIRES_HEADER.equalsIgnoreCase(name)
                        || CACHE_CONTROL_HEADER.equalsIgnoreCase(name)
                        || PRAGMA_HEADER.equalsIgnoreCase(name)) {
                    return;
                }

                // Prevent setting conditional download headers, if necessary.
                if (preventConditionalDownloadHeaders
                        && (ETAG_HEADER.equalsIgnoreCase(name)
                                || LAST_MODIFIED_HEADER.equalsIgnoreCase(name))) {
                    return;
                }

                httpResponse.setHeader(name, value);
            }
        };
    }

    protected boolean cacheFilterPatternMatches(HttpServletRequest httpRequest) {
        return cachePattern != null ? cachePattern.matcher(httpRequest.getRequestURI()).matches() : false;
    }

    protected boolean noCacheFilterPatternMatches(HttpServletRequest httpRequest) {
        return noCachePattern != null ? noCachePattern.matcher(httpRequest.getRequestURI()).matches() : false;
    }

    protected boolean noStoreFilterPatternMatches(HttpServletRequest httpRequest) {
        return noStorePattern != null ? noStorePattern.matcher(httpRequest.getRequestURI()).matches() : false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (cachePattern == null) {
            cachePattern = compilePatternFromInitParam(filterConfig, CACHE_INIT_PARAM);
        }
        if (noCachePattern == null) {
            noCachePattern = compilePatternFromInitParam(filterConfig, NO_CACHE_INIT_PARAM);
        }
        if (noStorePattern == null) {
            noStorePattern = compilePatternFromInitParam(filterConfig, NO_STORE_INIT_PARAM);
        }
    }

    private Pattern compilePatternFromInitParam(FilterConfig filterConfig, String name) {
        String paramValue = filterConfig.getInitParameter(name);
        return paramValue != null ? Pattern.compile(paramValue) : null;
    }

    @Override
    public void destroy() {
        // Do nothing.
    }

}
