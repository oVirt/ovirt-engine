package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiskVmElementValidatorTest {
    @Mock
    @InjectedMock
    public OsRepository osRepository;

    @Mock
    @InjectedMock
    public VmDeviceUtils vmDeviceUtils;

    @Mock
    @InjectedMock
    public VmValidationUtils vmValidationUtils;

    private static final int OS_WITH_SUPPORTED_INTERFACES = 1;
    private static final int OS_WITH_NO_SUPPORTED_INTERFACES = 2;

    private Disk disk;
    private DiskVmElement dve;
    private DiskVmElementValidator validator;

    @BeforeEach
    public void setUp() {
        initializeInterfaceValidation(DiskInterface.VirtIO);

        disk = new DiskImage();
        dve = new DiskVmElement();

        validator = new DiskVmElementValidator(disk, dve);
    }

    @Test
    public void readOnlyIsNotSupportedByDiskInterface() {
        dve.setReadOnly(true);
        dve.setDiskInterface(DiskInterface.IDE);

        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR)).
                        and(replacements(hasItem(String.format("$interface %1$s", DiskInterface.IDE)))));

        dve.setReadOnly(true);
        disk.setSgio(ScsiGenericIO.FILTERED);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(),
                failsWith(EngineMessage.SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK));
    }

    @Test
    public void readOnlyIsSupportedByDiskInterface() {
        dve.setReadOnly(true);
        DiskVmElement dve = new DiskVmElement();
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        dve.setReadOnly(false);
        dve.setDiskInterface(DiskInterface.IDE);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        dve.setReadOnly(true);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        dve.setReadOnly(false);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());

        dve.setDiskInterface(DiskInterface.IDE);
        assertThat(validator.isReadOnlyPropertyCompatibleWithInterface(), isValid());
    }

    @Test
    public void diskInterfaceSupportedByOs() {
        VM vm = createVM(OS_WITH_SUPPORTED_INTERFACES);
        initializeInterfaceValidation(DiskInterface.VirtIO);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), isValid());
    }

    @Test
    public void diskInterfaceNotSupportedByOs() {
        VM vm = createVM(OS_WITH_NO_SUPPORTED_INTERFACES);
        initializeInterfaceValidation(DiskInterface.VirtIO);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), failsWith(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
    }

    @Test
    public void diskImageWithSgio() {
        VM vm = createVM(OS_WITH_NO_SUPPORTED_INTERFACES);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
        disk.setSgio(ScsiGenericIO.UNFILTERED);

        assertThat(validator.isVirtIoScsiValid(vm),
                failsWith(EngineMessage.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK));
    }

    @Test
    public void virtioScsiDiskWithoutController() {
        VM vm = createVM(OS_WITH_NO_SUPPORTED_INTERFACES);
        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);

        assertThat(validator.isVirtIoScsiValid(vm),
                failsWith(EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED));
    }

    private void initializeInterfaceValidation(DiskInterface diskInterface) {
        when(vmValidationUtils.isDiskInterfaceSupportedByOs(
                OS_WITH_SUPPORTED_INTERFACES, null, ChipsetType.I440FX, diskInterface)).thenReturn(true);
    }

    private static VM createVM(int vmOs) {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        vm.setVmOs(vmOs);
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        // TODO: the test should be modified to Q35_SEA_BIOS bios type
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        return vm;
    }
}
