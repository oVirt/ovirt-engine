package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class OneUuidReturn extends StatusReturn {
    private static final String UUID = "uuid";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("uuid")]
    public String uuid;

    public OneUuidReturn(Map<String, Object> innerMap) {
        super(innerMap);
        uuid = (String) innerMap.get(UUID);
    }

}
