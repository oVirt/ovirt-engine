package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class CertificateInfo implements Serializable {

    private static final long serialVersionUID = 3805409159359700576L;
    private String payload;
    private String subject;
    private String issuer;
    private String sha1Fingerprint;
    private boolean selfSigned;

    public CertificateInfo() {
    }

    public CertificateInfo(String payload, String subject, String issuer, boolean selfSigned, String sha1Fingerprint) {
        this.payload = payload;
        this.subject = subject;
        this.issuer = issuer;
        this.selfSigned = selfSigned;
        this.sha1Fingerprint = sha1Fingerprint;
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

    public String getSHA1Fingerprint() {
        return sha1Fingerprint;
    }

    public void setSHA1Fingerprint(String sha1Fingerprint) {
        this.sha1Fingerprint = sha1Fingerprint;
    }

}
