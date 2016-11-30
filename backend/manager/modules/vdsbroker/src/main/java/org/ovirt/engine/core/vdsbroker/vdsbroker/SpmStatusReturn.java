package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class SpmStatusReturn extends StatusReturn {
    private static final String SPM_STATUS = "spm_st";
    // [MissingMapping(MappingAction.Ignore), Member("spm_st")]
    public Map<String, Object> spmStatus;

    @SuppressWarnings("unchecked")
    public SpmStatusReturn(Map<String, Object> innerMap) {
        super(innerMap);
        spmStatus = (Map<String, Object>) innerMap.get(SPM_STATUS);
    }

}
