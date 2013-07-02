package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * Contains methods used to validate VDS status to execute some operation on it
 */
public class VdsValidator {
    /**
     * Vds instance
     */
    private VDS vds;

    /**
     * Creates an instance with specified VDS
     *
     * @param vds
     *            specified VDS
     * @exception IllegalArgumentException if {@code vds} is {@code null}
     */
    public VdsValidator(VDS vds) {
        if (vds == null) {
            throw new IllegalArgumentException();
        }
        this.vds = vds;
    }

    /**
     * Determines if the VDS status is legal for execute fencing on host (either SSH Soft Fencing or real one)
     *
     * @return {@code true}, if fencing should be executed, otherwise {@code false}
     */
    public boolean shouldVdsBeFenced() {
        boolean result = false;

        switch (vds.getStatus()) {
        case Down:
        case InstallFailed:
        case Maintenance:
        case NonOperational:
        case NonResponsive:
            result = true;
            break;

        default:
            break;
        }

        return result;
    }
}
