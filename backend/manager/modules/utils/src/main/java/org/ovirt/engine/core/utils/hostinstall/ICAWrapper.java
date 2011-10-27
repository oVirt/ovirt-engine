package org.ovirt.engine.core.utils.hostinstall;

public interface ICAWrapper {
    boolean SignCertificateRequest(String requestFileName, int days, String signedCertificateFileName);
}
