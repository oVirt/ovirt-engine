package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class HotPlugDiskToVmParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = -1003552157459962546L;

    public HotPlugDiskToVmParameters(DiskVmElement diskVmElement) {
        super(diskVmElement);
    }

    public HotPlugDiskToVmParameters() {
    }
}
