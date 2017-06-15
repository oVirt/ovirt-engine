package org.ovirt.engine.ui.common.presenter.popup.numa;

import org.ovirt.engine.core.compat.Guid;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class UpdatedVnuma {
    /**
     * The {@code Guid} of the VM that contains the virtual NUMA node.
     */
    Guid sourceVmGuid;

    /**
     * Is the VM pinned.
     */
    boolean pinned;

    /**
     * The index of the virtual NUMA node on the VM.
     */
    int sourceVNumaNodeIndex;

    /**
     * The target physical NUMA node, -1 means unassigned.
     */
    int targetNumaNodeIndex;
}
