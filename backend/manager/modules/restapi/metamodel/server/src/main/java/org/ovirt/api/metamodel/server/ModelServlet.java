/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.api.metamodel.server;

import static org.ovirt.api.metamodel.server.MimeTypes.parseMimeType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.MimeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet provides access to the description of the model, in its original source form and also to the generated
 * XML and JSON descriptions. The environment where this servlet is placed must have the {@code model.jar},
 * {@code model.xml} and {@code model.json} files available for the context class loader. The servlet will ignore
 * the content of the request path, except the optional extension.
 */
public class ModelServlet extends HttpServlet {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(ModelServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Decide what is the requested MIME type:
        MimeType requestType = getRequestType(request);
        if (requestType == null) {
            requestType = MimeTypes.APPLICATION_XML;
        }

        // Calculate the name of the resource:
        String resourceName = "model.";
        MimeType resourceType;
        if (requestType.match(MimeTypes.APPLICATION_XML)) {
            resourceName += "xml";
            resourceType = MimeTypes.APPLICATION_XML;
        }
        else if (requestType.match(MimeTypes.APPLICATION_JSON)) {
            resourceName += "json";
            resourceType = MimeTypes.APPLICATION_JSON;
        }
        else if (requestType.match(MimeTypes.APPLICATION_OCTET_STREAM)) {
            resourceName += "jar";
            resourceType = MimeTypes.APPLICATION_OCTET_STREAM;
        }
        else {
            log.warn("Can't calculate resource name for MIME type \"{}\", will return a 404 response.", requestType);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // The resource isn't part of the .jar of the of the servlet, but of some other part of the web application or
        // one of its dependencies. This means that we can't use the class loader of the servlet to locate the resource,
        // we need to use the context class loader instead.
        ClassLoader resourceLoader = Thread.currentThread().getContextClassLoader();

        // Try to find the resource:
        String resourcePath = "/" + resourceName;
        InputStream resourceIn = resourceLoader.getResourceAsStream(resourcePath);
        if (resourceIn == null) {
            log.warn("Can't find resource \"{}\", will return a 404 response.", resourcePath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Send the resource body:
        response.setContentType(resourceType.toString());
        if (requestType.match(MimeTypes.APPLICATION_OCTET_STREAM)) {
            response.setHeader("Content-Disposition", "attachment; filename=" + resourceName);
        }
        OutputStream resourceOut = response.getOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = resourceIn.read(buffer)) != -1) {
                resourceOut.write(buffer, 0, count);
            }
        }
        finally {
            resourceIn.close();
        }
    }

    /**
     * Decides what is the requested content type, using first the value of the {@code accept} query parameter, then the
     * extension of request, and finally the value of the {@code Accept} header.
     *
     * @param request the HTTP request to extract the type from
     * @return the MIME type or {@code null} if it can't be determined
     */
    private MimeType getRequestType(HttpServletRequest request) throws ServletException {
        // Try the query parameter:
        String parameter = request.getParameter("accept");
        if (parameter != null) {
            MimeType type = parseMimeType(parameter);
            if (type != null) {
                return type;
            }
        }

        // Try the extension:
        String path = request.getRequestURI();
        if (path != null) {
            int last = path.lastIndexOf('.');
            if (last != -1) {
                String extension = path.substring(last + 1).toLowerCase();
                switch (extension) {
                case "xml":
                    return MimeTypes.APPLICATION_JSON;
                case "json":
                    return MimeTypes.APPLICATION_JSON;
                case "jar":
                    return MimeTypes.APPLICATION_OCTET_STREAM;
                }
            }
        }

        // Try the header:
        String header = request.getContentType();
        if (header != null) {
            MimeType type = parseMimeType(header);
            if (type != null) {
                return type;
            }
        }

        // No luck:
        return null;
    }
}
