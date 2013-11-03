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
        String alias;
        Details(File file, String alias) {
            this.file = file;
            this.alias = alias;
        }
        Details(File file) {
            this(file, null);
        }
    }

    private static final long serialVersionUID = 6242595311775511209L;

    private static final Logger log = Logger.getLogger(PKIResourceServlet.class);

    private static final String PARAMETER_RESOURCE = "resource";
    private static final String PARAMETER_FORMAT = "format";

    private String resource;
    private String format;

    private Map<String, Details> pkiResources;

    @Override
    public void init() throws ServletException {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        pkiResources = new HashMap<String, Details>();
        pkiResources.put("ca-certificate", new Details(config.getPKICACert()));
        pkiResources.put("engine-certificate", new Details(config.getPKIEngineCert(), "ovirt-engine"));

        resource = getInitParameter(PARAMETER_RESOURCE);
        format = getInitParameter(PARAMETER_FORMAT);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String localResource = (String)request.getAttribute(PARAMETER_RESOURCE);
        String localFormat = (String)request.getAttribute(PARAMETER_FORMAT);

        try {
            if (localResource == null) {
                localResource = resource;
            }
            if (localResource == null) {
                throw new IllegalArgumentException("Missing resource name");
            }

            Details details = pkiResources.get(localResource);
            if (details == null) {
                throw new IllegalArgumentException(String.format("Resource %1$s is invalid", localResource));
            }

            if (localFormat == null) {
                localFormat = format;
            }
            if (localFormat == null) {
                throw new IllegalArgumentException("Missing format");
            }

            try (InputStream in = new FileInputStream(details.file)) {

                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                final Certificate certificate = cf.generateCertificate(in);

                if (localFormat.startsWith("X509-PEM")) {
                    if (localFormat.endsWith("-CA")) {
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
                else if ("OPENSSH-PUBKEY".equals(localFormat)) {
                    response.setContentType("text/plain");
                    // do not let println to use platform specific new line
                    response.getWriter().print(
                        OpenSSHUtils.getKeyString(
                            certificate.getPublicKey(),
                            details.alias
                        )
                    );
                }
                else {
                    throw new IllegalArgumentException(
                        String.format(
                            "Unsupported output format %1$s", localFormat
                        )
                    );
                }
            }
        }
        catch (FileNotFoundException e) {
            log.error(
                String.format(
                    "Cannot send public key resource '%1$s' format '%2$s'",
                    localResource,
                    localFormat
                ),
                e
            );
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        catch(Exception e) {
            log.error(
                String.format(
                    "Cannot send public key resource '%1$s' format '%2$s'",
                    localResource,
                    localFormat
                ),
                e
            );
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
