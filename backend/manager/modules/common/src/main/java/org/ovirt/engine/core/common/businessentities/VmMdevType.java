package org.ovirt.engine.core.common.businessentities;

// This class is introduced to avoid type ambiguity in REST API
// mapping conversions.
public class VmMdevType extends VmDevice {

    public VmMdevType() {
        super();
    }

    public VmMdevType(VmDevice device) {
        super(device.getId(), device.getType(), device.getDevice(), device.getAddress(), device.getSpecParams(),
                device.isManaged(), device.getReadOnly(), device.getReadOnly(), device.getAlias(),
                device.getCustomProperties(), device.getSnapshotId(), device.getLogicalName());
    }
}
