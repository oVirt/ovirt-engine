package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;

public class GlusterServiceParameters extends ActionParametersBase {

    private static final long serialVersionUID = 8706812640906006229L;

    private Guid serverId;
    private Guid clusterId;
    private ServiceType serviceType;
    private String actionType;

    public GlusterServiceParameters() {
    }

    public GlusterServiceParameters(Guid clusterId, Guid serverId, ServiceType serviceType, String actionType) {
        this.clusterId = clusterId;
        this.serverId = serverId;
        this.serviceType = serviceType;
        this.actionType = actionType;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid serverId) {
        this.serverId = serverId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
