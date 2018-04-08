package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class DevicesVisibilityMapReturn extends StatusReturn {

    private static final String DEVICES_VISIBILITY = "visible";

    private Map<String, String> devicesVisibilityResult;

    @SuppressWarnings("unchecked")
    public DevicesVisibilityMapReturn(Map<String, Object> innerMap) {
        super(innerMap);
        devicesVisibilityResult = (Map<String, String>)innerMap.get(DEVICES_VISIBILITY);
    }

    public Map<String, String> getDevicesVisibilityResult() {
        return devicesVisibilityResult;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(devicesVisibilityResult, builder);
        return builder.toString();
    }

}
