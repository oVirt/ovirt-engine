package org.ovirt.engine.core.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.ovirt.engine.core.utils.PKIResources;

public class PKIResourceServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(PKIResourceServlet.class);

    private static Map<String, PKIResources.Resource> resources;
    private static Map<String, PKIResources.OutputType> formats;
    private transient PKIResources pkiResources;

    static {
        resources = new HashMap<String, PKIResources.Resource>();
        resources.put("ca-certificate", PKIResources.Resource.CACertificate);
        resources.put("engine-certificate", PKIResources.Resource.EngineCertificate);
        formats = new HashMap<String, PKIResources.OutputType>();
        formats.put("X509-PEM", PKIResources.OutputType.X509_PEM);
        formats.put("X509-PEM-CA", PKIResources.OutputType.X509_PEM_CA);
        formats.put("OPENSSH-PUBKEY", PKIResources.OutputType.OPENSSH_PUBKEY);
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
        pkiResources = PKIResources.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PKIResources.OutputType outputType = null;

        String resource = getMyParameter("resource", request);
        String format = getMyParameter("format", request);
        String alias = getMyParameter("alias", request);

        try {
            if (resource == null) {
                throw new IllegalArgumentException("Missing resource name");
            }

            PKIResources.Resource r = resources.get(resource);
            if (r == null) {
                throw new IllegalArgumentException(String.format("Resource '%1$s' is invalid", resource));
            }

            if (format != null) {
                outputType = formats.get(format);
                if (outputType == null) {
                    throw new IllegalArgumentException(String.format("Format '%1$s' is invalid", format));
                }
            }

            try (PrintWriter out = response.getWriter()) {
                response.setContentType(pkiResources.getContentType(r, outputType));
                out.print(pkiResources.getAsString(r, outputType, alias));
            }
        }
        catch(Exception e) {
            log.error(
                String.format(
                    "Cannot send public key resource '%1$s' format '%2$s'",
                    resource,
                    format
                ),
                e
            );
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
