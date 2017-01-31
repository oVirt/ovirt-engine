package org.ovirt.engine.core.common.businessentities;

/**
 * This class should be used ONLY by Webadmin UI.
 *
 * It's purpose is to consider the "exclusive" lock of the VM as an "ImageLocked" status
 * while updating actions availability in UI.
 * Without using this class, the "exclusive" lock status of a VM is ignored by UI .
 */
public class VmWithStatusForExclusiveLock extends VM {
    public VmWithStatusForExclusiveLock() {
            super();
        }

    public VmWithStatusForExclusiveLock(VM vm) {
        super(vm.getStaticData(), vm.getDynamicData(), vm.getStatisticsData());
        setLockInfo(vm.getLockInfo());
    }

    @Override
    public VMStatus getStatus() {
        if (getLockInfo() != null && getLockInfo().isExclusive() && super.getStatus() == VMStatus.Down) {
            return VMStatus.ImageLocked;
        } else {
            return super.getStatus();
        }
    }
}
