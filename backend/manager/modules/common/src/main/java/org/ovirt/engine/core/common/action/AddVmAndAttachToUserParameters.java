package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmAndAttachToUserParameters extends AddVmFromScratchParameters {
    private static final long serialVersionUID = -5240624372284576720L;

    private boolean _fromScratch;
    private Guid _userId = Guid.Empty;

    public AddVmAndAttachToUserParameters(VmStatic vmStatic, ArrayList<DiskImage> diskInfoList,
            boolean fromScratch, Guid userId, Guid storageDomainId) {
        super(vmStatic, diskInfoList, storageDomainId);
        _fromScratch = fromScratch;
        _userId = userId;
    }

    public boolean getFromScratch() {
        return _fromScratch;
    }

    public Guid getUserId() {
        return _userId;
    }

    public AddVmAndAttachToUserParameters() {
    }
}
