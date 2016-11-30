package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class OvfReturn extends StatusReturn {
    private static final String OVF = "ovf";
    public String ovf;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        builder.append(ovf);
        return builder.toString();
    }

    public OvfReturn(Map<String, Object> innerMap) {
        super(innerMap);
        ovf = (String) innerMap.get(OVF);
    }
}
