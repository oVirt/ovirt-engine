package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;

public class PKIResources {
    private static Resource caCertificate;

    private static Resource qemuCaCertificate;

    private static Resource engineCertificate;


    public static Resource getCaCertificate() {
        if (caCertificate == null) {
            initCaCertificate();
        }
        return caCertificate;
    }

    private static synchronized void initCaCertificate() {
        if (caCertificate == null) {
            caCertificate = new Resource(
                    EngineLocalConfig.getInstance().getPKICACert(),
                    Format.X509_PEM_CA,
                    null);
        }
    }

    public static Resource getQemuCaCertificate() {
        if (qemuCaCertificate == null) {
            initQemuCaCertificate();
        }
        return qemuCaCertificate;
    }

    private static synchronized void initQemuCaCertificate() {
        if (qemuCaCertificate == null) {
            qemuCaCertificate = new Resource(
                    EngineLocalConfig.getInstance().getPKIQemuCACert(),
                    Format.X509_PEM_CA,
                    null);
        }
    }

    public static Resource getEngineCertificate() {
        if (engineCertificate == null) {
            initEngineCertificate();
        }
        return engineCertificate;
    }

    private static synchronized void initEngineCertificate() {
        if (engineCertificate == null) {
            engineCertificate = new Resource(
                    EngineLocalConfig.getInstance().getPKIEngineCert(),
                    Format.X509_PEM,
                    Config.getValue(ConfigValues.SSHKeyAlias));
        }
    }

    @FunctionalInterface
    private interface IFormatter {
        String toString(Certificate cert, String alias);
    }

    private static IFormatter formatPEM = (cert, alias) -> {
        try {
            return String.format(
                    "-----BEGIN CERTIFICATE-----%1$c" +
                    "%2$s" +
                    "-----END CERTIFICATE-----%1$c",
                '\n',
                new Base64(
                    76,
                    new byte[] { (byte)'\n' }
                ).encodeToString(
                    cert.getEncoded()
                )
            );
        } catch(CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    };

    private static IFormatter formatOpenSSH = (cert, alias) -> OpenSSHUtils.getKeyString(cert.getPublicKey(), alias);

    public enum Format {

        X509_PEM_CA("application/x-x509-ca-cert", formatPEM),
        X509_PEM("application/x-x509-cert", formatPEM),
        OPENSSH_PUBKEY("text/plain", formatOpenSSH);

        private final String contentType;
        private final IFormatter formatter;

        private Format(String contentType, IFormatter formatter) {
            this.contentType = contentType;
            this.formatter = formatter;
        }

        public String getContentType() {
            return contentType;
        }

        public String toString(Certificate cert, String alias) {
            return formatter.toString(cert, alias);
        }

        public String toString(Certificate cert) {
            return toString(cert, null);
        }
    }

    public static class Resource {
        private final Certificate cert;
        private final Format defaultFormat;
        private final String defaultAlias;

        private Resource(File cert, Format defaultFormat, String defaultAlias) {
            try (InputStream in = new FileInputStream(cert)) {
                this.cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
                this.defaultFormat = defaultFormat;
                this.defaultAlias = defaultAlias;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String toString(Format format, String alias) {
            return (format != null ? format : defaultFormat).toString(
                cert,
                alias != null ? alias : defaultAlias
            );
        }

        public String toString(Format format) {
            return toString(format, null);
        }

        public String toString() {
            return toString(null, null);
        }

        public String getContentType(Format format) {
            return (format != null ? format : defaultFormat).getContentType();
        }

        public String getContentType() {
            return getContentType(null);
        }
    }
}
