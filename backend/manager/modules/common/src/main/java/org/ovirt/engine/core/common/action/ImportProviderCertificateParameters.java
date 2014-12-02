package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Provider;

public class ImportProviderCertificateParameters extends ProviderParameters {

    /**
     *
     */
    private static final long serialVersionUID = 2158065009589899085L;
    private String certificate;

    public ImportProviderCertificateParameters(final Provider<?> provider,
            final String certificate) {
        super(provider);
        this.certificate = certificate;
    }

    public ImportProviderCertificateParameters() {
    }

    /**
     * Gets the base64 encoding for the certificate
     * @return encoded certificate
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Sets base64 encoding of the certificate
     * @param encoded certificate
     */
    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

}
