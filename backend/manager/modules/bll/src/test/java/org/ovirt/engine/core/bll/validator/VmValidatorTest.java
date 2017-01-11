package org.ovirt.engine.core.bll.validator;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class VmValidatorTest extends DbDependentTestBase {

    private VmValidator validator;

    private VM vm;

    private static final String COMPAT_VERSION_FOR_CPU_SOCKET_TEST = "4.0";
    private static final int MAX_NUM_CPUS = 16;
    private static final int MAX_NUM_SOCKETS = 4;
    private static final int MAX_NUM_CPUS_PER_SOCKET = 3;
    private static final int MAX_NUM_THREADS_PER_CPU = 2;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxNumOfVmCpus, COMPAT_VERSION_FOR_CPU_SOCKET_TEST, MAX_NUM_CPUS),
            mockConfig(ConfigValues.MaxNumOfVmSockets, COMPAT_VERSION_FOR_CPU_SOCKET_TEST, MAX_NUM_SOCKETS),
            mockConfig(ConfigValues.MaxNumOfCpuPerSocket, COMPAT_VERSION_FOR_CPU_SOCKET_TEST, MAX_NUM_CPUS_PER_SOCKET),
            mockConfig(ConfigValues.MaxNumOfThreadsPerCpu, COMPAT_VERSION_FOR_CPU_SOCKET_TEST, MAX_NUM_THREADS_PER_CPU),
            mockConfig(ConfigValues.SriovHotPlugSupported, Version.v3_6, false),
            mockConfig(ConfigValues.SriovHotPlugSupported, Version.v4_0, true)
    );

    @Mock
    VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    DiskVmElementDao diskVmElementDao;

    @Mock
    VnicProfileDao vnicProfileDao;

    @Before
    public void setUp() {
        vm = createVm();
        validator = new VmValidator(vm);

        when(DbFacade.getInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDao);
        when(DbFacade.getInstance().getVnicProfileDao()).thenReturn(vnicProfileDao);
    }

    private VM createVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterCompatibilityVersion(Version.v4_0);
        return vm;
    }

    private void setVmCpuValues(int numOfSockets, int cpuPerSocket, int threadsPerCpu) {
        vm.setNumOfSockets(numOfSockets);
        vm.setCpuPerSocket(cpuPerSocket);
        vm.setThreadsPerCpu(threadsPerCpu);
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
    public void allPassthroughVnicsMigratableForRegularVnics() {
        mockVnics(createRegularNics(2));
        assertThatAllPassthroughVnicsMigratable(true);
    }

    @Test
    public void allPassthroughVnicsMigratableForEmptyVnicList() {
        mockVnics(createRegularNics(0));
        assertThatAllPassthroughVnicsMigratable(true);
    }

    @Test
    public void allPassthroughVnicsMigratable() {
        mockVnics(Stream.concat(createMigratablePassthroughNics(2, false), createRegularNics(3)));
        assertThatAllPassthroughVnicsMigratable(true);
    }

    @Test
    public void testVmHasPluggedDisksUsingScsiReservation() {
        validateVMPluggedDisksWithReservationStatus(true);
    }

    @Test
    public void testVmHasNoPluggedDisksUsingScsiReservation() {
        validateVMPluggedDisksWithReservationStatus(false);
    }

    @Test
    public void testVmPassesCpuSocketValidation() {
        setVmCpuValues(1, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST), isValid());
    }

    @Test
    public void testVmExceedsMaxNumOfVmCpus() {
        setVmCpuValues(2, 3, 3);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_CPU));
    }

    @Test
    public void testVmExceedsMaxNumOfSockets() {
        setVmCpuValues(MAX_NUM_SOCKETS + 1, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_SOCKETS));
    }

    @Test
    public void testVmExceedsMaxNumOfCpusPerSocket() {
        setVmCpuValues(1, MAX_NUM_CPUS_PER_SOCKET + 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET));
    }

    @Test
    public void testVmExceedsMaxThreadsPerCpu() {
        setVmCpuValues(1, 1, MAX_NUM_THREADS_PER_CPU + 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_THREADS_PER_CPU));
    }

    @Test
    public void testVmUnderMinNumOfSockets() {
        setVmCpuValues(1, -2, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET));
    }

    @Test
    public void testVmUnderMinNumOfCpusPerSocket() {
        setVmCpuValues(-2, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_NUM_SOCKETS));
    }

    @Test
    public void testVmUnderMinNumOfThreadsPerCpu() {
        setVmCpuValues(1, 1, -2);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_THREADS_PER_CPU));
    }

    private void validateVMPluggedDisksWithReservationStatus(boolean vmHasDisksPluggedWithReservation) {
        DiskVmElement dve = new DiskVmElement(null, vm.getId());
        dve.setUsingScsiReservation(vmHasDisksPluggedWithReservation);

        when(DbFacade.getInstance().getDiskVmElementDao()).thenReturn(diskVmElementDao);
        when(diskVmElementDao.getAllPluggedToVm(vm.getId())).thenReturn(
                Collections.singletonList(dve));

        if (vmHasDisksPluggedWithReservation) {
            // If the VM has plugged disks using ISCSI reservation the validation should fail
            assertThat(validator.isVmPluggedDiskNotUsingScsiReservation(),
                    failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION));
        }
        else {
            assertThat(validator.isVmPluggedDiskNotUsingScsiReservation(), isValid());
        }
    }

    @Test
    public void thereIsPluggedPassthroughNonMigratableVnic() {
        mockVnics(createNonMigratablePassthroughNics(1, true));
        assertThatAllPassthroughVnicsMigratable(false);
    }

    @Test
    public void allPassthroughNonMigratableNicsAreUnplugged() {
        mockVnics(createNonMigratablePassthroughNics(2, false));
        assertThatAllPassthroughVnicsMigratable(true);
    }

    @Test
    public void passthroughVnicsMigrationIsNotSupported() {
        mockVnics(createMigratablePassthroughNics(1, true));
        vm.setClusterCompatibilityVersion(Version.v3_6);
        assertThatAllPassthroughVnicsMigratable(false);
    }

    private Stream<VmNetworkInterface> createRegularNics(int numberOfMocks) {
        //migratable should be ignored for regular nics, therefore its value should not matter.
        boolean migratable = RandomUtils.instance().nextBoolean();
        return createMocks(() -> mockVnic(false, migratable, true), numberOfMocks);
    }

    private Stream<VmNetworkInterface> createNonMigratablePassthroughNics(int numberOfMocks, boolean nicIsPlugged) {
        return createMocks(() -> mockVnic(true, false, nicIsPlugged), numberOfMocks);
    }

    private Stream<VmNetworkInterface> createMigratablePassthroughNics(int numberOfMocks, boolean nicIsPlugged) {
        return createMocks(() -> mockVnic(true, true, nicIsPlugged), numberOfMocks);
    }

    private Stream<VmNetworkInterface> createMocks(Supplier<VmNetworkInterface> supplier, long count) {
        return Stream.generate(supplier).limit(count);
    }

    private void mockVnics(Stream<VmNetworkInterface> vnics) {
        List<VmNetworkInterface> vNics = vnics.collect(toList());
        when(vmNetworkInterfaceDao.getAllForVm(vm.getId())).thenReturn(vNics);
    }

    private VmNetworkInterface mockVnic(boolean passthrough, boolean migratable, boolean plugged) {
        VmNetworkInterface vnic = mock(VmNetworkInterface.class);
        Guid vnicId = Guid.newGuid();
        when(vnic.getId()).thenReturn(vnicId);
        when(vnic.isPassthrough()).thenReturn(passthrough);

        when(vnic.isPlugged()).thenReturn(plugged);

        Guid vnicProfileId = Guid.newGuid();
        when(vnic.getVnicProfileId()).thenReturn(vnicProfileId);
        VnicProfile profile = mock(VnicProfile.class);
        when(vnicProfileDao.get(vnicProfileId)).thenReturn(profile);
        when(profile.isMigratable()).thenReturn(migratable);

        return vnic;
    }

    private void assertThatAllPassthroughVnicsMigratable(boolean valid) {
        assertThat(validator.allPassthroughVnicsMigratable(), valid ? isValid()
                : failsWith(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_OF_NON_MIGRATABLE_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED));
    }
}
