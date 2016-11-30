package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class StorageStatusReturn extends StatusReturn {
    private static final String STORAGE_STATUS = "storageStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore),
    //  Member("storageStatus")]
    public String storageStatus;

    public StorageStatusReturn(Map<String, Object> innerMap) {
        super(innerMap);
        storageStatus = (String) innerMap.get(STORAGE_STATUS);
    }

}
