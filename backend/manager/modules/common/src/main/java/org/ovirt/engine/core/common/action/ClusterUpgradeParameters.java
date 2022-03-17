package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ClusterUpgradeParameters extends ClusterParametersBase {
    private static final long serialVersionUID = -9133528679053901136L;

    private String upgradeCorrelationId;
    private int upgradePercentComplete;

    public ClusterUpgradeParameters() {
    }

    public ClusterUpgradeParameters(Guid clusterId, String upgradeCorrelationId) {
        this(clusterId, upgradeCorrelationId, 0);
    }

    public ClusterUpgradeParameters(Guid clusterId, String upgradeCorrelationId, int upgradePercentComplete) {
        super(clusterId);
        this.upgradeCorrelationId = upgradeCorrelationId;
        this.upgradePercentComplete = upgradePercentComplete;
    }

    public void setUpgradeCorrelationId(String upgradeCorrelationId) {
        this.upgradeCorrelationId = upgradeCorrelationId;
    }

    public String getUpgradeCorrelationId() {
        return upgradeCorrelationId;
    }

    public void setUpgradePercentComplete(int upgradePercentComplete) {
        this.upgradePercentComplete = upgradePercentComplete;
    }

    public int getUpgradePercentComplete() {
        return upgradePercentComplete;
    }

    public String getEffectiveCorrelationid() {
        String correlationId =
            getUpgradeCorrelationId() == null || "".equals(getUpgradeCorrelationId().trim())
                ? getCorrelationId()
                : getUpgradeCorrelationId();

        return correlationId;
    }

}
