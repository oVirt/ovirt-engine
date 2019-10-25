package org.ovirt.engine.core.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.utils.PKIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKIResourceServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PKIResourceServlet.class);

    private static boolean resourcesInitialized = false;

    private static Map<String, PKIResources.Resource> resources;
    private static Map<String, PKIResources.Format> formats;

    private static synchronized void initResources() {
        if (!resourcesInitialized) {
            resources = new HashMap<>();
            resources.put("ca-certificate", PKIResources.getCaCertificate());
            resources.put("qemu-ca-certificate", PKIResources.getQemuCaCertificate());
            resources.put("engine-certificate", PKIResources.getEngineCertificate());
            formats = new HashMap<>();
            formats.put("X509-PEM", PKIResources.Format.X509_PEM);
            formats.put("X509-PEM-CA", PKIResources.Format.X509_PEM_CA);
            formats.put("OPENSSH-PUBKEY", PKIResources.Format.OPENSSH_PUBKEY);
            resourcesInitialized = true;
        }
    }

    private String getMyParameter(String name, HttpServletRequest request) {
        String value;

        value = request.getParameter(name);
        if (value == null) {
            value = (String)request.getAttribute(name);
        }
        if (value == null) {
            value = getInitParameter(name);
        }
        return value;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (!resourcesInitialized) {
            try {
                initResources();
            } catch (NullPointerException ex) {
                throw new ServletException(
                        "Certificate is not available yet, as engine startup has not yet finished",
                        ex);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String resourceStr = getMyParameter("resource", request);
        String formatStr = getMyParameter("format", request);
        String alias = getMyParameter("alias", request);

        try {
            if (resourceStr == null) {
                throw new IllegalArgumentException("Missing resource name");
            }

            PKIResources.Resource resource = resources.get(resourceStr);
            if (resource == null) {
                throw new IllegalArgumentException(String.format("Resource '%1$s' is invalid", resourceStr));
            }

            PKIResources.Format format = null;
            if (formatStr != null) {
                format = formats.get(formatStr);
                if (format == null) {
                    throw new IllegalArgumentException(String.format("Format '%1$s' is invalid", formatStr));
                }
            }

            try (PrintWriter out = response.getWriter()) {
                response.setContentType(resource.getContentType(format));
                out.print(resource.toString(format, alias));
            }
        } catch(Exception e) {
            log.error("Cannot send public key resource '{}' format '{}': {}", resourceStr, formatStr, e.getMessage());
            log.debug("Exception", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
