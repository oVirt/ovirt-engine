package org.ovirt.engine.ui.uicommonweb;

@SuppressWarnings("unused")
public enum ValidateServerCertificateEnum {
    // Validate server certificate.
    TRUE,
    // Don't validate server certificate.
    FALSE,
    // Validate server certificate only if we browse in ssl.
    AUTO;

    public int getValue() {
        return this.ordinal();
    }

    public static ValidateServerCertificateEnum forValue(int value) {
        return values()[value];
    }
}
