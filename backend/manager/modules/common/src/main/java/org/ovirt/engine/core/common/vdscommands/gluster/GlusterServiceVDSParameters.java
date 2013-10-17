package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterServiceVDSParameters extends VdsIdVDSCommandParametersBase {
    private List<String> serviceList;
    private String actionType;

    public GlusterServiceVDSParameters(Guid serverId, List<String> serviceList, String actionType) {
        super(serverId);
        this.serviceList = serviceList;
        this.actionType = actionType;
    }

    public GlusterServiceVDSParameters() {
    }

    public List<String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<String> serviceList) {
        this.serviceList = serviceList;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
