package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

public class PKIResources {

    public static enum Resource {
        CACertificate,
        EngineCertificate
    }

    public static enum OutputType {
        X509_PEM_CA,
        X509_PEM,
        OPENSSH_PUBKEY
    }

    private class Details {
        Certificate cert;
        OutputType outputType;
        String alias;
        Details(File file, OutputType outputType, String alias) {
            try (InputStream in = new FileInputStream(file)) {
                this.cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
                this.outputType = outputType;
                this.alias = alias;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Details(File file, OutputType outputType) {
            this(file, outputType, null);
        }
    }

    private static volatile PKIResources instance;
    private Map<Resource, Details> resources;

    private PKIResources() {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        resources = new HashMap<Resource, Details>();
        resources.put(Resource.CACertificate, new Details(config.getPKICACert(), OutputType.X509_PEM_CA));
        resources.put(Resource.EngineCertificate, new Details(config.getPKIEngineCert(), OutputType.X509_PEM, "ovirt-engine"));
    }

    public static PKIResources getInstance() {
        if (instance == null) {
            synchronized(PKIResources.class) {
                if (instance == null) {
                    instance = new PKIResources();
                }
            }
        }
        return instance;
    }

    public String getAsString(Resource resource, OutputType outputType, String alias) {
        try {
            String ret;

            Details details = resources.get(resource);
            if (details == null) {
                throw new IllegalArgumentException("Invalid resource");
            }

            switch (outputType != null ? outputType : details.outputType) {
                default:
                    throw new RuntimeException("Invalid output type");

                case X509_PEM:
                case X509_PEM_CA:
                    ret = String.format(
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
                            details.cert.getEncoded()
                        )
                    );
                break;
                case OPENSSH_PUBKEY:
                    ret = OpenSSHUtils.getKeyString(
                        details.cert.getPublicKey(),
                        alias != null ? alias : details.alias
                    );
                break;
            }

            return ret;
        }
        catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAsString(Resource resource, OutputType outputType) {
        return getAsString(resource, outputType, null);
    }

    public String getContentType(Resource resource, OutputType outputType) {
        String ret;

        Details details = resources.get(resource);
        if (details == null) {
            throw new IllegalArgumentException("Invalid resource");
        }

        switch (outputType != null ? outputType : details.outputType) {
            default:
                throw new RuntimeException("Invalid output type");

            case X509_PEM:
                ret = "application/x-x509-cert";
            break;
            case X509_PEM_CA:
                ret = "application/x-x509-ca-cert";
            break;
            case OPENSSH_PUBKEY:
                ret = "text/plain";
            break;
        }

        return ret;
    }

    public String getContentType(Resource resource) {
        return getContentType(resource, null);
    }

    public void setHttpResponse(HttpServletResponse response, Resource resource, OutputType outputType, String alias) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            response.setContentType(getContentType(resource, outputType));
            out.print(getAsString(resource, outputType, alias));
        }
    }

    public void setHttpResponse(HttpServletResponse response, Resource resource, OutputType outputType) throws IOException {
        setHttpResponse(response, resource, outputType, null);
    }

    public void setHttpResponse(HttpServletResponse response, Resource resource) throws IOException {
        setHttpResponse(response, resource, null, null);
    }
}
