package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Strict.class)
public class AddVmFromSnapshotCommandTest extends AddVmCommandTestBase<AddVmFromSnapshotCommand<AddVmFromSnapshotParameters>> {

    private static final Guid SOURCE_SNAPSHOT_ID = Guid.newGuid();

    @Spy
    private SnapshotsValidator snapshotsValidator;

    @Override
    protected AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> createCommand() {
        initVM();
        AddVmFromSnapshotParameters param = new AddVmFromSnapshotParameters();
        param.setVm(vm);
        param.setSourceSnapshotId(SOURCE_SNAPSHOT_ID);
        param.setStorageDomainId(Guid.newGuid());
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = new AddVmFromSnapshotCommand<>(param, null);

        cmd.setVm(vm);
        cmd.setVmId(vm.getId());

        return cmd;
    }

    @Override
    public void setUp() {
        super.setUp();
        generateStorageToDisksMap();
        initDestSDs();
    }

    @Test
    public void validateSpaceAndThreshold() {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    @Test
    public void testCannotDisableVirtioScsiCanDisableIDE() {
        cmd.getParameters().setVirtioScsiEnabled(false);

        VM vm = new VM();
        doReturn(vm).when(cmd).getVmFromConfiguration();

        DiskImage disk = new DiskImage();
        disk.setPlugged(true);
        DiskVmElement dve = new DiskVmElement(disk.getId(), vm.getId());
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        disk.setDiskVmElements(Collections.singletonList(dve));
        doReturn(Collections.singletonList(disk)).when(cmd).getAdjustedDiskImagesFromConfiguration();

        VmValidator vmValidator = spy(new VmValidator(vm));
        doReturn(vmValidator).when(cmd).createVmValidator(vm);

        assertFalse(cmd.checkCanDisableVirtIoScsi());
        ValidateTestUtils.assertValidationMessages("Validation should prevent disabling of virtIO-scsi.",
                cmd,
                EngineMessage.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);

        dve.setDiskInterface(DiskInterface.IDE);
        assertTrue(cmd.checkCanDisableVirtIoScsi());
    }

    @Test
    public void canAddCloneVmFromSnapshotSnapshotDoesNotExist() {
        cmd.getVm().setName("vm1");
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void canAddCloneVmFromSnapshotNoConfiguration() {
        cmd.getVm().setName("vm1");
        doReturn(null).when(cmd).getVmFromConfiguration();
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any());
        when(snapshotDao.get(SOURCE_SNAPSHOT_ID)).thenReturn(new Snapshot());
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
    }
}
