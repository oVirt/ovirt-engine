package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.dao.SnapshotDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmFromSnapshotCommandTest extends AddVmCommandTestBase<AddVmFromSnapshotCommand<AddVmFromSnapshotParameters>> {

    private static final Guid SOURCE_SNAPSHOT_ID = Guid.newGuid();

    @Mock
    private SnapshotDao snapshotDao;

    @Spy
    private SnapshotsValidator snapshotsValidator;

    @Override
    protected AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> createCommand() {
        AddVmFromSnapshotParameters param = new AddVmFromSnapshotParameters();
        param.setVm(vm);
        param.setSourceSnapshotId(SOURCE_SNAPSHOT_ID);
        param.setStorageDomainId(Guid.newGuid());
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = new AddVmFromSnapshotCommand<>(param, null);

        cmd.setVm(vm);
        cmd.setVmId(vm.getId());

        return cmd;
    }

    @Test
    public void validateSpaceAndThreshold() {
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotEnough() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() {
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
        doReturn(Collections.singletonList(disk)).when(cmd).getSourceDisks();

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
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any());
        Snapshot snap = new Snapshot();
        snap.setType(Snapshot.SnapshotType.REGULAR);
        when(snapshotDao.get(SOURCE_SNAPSHOT_ID)).thenReturn(snap);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
    }
}
