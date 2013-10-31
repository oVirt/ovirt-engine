package org.ovirt.engine.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

public class PKIResourceServlet extends HttpServlet {

    private class Details {
        File file;
        String format;
        String alias;
        Details(File file, String format, String alias) {
            this.file = file;
            this.format = format;
            this.alias = alias;
        }
        Details(File file, String format) {
            this(file, format, null);
        }
    }

    private static final long serialVersionUID = 6242595311775511209L;

    private static final Logger log = Logger.getLogger(PKIResourceServlet.class);

    private Map<String, Details> pkiResources;

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
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        EngineLocalConfig localconfig = EngineLocalConfig.getInstance();
        pkiResources = new HashMap<String, Details>();
        pkiResources.put("ca-certificate", new Details(localconfig.getPKICACert(), "X509-PEM-CA"));
        pkiResources.put("engine-certificate", new Details(localconfig.getPKIEngineCert(), "X509-PEM", "ovirt-engine"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String resource = getMyParameter("resource", request);
        String format = getMyParameter("format", request);
        String alias = getMyParameter("alias", request);

        try {
            if (resource == null) {
                throw new IllegalArgumentException("Missing resource name");
            }

            Details details = pkiResources.get(resource);
            if (details == null) {
                throw new IllegalArgumentException(String.format("Resource '%1$s' is invalid", resource));
            }

            if (format == null) {
                format = details.format;
            }

            if (alias == null) {
                alias = details.alias;
            }

            try (InputStream in = new FileInputStream(details.file)) {

                final Certificate certificate =  CertificateFactory.getInstance("X.509").generateCertificate(in);

                if (format.startsWith("X509-PEM")) {
                    if (format.endsWith("-CA")) {
                        response.setContentType("application/x-x509-ca-cert");
                    }
                    else {
                        response.setContentType("application/x-x509-cert");
                    }

                    // do not let println to use platform specific new line
                    response.getWriter().print(
                        String.format(
                            (
                                "-----BEGIN CERTIFICATE-----%1$c" +
                                "%2$s" +
                                "-----END CERTIFICATE-----%1$c"
                            ),
                            '\n',
                            new Base64(
                                76,
                                new byte[] { (byte)'\n' }
                            ).encodeToString(
                                certificate.getEncoded()
                            )
                        )
                    );
                }
                else if ("OPENSSH-PUBKEY".equals(format)) {
                    response.setContentType("text/plain");
                    // do not let println to use platform specific new line
                    response.getWriter().print(
                        OpenSSHUtils.getKeyString(
                            certificate.getPublicKey(),
                            alias
                        )
                    );
                }
                else {
                    throw new IllegalArgumentException(
                        String.format(
                            "Unsupported output format '%1$s'", format
                        )
                    );
                }
            }
        }
        catch (FileNotFoundException e) {
            log.error(
                String.format(
                    "Cannot send public key resource '%1$s' format '%2$s'",
                    resource,
                    format
                ),
                e
            );
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
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
