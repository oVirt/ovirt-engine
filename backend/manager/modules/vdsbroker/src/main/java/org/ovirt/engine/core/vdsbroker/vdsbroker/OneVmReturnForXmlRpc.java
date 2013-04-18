package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class OneVmReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";

    public StatusForXmlRpc mStatus;
    public Map<String, Object> mVm;

    public OneVmReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        mVm = (Map<String, Object>) innerMap.get(VM_LIST);
    }
}
