package org.ovirt.engine.docs.utils.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.servlet.FileServlet;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;
import org.ovirt.engine.core.utils.servlet.ServletUtils;

/**
 * Serves in-product documentation for the current locale.
 */
public class DocsServlet extends FileServlet {

    private static final long serialVersionUID = 3804716423059474164L;

    public static final Pattern bookHtmlIndexPattern = Pattern.compile(".*\\/([^\\/]*)\\/(\\?.*)?");
    public static final Pattern bookPdfPattern = Pattern.compile(".*\\/([^\\/]*)\\.pdf(\\?.*)?");
    public static final Pattern bookHtmlContentPattern = Pattern.compile(".*?\\/([^\\/]*)\\/(.*\\.[a-z]*)(\\?.*)?");

    private Locale DEFAULT_US_LOCALE = Locale.US;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // limited set of acceptable URLs based on the book names/paths:
        // HTML index -- '/docs/<book_path>/'
        // HTML content -- '/docs/<book_path>/.*'
        // PDF -- '/docs/<book_path>.pdf'
        // anything else is a 404

        String url = request.getPathInfo();
        Locale locale = LocaleFilter.getLocaleFromRequest(request);

        File file = null;

        Matcher m = bookHtmlIndexPattern.matcher(url);
        if (m.matches()) {
            // book index page
            String bookPath = m.group(1);
            String defaultPath = getBookIndexPath(bookPath, locale);
            String backupPath = getBookIndexPath(bookPath, DEFAULT_US_LOCALE);
            file = getFile(request, response, defaultPath, backupPath, locale);
        } else {
            m = bookHtmlContentPattern.matcher(url);
            if (m.matches()) {
                // book content page
                String bookPath = m.group(1);
                String contentPath = m.group(2);
                String defaultPath = getBookContentPath(bookPath, contentPath, locale);
                String backupPath = getBookContentPath(bookPath, contentPath, DEFAULT_US_LOCALE);
                file = getFile(request, response, defaultPath, backupPath, locale);
            } else {
                m = bookPdfPattern.matcher(url);
                if (m.matches()) {
                    // book pdf
                    String bookPath = m.group(1);
                    String defaultPath = getBookPdfPath(bookPath, locale);
                    String backupPath = getBookPdfPath(bookPath, DEFAULT_US_LOCALE);
                    file = getFile(request, response, defaultPath, backupPath, locale);
                }
            }
        }

        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            ServletUtils.sendFile(request, response, file, type);
        }
    }

    protected File getFile(HttpServletRequest request, HttpServletResponse response, String defaultPath, String backupPath, Locale locale) {

        File file = null;

        file = ServletUtils.makeFileFromSanePath(defaultPath, base);
        if (!ServletUtils.canReadFile(file)) {
            // try English
            if (!DEFAULT_US_LOCALE.equals(locale)) {
                file = ServletUtils.makeFileFromSanePath(backupPath, base);
                if (!ServletUtils.canReadFile(file)) {
                    file = null;
                }
            }
        }

        return file;
    }

    protected String getBookIndexPath(String bookPath, Locale locale) {
        return "/" + locale.toLanguageTag() + "/" + bookPath + "/html/index.html";
    }

    protected String getBookPdfPath(String bookPath, Locale locale) {
        return "/" + locale.toLanguageTag() + "/" + bookPath + "/" + bookPath + ".pdf";
    }

    protected String getBookContentPath(String bookPath, String contentPath, Locale locale) {
        return "/" + locale.toLanguageTag() + "/" + bookPath + "/html/" + contentPath;
    }

}
