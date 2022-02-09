package org.ovirt.engine.core.bll.validator;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmValidatorTest extends BaseCommandTest {

    private VmValidator validator;

    private VM vm;

    private static final Version COMPAT_VERSION_FOR_CPU_SOCKET_TEST = Version.v4_6;
    private static final int MAX_NUM_CPUS = 16;
    private static final int MAX_NUM_SOCKETS = 4;
    private static final int MAX_NUM_CPUS_PER_SOCKET = 3;
    private static final int MAX_NUM_THREADS_PER_CPU = 2;

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", MAX_NUM_CPUS);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                        createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets,
                        COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                        MAX_NUM_SOCKETS),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfCpuPerSocket,
                        COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                        MAX_NUM_CPUS_PER_SOCKET),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfThreadsPerCpu,
                        COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                        MAX_NUM_THREADS_PER_CPU)
        );
    }

    @Mock
    @InjectedMock
    public VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    @InjectedMock
    public DiskVmElementDao diskVmElementDao;

    @Mock
    @InjectedMock
    public VnicProfileDao vnicProfileDao;

    @Mock
    private OsRepository osRepository;

    @BeforeEach
    public void setUp() throws InitializationException {
        vm = createVm();
        validator = spy(new VmValidator(vm));
        mockVmPropertiesUtils();
        when(osRepository.getMinimumCpus(anyInt())).thenReturn(2);
    }

    private VM createVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterCompatibilityVersion(COMPAT_VERSION_FOR_CPU_SOCKET_TEST);
        return vm;
    }

    private void setVmCpuValues(int numOfSockets, int cpuPerSocket, int threadsPerCpu) {
        vm.setNumOfSockets(numOfSockets);
        vm.setCpuPerSocket(cpuPerSocket);
        vm.setThreadsPerCpu(threadsPerCpu);
    }

    private void mockVmPropertiesUtils() throws InitializationException {
        VmPropertiesUtils utils = spy(new VmPropertiesUtils());
        doReturn("mdev_type=^(,?[0-9A-Za-z-]+)+$").
                when(utils)
                .getPredefinedVMProperties(any());
        doReturn("").
                when(utils)
                .getUserDefinedVMProperties(any());
        doReturn(new HashSet<>(Arrays.asList(Version.v4_4, Version.v4_5, Version.v4_6))).
                when(utils)
                .getSupportedClusterLevels();
        doReturn(utils).when(validator).getVmPropertiesUtils();
        utils.init();
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
        setVmCpuValues(1, 1, 2);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository), isValid());
    }

    @Test
    public void testVmExceedsMaxNumOfVmCpus() {
        setVmCpuValues(2, 3, 3);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_CPU));
    }

    @Test
    public void testVmExceedsMaxNumOfSockets() {
        setVmCpuValues(MAX_NUM_SOCKETS + 1, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_SOCKETS));
    }

    @Test
    public void testVmExceedsMaxNumOfCpusPerSocket() {
        setVmCpuValues(1, MAX_NUM_CPUS_PER_SOCKET + 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET));
    }

    @Test
    public void testVmExceedsMaxThreadsPerCpu() {
        setVmCpuValues(1, 1, MAX_NUM_THREADS_PER_CPU + 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAX_THREADS_PER_CPU));
    }

    @Test
    public void testVmUnderMinNumOfSockets() {
        setVmCpuValues(1, -2, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET));
    }

    @Test
    public void testVmUnderMinNumOfCpusPerSocket() {
        setVmCpuValues(-2, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_NUM_SOCKETS));
    }

    @Test
    public void testVmUnderMinNumOfThreadsPerCpu() {
        setVmCpuValues(1, 1, -2);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_THREADS_PER_CPU));
    }

    @Test
    public void testVmUnderMinNumCpusForOs() {
        setVmCpuValues(1, 1, 1);
        assertThat(VmValidator.validateCpuSockets(vm.getStaticData(), COMPAT_VERSION_FOR_CPU_SOCKET_TEST,
                ArchitectureType.x86_64, osRepository),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MIN_NUM_CPU_FOR_OS));
    }

    @Test
    public void testVmNotUsingMdevTypeHook() {
        assertNotNull(VmPropertiesUtils.getInstance());
        assertThat(validator.vmNotUsingMdevTypeHook(), isValid());
    }

    @Test
    public void testVmUsingMdevTypeHook() {
        vm.setCustomProperties("mdev_type=Test");
        assertNotNull(VmPropertiesUtils.getInstance());
        assertThat(validator.vmNotUsingMdevTypeHook(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_USES_MDEV_TYPE_HOOK));
    }

    private void validateVMPluggedDisksWithReservationStatus(boolean vmHasDisksPluggedWithReservation) {
        DiskVmElement dve = new DiskVmElement(null, vm.getId());
        dve.setUsingScsiReservation(vmHasDisksPluggedWithReservation);

        when(diskVmElementDao.getAllPluggedToVm(vm.getId())).thenReturn(
                Collections.singletonList(dve));

        if (vmHasDisksPluggedWithReservation) {
            // If the VM has plugged disks using ISCSI reservation the validation should fail
            assertThat(validator.isVmPluggedDiskNotUsingScsiReservation(),
                    failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION));
        } else {
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
