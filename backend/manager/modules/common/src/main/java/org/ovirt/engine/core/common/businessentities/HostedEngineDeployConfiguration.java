package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

public class HostedEngineDeployConfiguration implements Serializable {

    private Action deployAction;

    public HostedEngineDeployConfiguration() {
    }

    public HostedEngineDeployConfiguration(Action deployAction) {
        Objects.requireNonNull(deployAction);
        this.deployAction = deployAction;
    }

    public Action getDeployAction() {
        return deployAction;
    }

    /**
     * The various operation the install protocol supports.
     * See {@link Action#NONE}, {@link Action#DEPLOY}, {@link Action#UNDEPLOY}
     */
    public enum Action {
        /**
         * Leave the components untouched
         */
        NONE,
        /**
         * Deploy the hosted engine components on the host.
         * The hosted engine installation must already be up & running by some other host.
         */
        DEPLOY,
        /**
         * Undeploy or decommission a host from being a part of the hosted engine High Availability cluster.
         */
        UNDEPLOY;
    }
}
