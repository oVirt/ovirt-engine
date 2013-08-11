package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmFromTemplateParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = -3400982291165788716L;

    public Guid OriginalTemplate;

    public AddVmFromTemplateParameters(VmStatic vmStatic, HashMap<Guid, DiskImage> diskInfoDestinationMap,
            Guid storageDomainId) {
        super(vmStatic);
        setStorageDomainId(storageDomainId);
        setDiskInfoDestinationMap(diskInfoDestinationMap);
        OriginalTemplate = Guid.Empty;
    }

    public AddVmFromTemplateParameters() {
        OriginalTemplate = Guid.Empty;
    }

    public AddVmFromTemplateParameters(VM vm, HashMap<Guid, DiskImage> diskInfoDestinationMap, Guid storageDomainId) {
        this(vm.getStaticData(), diskInfoDestinationMap, storageDomainId);
    }
}
