package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class CertificateInfo implements Serializable {

    public static final String SHA256_NAME = "SHA-256";
    public static final String SHA256_ALGO = "SHA256";
    private static final long serialVersionUID = 3805409159359700576L;
    private String payload;
    private String subject;
    private String issuer;
    private String sha256Fingerprint;
    private boolean selfSigned;

    public CertificateInfo() {
    }

    public CertificateInfo(String payload, String subject, String issuer, boolean selfSigned, String sha256Fingerprint) {
        this.payload = payload;
        this.subject = subject;
        this.issuer = issuer;
        this.selfSigned = selfSigned;
        this.sha256Fingerprint = sha256Fingerprint;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean getSelfSigned() {
        return selfSigned;
    }

    public void setSelfSigned(boolean selfSigned) {
        this.selfSigned = selfSigned;
    }

    public String getSHA256Fingerprint() {
        return sha256Fingerprint;
    }

    public void setSHA256Fingerprint(String sha256Fingerprint) {
        this.sha256Fingerprint = sha256Fingerprint;
    }

}
