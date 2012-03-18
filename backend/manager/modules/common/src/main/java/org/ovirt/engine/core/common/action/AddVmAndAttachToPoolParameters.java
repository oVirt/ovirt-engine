package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmAndAttachToPoolParameters extends AddVmFromScratchParameters {
    private static final long serialVersionUID = -2676528333942591702L;

    private Guid poolId;
    private String currentVmName;

    public AddVmAndAttachToPoolParameters() {
    }

    public AddVmAndAttachToPoolParameters(VmStatic currVm, Guid poolId, String currentVmName, Guid storageDomainId) {
        super(currVm, new ArrayList<DiskImageBase>(
                Arrays.asList(new DiskImageBase())), storageDomainId);
        this.poolId = poolId;
        this.currentVmName = currentVmName;
    }

    public AddVmAndAttachToPoolParameters(VmStatic currVm,
            Guid poolId,
            String currentVmName,
            HashMap<Guid, Guid> imageToDestinationDomainMap) {
        super(currVm, new ArrayList<DiskImageBase>(
                Arrays.asList(new DiskImageBase())), Guid.Empty);
        this.poolId = poolId;
        this.currentVmName = currentVmName;
        setImageToDestinationDomainMap(imageToDestinationDomainMap);
    }

    public Guid getPoolId() {
        return poolId;
    }

    public String getCurrentVmName() {
        return currentVmName;
    }
}
