package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

@SuppressWarnings("unchecked")
public final class VmInfoReturn extends StatusReturn {
    private static final String INFO = "info";

    public Map<String, Object> vmInfo;

    public VmInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        vmInfo = (Map<String, Object>) innerMap.get(INFO);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(vmInfo, builder);
        return builder.toString();
    }

    public Map<String, Object> getVmInfo() {
        return vmInfo;
    }
}
