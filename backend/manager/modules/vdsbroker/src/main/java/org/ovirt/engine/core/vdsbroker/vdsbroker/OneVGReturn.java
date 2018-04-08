package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class OneVGReturn extends StatusReturn {
    private static final String INFO = "info";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    public Map<String, Object> vgInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(vgInfo, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public OneVGReturn(Map<String, Object> innerMap) {
        super(innerMap);
        vgInfo = (Map<String, Object>) innerMap.get(INFO);
    }

}
