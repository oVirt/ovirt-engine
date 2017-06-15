package org.ovirt.engine.core.services;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;

public class GetAttachmentServlet extends HttpServlet {

    private static final long serialVersionUID = 8496520437603585173L;

    private static final String ENCODINGTYPE_BINARY = "binary";
    private static final Object ENCODINGTYPE_PLAIN = "plain";

    /**
     * A list of regular expressions containing the acceptable values for the content type. Currently the only
     * acceptable content types for this servlet are @{code application/x-virt-viewer} and @{code application/rdp},
     * refrain from adding new content types unless strictly necessary.
     */
    private static final List<Pattern> ACCEPTABLE_CONTENT_TYPES = Arrays.asList(
        Pattern.compile("^application/(x-virt-viewer|rdp)(;.*)?$", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Checks if the given content type is acceptable.
     */
    private static boolean isAcceptableContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (Pattern acceptableContentType : ACCEPTABLE_CONTENT_TYPES) {
            if (acceptableContentType.matcher(contentType).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String contentType = request.getParameter("contenttype");
        Boolean cache = Boolean.parseBoolean(request.getParameter("cache"));
        String encodingType = request.getParameter("encodingtype");
        String content = request.getParameter("content");

        // Check if the content type is acceptable, and copy it to the request:
        if (!isAcceptableContentType(contentType)) {
            throw new ServletException(String.format("Unsupported content type '%s'", contentType));
        }
        response.setContentType(contentType);

        if (!cache) {
            response.setHeader("Cache-Control", "max-age=0, must-revalidate"); //disable caching HTTP/1.1
            response.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT"); //disable caching HTTP/1.0
        }

        if (content == null) {
            return;
        }

        if (ENCODINGTYPE_BINARY.equals(encodingType)) {
            response.getOutputStream().write(Base64.decodeBase64(content));
        } else if (ENCODINGTYPE_PLAIN.equals(encodingType)) {
            content = URLDecoder.decode(content, "UTF-8");
            content = StringEscapeUtils.unescapeHtml(content);

            response.getWriter().write(content);

            if (response.getWriter().checkError()) {
                throw new IOException("Error when writing to response stream");
            }
        } else {
            throw new ServletException(String.format("Unsupported encoding type %s", encodingType));
        }
    }
}
