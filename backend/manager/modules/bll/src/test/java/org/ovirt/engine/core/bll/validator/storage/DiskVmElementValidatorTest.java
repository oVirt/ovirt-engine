package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class DiskVmElementValidatorTest {

    @Mock
    private OsRepository osRepository;

    private static final int OS_WITH_SUPPORTED_INTERFACES = 1;
    private static final int OS_WITH_NO_SUPPORTED_INTERFACES = 2;

    private Disk disk;
    private DiskVmElement dve;
    private DiskVmElementValidator validator;

    @Before
    public void setUp() {
        initializeOsRepository(DiskInterface.VirtIO);

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
        initializeOsRepository(DiskInterface.VirtIO);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), isValid());
    }

    @Test
    public void diskInterfaceNotSupportedByOs() {
        VM vm = createVM(OS_WITH_NO_SUPPORTED_INTERFACES);
        initializeOsRepository(DiskInterface.VirtIO);
        dve.setDiskInterface(DiskInterface.VirtIO);
        assertThat(validator.isDiskInterfaceSupported(vm), failsWith(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED));
    }

    private void initializeOsRepository(DiskInterface diskInterface) {
        ArrayList<String> supportedDiskInterfaces = new ArrayList<>();
        supportedDiskInterfaces.add(diskInterface.name());
        when(osRepository.getDiskInterfaces(OS_WITH_SUPPORTED_INTERFACES, null)).thenReturn(supportedDiskInterfaces);
        // init the injector with the osRepository instance
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    private static VM createVM(int vmOs) {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        vm.setVmOs(vmOs);
        return vm;
    }
}
