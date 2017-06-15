package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with hostnameOrIp and forceAction as parameters. <br>
 * This will be used by remove gluster server command. <br>
 */
public class RemoveGlusterServerParameters extends ActionParametersBase {
    private static final long serialVersionUID = -1224829720081853632L;

    private Guid clusterId;
    private String hostnameOrIp;
    private boolean forceAction;

    public RemoveGlusterServerParameters() {
    }

    public RemoveGlusterServerParameters(Guid clusterId, String hostnameOrIp, boolean forceAction) {
        setClusterId(clusterId);
        setHostnameOrIp(hostnameOrIp);
        setForceAction(forceAction);
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public String getHostnameOrIp() {
        return hostnameOrIp;
    }

    public void setHostnameOrIp(String hostnameOrIp) {
        this.hostnameOrIp = hostnameOrIp;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }

}
