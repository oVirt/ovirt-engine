package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;

public final class GetVmsInfoReturn extends StatusReturn {
    private static final String VM_LIST = "vmlist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    public Map<String, Object> vmlist;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(vmlist, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public GetVmsInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        vmlist = (Map<String, Object>) innerMap.get(VM_LIST);
    }
}
