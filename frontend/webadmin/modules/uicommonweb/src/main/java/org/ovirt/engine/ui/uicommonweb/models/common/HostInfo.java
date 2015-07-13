package org.ovirt.engine.ui.uicommonweb.models.common;

@SuppressWarnings("unused")
public class HostInfo {
    private String privateHostName;

    public String getHostName() {
        return privateHostName;
    }

    public void setHostName(String value) {
        privateHostName = value;
    }

    private String privateOSVersion;

    public String getOSVersion() {
        return privateOSVersion;
    }

    public void setOSVersion(String value) {
        privateOSVersion = value;
    }

    private String privateVDSMVersion;

    public String getVDSMVersion() {
        return privateVDSMVersion;
    }

    public void setVDSMVersion(String value) {
        privateVDSMVersion = value;
    }
}
