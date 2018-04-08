package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

@SuppressWarnings("unchecked")
public final class OneStorageDomainInfoReturn extends StatusReturn {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("info")]
    public Map<String, Object> storageInfo;

    public OneStorageDomainInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        storageInfo = (Map<String, Object>) innerMap.get(INFO);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(storageInfo, builder);
        return builder.toString();
    }
}
