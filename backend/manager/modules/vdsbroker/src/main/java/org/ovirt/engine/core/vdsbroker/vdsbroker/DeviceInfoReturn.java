package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class DeviceInfoReturn extends StatusReturn {
    public static final String PATH = "path";
    public static final String TYPE = "type";
    public static final String SCSI_WWN = "scsi_wwn";
    public static final String ATTACHMENT = "attachment";

    private Map<String, Object> deviceInfo;

    public DeviceInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        deviceInfo = (Map<String, Object>) innerMap.get("info");
    }

    public Map<String, Object> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(Map<String, Object> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceInfoReturn)) {
            return false;
        }
        DeviceInfoReturn that = (DeviceInfoReturn) o;
        return Objects.equals(deviceInfo, that.deviceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceInfo);
    }
}
