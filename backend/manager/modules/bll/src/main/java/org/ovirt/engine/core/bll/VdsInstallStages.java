package org.ovirt.engine.core.bll;

public enum VdsInstallStages {
    None,
    Start,
    ConnectToServer,
    CheckUniqueVds,
    UploadScript,
    RunScript,
    DownloadCertificateRequest,
    SignCertificateRequest,
    UploadSignedCertificate,
    UploadCA,
    FinishCommand,
    End,
    Error;

    public int getValue() {
        return this.ordinal();
    }

    public static VdsInstallStages forValue(int value) {
        return values()[value];
    }
}
