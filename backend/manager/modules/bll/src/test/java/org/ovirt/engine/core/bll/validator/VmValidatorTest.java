package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

@RunWith(MockitoJUnitRunner.class)
public class VmValidatorTest extends DbDependentTestBase {

    private VmValidator validator;

    private VM vm;

    @Mock
    VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Before
    public void setUp() {
        vm = createVm();
        validator = new VmValidator(vm);

        when(DbFacade.getInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDao);
    }

    private VM createVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        return vm;
    }

    @Test
    public void canDisableVirtioScsiSuccess() {
        Disk disk = new DiskImage();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vm.getId());
        dve.setDiskInterface(DiskInterface.VirtIO);
        disk.setDiskVmElements(Collections.singletonList(dve));

        assertThat(validator.canDisableVirtioScsi(Collections.singletonList(disk)), isValid());
    }

    @Test
    public void canDisableVirtioScsiFail() {
        Disk disk = new DiskImage();
        DiskVmElement dve = new DiskVmElement(disk.getId(), vm.getId());
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        disk.setDiskVmElements(Collections.singletonList(dve));

        assertThat(validator.canDisableVirtioScsi(Collections.singletonList(disk)),
                failsWith(EngineMessage.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS));
    }

    @Test
    public void vmNotHavingPassthroughVnicsValid() {
        vmNotHavingPassthroughVnicsCommon(vm.getId(), 0, 2);
        assertThatVmNotHavingPassthroughVnics(true);
    }

    @Test
    public void vmNotHavingVnicsValid() {
        vmNotHavingPassthroughVnicsCommon(vm.getId(), 0, 0);
        assertThatVmNotHavingPassthroughVnics(true);
    }

    @Test
    public void vmNotHavingPassthroughVnicsNotValid() {
        vmNotHavingPassthroughVnicsCommon(vm.getId(), 2, 3);
        assertThat(validator.vmNotHavingPassthroughVnics(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_OF_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED));

    }

    private void vmNotHavingPassthroughVnicsCommon(Guid vmId, int numOfPassthroughVnic, int numOfRegularVnics) {
        List<VmNetworkInterface> vnics = new ArrayList<>();

        for (int i = 0; i < numOfPassthroughVnic; ++i) {
            vnics.add(mockVnic(true));
        }

        for (int i = 0; i < numOfRegularVnics; ++i) {
            vnics.add(mockVnic(false));
        }

        when(vmNetworkInterfaceDao.getAllForVm(vmId)).thenReturn(vnics);
    }

    private VmNetworkInterface mockVnic(boolean passthrough) {
        VmNetworkInterface vnic = mock(VmNetworkInterface.class);
        when(vnic.isPassthrough()).thenReturn(passthrough);

        return vnic;
    }

    private void assertThatVmNotHavingPassthroughVnics(boolean valid) {
        assertThat(validator.vmNotHavingPassthroughVnics(), valid ? isValid()
                : failsWith(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_OF_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED));
    }
}
