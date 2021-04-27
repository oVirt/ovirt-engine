package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

public class ReplaceHostConfiguration implements  Serializable {

    private ReplaceHostConfiguration.Action deployAction;

    public ReplaceHostConfiguration() {

    }

    public ReplaceHostConfiguration(ReplaceHostConfiguration.Action deployAction) {
        Objects.requireNonNull(deployAction);
        this.deployAction = deployAction;
    }

    public ReplaceHostConfiguration.Action getDeployAction() {
        return ReplaceHostConfiguration.Action.NONE;
    }

    /**
     * The various operation the install protocol supports.
     * See {@link ReplaceHostConfiguration.Action#NONE}, {@link ReplaceHostConfiguration.Action#SAMEFQDN}, {@link ReplaceHostConfiguration.Action#DIFFERENTFQDN}
     */
    public enum Action {
        /**
         * Leave the components untouched
         */
        NONE,
        /**
         * Replace Host with same FQDN.
         */
        SAMEFQDN,
        /**
         * Replace Host with another host of different fqdn.
         */
        DIFFERENTFQDN;
    }
}
