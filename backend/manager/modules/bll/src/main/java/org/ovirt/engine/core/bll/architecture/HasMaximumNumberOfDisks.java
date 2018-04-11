package org.ovirt.engine.core.bll.architecture;

import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.archstrategy.ArchCommand;


public class HasMaximumNumberOfDisks implements ArchCommand {
    private boolean hasMaximum;
    private List<DiskVmElement> allDiskVmElements;

    public HasMaximumNumberOfDisks(Guid vmId) {
        allDiskVmElements = Injector.get(DiskVmElementDao.class).getAllForVm(vmId);
    }

    private int countDisks(final DiskInterface diskType) {
        return (int) allDiskVmElements.stream().filter(a -> a.getDiskInterface() == diskType).count();
    }

    @Override
    public void runForX86_64() {
        hasMaximum = VmCommand.MAX_IDE_SLOTS == countDisks(DiskInterface.IDE);
    }

    @Override
    public void runForPPC64() {
        hasMaximum = VmCommand.MAX_SPAPR_SCSI_DISKS == countDisks(DiskInterface.SPAPR_VSCSI);
    }

    @Override
    public void runForS390X() {
        hasMaximum = VmCommand.MAX_VIRTIO_CCW_DISKS == countDisks(DiskInterface.VirtIO);
    }

    public boolean returnValue() {
        return hasMaximum;
    }
}
