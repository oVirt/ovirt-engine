package org.ovirt.engine.core.pki;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

/**
 * Send PKI resource.
 *
 * Init parameters:
 *
 * resource-location location of resource, currently supported X.509 Certificate.
 *
 * output-format output format (X509-PEM, SSH)
 *
 * output-alias
 */
public class PKIResourceServlet extends HttpServlet {
    // Serialization id:
    private static final long serialVersionUID = 6242595311775511209L;

    // The log:
    private static final Logger log = Logger.getLogger(PKIResourceServlet.class);

    private static final String PARAMETER_RESOURCE_LOCATION = "resource-location";
    private static final String PARAMETER_OUTPUT_FORMAT = "output-format";
    private static final String PARAMETER_OUTPUT_ALIAS = "output-alias";

    private String resourceLocation;
    private String outputFormat;
    private String outputAlias;

    @Override
    public void init() throws ServletException {
        try {
            this.resourceLocation = getInitParameter(PARAMETER_RESOURCE_LOCATION);
            this.outputFormat = getInitParameter(PARAMETER_OUTPUT_FORMAT);
            this.outputAlias = getInitParameter(PARAMETER_OUTPUT_ALIAS);

            if (this.resourceLocation == null) {
                throw new ServletException(
                    String.format(
                        "The parameter '%s' must be specified.",
                        PARAMETER_RESOURCE_LOCATION
                    )
                );
            }
            if (this.outputFormat == null) {
                throw new ServletException(
                    String.format(
                        "The parameter '%s' must be specified.",
                        PARAMETER_OUTPUT_FORMAT
                    )
                );
            }
        }
        catch(Exception e) {
            log.error("Cannot initialize", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        InputStream in = null;

        try {
            in = new FileInputStream(this.resourceLocation);

            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate certificate = cf.generateCertificate(in);

            if ("X509-PEM".equals(this.outputFormat)) {
                response.setContentType("application/x-x509-ca-cert");
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
            else if ("SSH".equals(this.outputFormat)) {
                response.setContentType("text/plain");
                // do not let println to use platform specific new line
                response.getWriter().print(
                    OpenSSHUtils.getKeyString(
                        certificate.getPublicKey(),
                        this.outputAlias
                    )
                );
            }
            else {
                throw new IllegalArgumentException("Unsupported output format " + this.outputFormat);
            }
        }
        catch(Exception e) {
            log.error(
                String.format(
                    "Cannot send public key resource '%1$s' format '%2$s'",
                    this.resourceLocation,
                    this.outputFormat
                ),
                e
            );
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
