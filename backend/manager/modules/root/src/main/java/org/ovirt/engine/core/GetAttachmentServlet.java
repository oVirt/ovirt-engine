package org.ovirt.engine.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;

public class GetAttachmentServlet extends HttpServlet {

    private static final long serialVersionUID = 8496520437603585173L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contentType = request.getParameter("contenttype");
        String fileName = request.getParameter("filename");
        Boolean cache = Boolean.parseBoolean(request.getParameter("cache"));
        String encodingType = request.getParameter("encodingtype");
        String content = request.getParameter("content");

        if (contentType != null) {
            response.setContentType(contentType);
        }

        if (fileName == null) {
            fileName = "attachment";
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        if (!cache) {
            response.setHeader("Cache-Control", "no-cache, must-revalidate"); //disable caching HTTP/1.1
            response.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT"); //disable caching HTTP/1.0
        }

        if (content == null) {
            return;
        }

        if ("binary".equals(encodingType)) {
            response.getOutputStream().write(Base64.decodeBase64(content));
        } else if ("plain".equals(encodingType)) {
            content = StringEscapeUtils.unescapeHtml(content);
            response.getWriter().write(content);

            if (response.getWriter().checkError()) {
                throw new IOException("Error when writing to response stream");
            }
        } else {
            throw new ServletException(String.format("Unsupported encoding type {0}", encodingType));
        }
    }
}
