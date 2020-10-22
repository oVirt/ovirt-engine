package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.vdscommands.VDSCommandType.ConnectStorageServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.RunVmParams.RunVmFlow;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class RunVmCommandTest extends BaseCommandTest {
    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private RunVmCommand<RunVmParams> command = new RunVmCommand<>(new RunVmParams(Guid.newGuid()), null);

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VmDao vmDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private SnapshotDao snapshotDAO;

    @Mock
    private VmDeviceDao deviceDao;

    @Mock
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    @Mock
    OsRepository osRepository;

    @Mock
    @InjectedMock
    public CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;

    @Spy
    @InjectMocks
    VmHandler vmHandler;

    private static final String ACTIVE_ISO_PREFIX =
            "/rhev/data-center/mnt/some_computer/f6bccab4-e2f5-4e02-bba0-5748a7bc07b6/images/11111111-1111-1111-1111-111111111111";
    private static final String INACTIVE_ISO_PREFIX = "";
    public static final String CPU_ID = "mock-cpu-id";

    public void mockBackend() {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(true);
        when(vdsBrokerFrontend.runVdsCommand(any(), any())).thenReturn(vdsReturnValue);

        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(ACTIVE_ISO_PREFIX);

        // Set create Vm.
        setCreateVmVDSMethod();
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.BiosTypeSupported, Version.getLast(), true),
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }
    /**
     * Set create VM to return VM with status Up.
     */
    private void setCreateVmVDSMethod() {
        VDSReturnValue returnValue = succesfull();
        returnValue.setReturnValue(VMStatus.Up);
        when(vdsBrokerFrontend.runAsyncVdsCommand(eq(VDSCommandType.Create), any(), any())).thenReturn(returnValue);
    }

    /**
     * Set the Iso prefix.
     *
     * @param isoPrefix
     *            - Valid Iso patch or blank (when the Iso is not active.
     */
    private void setIsoPrefixVDSMethod(final String isoPrefix) {
        doReturn(isoPrefix).when(command).getIsoPrefix(any(), any());
    }

    @Test
    public void validateSimpleInitrdAndKernelName() {
        String Initrd = "/boot/initrd.initrd";
        String Kernel = "/boot/kernel.image";
        VM vm = createVmForTesting(Initrd, Kernel);
        assertEquals(Initrd, vm.getInitrdUrl());
        assertEquals(Kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefix() {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertEquals(ACTIVE_ISO_PREFIX + "/" + kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixForKernelAndNoPrefixForInitrd() {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(ACTIVE_ISO_PREFIX + "/" + kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixForInitrdAndNoPrefixForKernel() {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, kernel);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertEquals(kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixNameForKernelAndNullForInitrd() {
        String kernel = "kernel";
        VM vm = createVmForTesting(null, RunVmCommand.ISO_PREFIX + kernel);
        assertNull(vm.getInitrdUrl());
        assertEquals(vm.getKernelUrl(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixCaseSensitive() {
        String initrd = "ISO://";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixForOnlyIsoPrefixInKernelAndInitrd() {
        String initrd = RunVmCommand.ISO_PREFIX;
        String kernelUrl = RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals("", vm.getInitrdUrl());
        assertEquals("", vm.getKernelUrl());
    }

    @Test
    public void checkIsoPrefixForNastyCharacters() {
        String initrd = "@#$!";
        String kernelUrl = "    ";
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelUrl, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixNameForInitrdAndNullForKernel() {
        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertNull(vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixWhenNoActiveIso() {
        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(INACTIVE_ISO_PREFIX);

        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(INACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixWithTrippleSlash() {
        String initrd = RunVmCommand.ISO_PREFIX + "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(ACTIVE_ISO_PREFIX + "/", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixInTheMiddleOfTheInitrdAndKerenelName() {
        String initrd = "initrd " + RunVmCommand.ISO_PREFIX;
        String kernelUrl = "kernelUrl " + RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelUrl, vm.getKernelUrl());
    }

    @Test
    public void validateInitrdWithSlashOnly() {
        String initrd = "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("/", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixWithBackSlash() {
        String initrd = "iso:\\";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("iso:\\", vm.getInitrdUrl());
    }

    @Test
    public void validateBootPrefixForInitrdAndKernelImage() {
        String initrd = "/boot";
        String kernelImage = "/boot";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelImage, vm.getKernelUrl());
    }

    @Test
    public void validateInitrdAndKernelImageWithOneCharacter() {
        String initrd = "i";
        String kernelImage = "k";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals("i", vm.getInitrdUrl());
        assertEquals("k", vm.getKernelUrl());
    }

    private VM createVmForTesting(String initrd, String kernel) {
        mockVm();

        // Set parameter
        command.getVm().setInitrdUrl(initrd);
        command.getVm().setKernelUrl(kernel);
        command.createVm();

        return vmDao.get(command.getParameters().getVmId());
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setClusterCompatibilityVersion(Version.getLast());
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        command.setCluster(new Cluster());
        // Avoid referencing the unmockable static VmHandler.updateCurrentCd
        doNothing().when(command).updateCurrentCd(any());
        doReturn(false).when(command).shouldRestoreMemory();
        when(snapshotDAO.exists(any(Guid.class), any(SnapshotStatus.class))).thenReturn(false);
        return vm;
    }

    @BeforeEach
    @MockedConfig("mockConfiguration")
    public void setUp() {
        mockCpuFlagsManagerHandler();
        when(osRepository.isWindows(anyInt())).thenReturn(false);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        doNothing().when(vmHandler).enableVmsToolVersionCheck();
        vmHandler.init();

        mockSuccessfulRunVmValidator();
        doNothing().when(command).initParametersForExternalNetworks(null, false);
        doReturn(Collections.emptyMap()).when(command).flushPassthroughVnicToVfMap();
        mockBackend();
    }

    private void mockCpuFlagsManagerHandler() {
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testValidate() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        command.setVm(vm);
        command.setStoragePool(new StoragePool());
        doReturn(true).when(command).checkRngDeviceClusterCompatibility();
        doReturn(true).when(command).checkPayload(any());
        doReturn(ValidationResult.VALID).when(command).checkDisksInBackupStorage();
        doReturn(false).when(command).isVmDuringBackup();
        doNothing().when(command).checkVmLeaseStorageDomain();
        Cluster cluster = new Cluster();
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        command.setCluster(cluster);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    public static Stream<Arguments> validateUnsupportedRng() {
        Set<VmRngDevice.Source> sourcesEmpty = new HashSet<>();
        Set<VmRngDevice.Source> sourcesRandom = new HashSet<>();
        sourcesRandom.add(VmRngDevice.Source.RANDOM);
        Set<VmRngDevice.Source> sourcesHwRng = new HashSet<>();
        sourcesHwRng.add(VmRngDevice.Source.HWRNG);
        Set<VmRngDevice.Source> sourcesAll = new HashSet<>();
        sourcesAll.add(VmRngDevice.Source.RANDOM);
        sourcesAll.add(VmRngDevice.Source.HWRNG);

        // Create a stream of all the permutations of each VmRngDevice.Source with each set:
        return Stream.of(sourcesEmpty, sourcesRandom, sourcesHwRng, sourcesAll)
                .flatMap(v -> Arrays.stream(VmRngDevice.Source.values())
                        .map(c -> Arguments.of(c, v)));
    }

    @ParameterizedTest
    @MethodSource
    public void validateUnsupportedRng(VmRngDevice.Source vmRngSource, Set<VmRngDevice.Source> clusterReqSources) {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(command.getVmId());
        command.setVm(vm);

        Cluster cluster = mock(Cluster.class);
        when(cluster.getRequiredRngSources()).thenReturn(clusterReqSources);
        command.setCluster(cluster);

        VmRngDevice rngDevice = new VmRngDevice();
        rngDevice.setSource(vmRngSource);
        VmDevice rngAsDevice = new VmDevice();
        rngAsDevice.setSpecParams(rngDevice.getSpecParams());
        when(deviceDao.getVmDeviceByVmIdTypeAndDevice(command.getVmId(), VmDeviceGeneralType.RNG, VmDeviceType.VIRTIO))
                .thenReturn(Collections.singletonList(rngAsDevice));

        assertThat(command.checkRngDeviceClusterCompatibility(), is(clusterReqSources.contains(vmRngSource)));
    }

    @Test
    public void testFlowOnResume() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Paused);
        command.setVm(vm);
        assertEquals(RunVmFlow.RESUME_PAUSE, command.getFlow());
    }

    @Test
    public void testFlowOnDehibernate() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Suspended);
        command.setVm(vm);
        assertEquals(RunVmFlow.RESUME_HIBERNATE, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessNoDisks() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.getDiskList().clear();
        command.setVm(vm);
        command.getParameters().setRunAsStateless(true);
        doNothing().when(command).fetchVmDisksFromDb();
        doReturn(false).when(command).isStatelessSnapshotExistsForVm();
        assertEquals(RunVmFlow.CREATE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessWithDisks() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.getDiskList().add(new DiskImage());
        command.setVm(vm);
        command.getParameters().setRunAsStateless(true);
        doReturn(false).when(command).isStatelessSnapshotExistsForVm();
        doNothing().when(command).fetchVmDisksFromDb();
        assertEquals(RunVmFlow.CREATE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnRemoveStatelessImages() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        command.setVm(vm);
        command.getParameters().setRunAsStateless(false);
        command.setInternalExecution(false);
        doReturn(true).when(command).isStatelessSnapshotExistsForVm();
        doReturn(false).when(command).isVmPartOfManualPool();
        assertEquals(RunVmFlow.REMOVE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessWithStatelessSnapshot() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.getDiskList().add(new DiskImage());
        command.setVm(vm);
        command.getParameters().setRunAsStateless(true);
        doReturn(true).when(command).isStatelessSnapshotExistsForVm();
        doNothing().when(command).fetchVmDisksFromDb();
        assertEquals(RunVmFlow.REMOVE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessExistsForManualPool() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        command.setVm(vm);
        command.getParameters().setRunAsStateless(false);
        command.setInternalExecution(false);
        doReturn(true).when(command).isStatelessSnapshotExistsForVm();
        doReturn(true).when(command).isVmPartOfManualPool();
        assertEquals(RunVmFlow.RUN, command.getFlow());
    }

    private RunVmValidator mockSuccessfulRunVmValidator() {
        RunVmValidator runVmValidator = mock(RunVmValidator.class);
        when(runVmValidator.canRunVm(any(), any(), any(), any(), any(), anyBoolean())).thenReturn(true);
        doReturn(runVmValidator).when(command).getRunVmValidator();
        return runVmValidator;
    }

    @Test
    public void auditLogRunStatelessVmCreateImages() {
        doReturn(RunVmFlow.CREATE_STATELESS_IMAGES).when(command).getFlow();
        command.setVm(new VM());
        command.setInternalExecution(false);
        command.setSucceeded(true);
        command.setActionReturnValue(VMStatus.Down);
        doReturn(false).when(command).isStatelessSnapshotExistsForVm();
        assertEquals(AuditLogType.USER_INITIATED_RUN_VM, command.getAuditLogTypeValue());
    }

    @Test
    public void skipIdenticalTargets() {
        command.setVm(new VM());
        // create 2 lun disks
        LunDisk lunDisk1 = new LunDisk();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("id1");
        lunDisk1.setLun(lun1);
        LunDisk lunDisk2 = new LunDisk();
        LUNs lun2 = new LUNs();
        lun2.setLUNId("id2");
        lunDisk2.setLun(lun2);

        // add luns to the vm
        command.getVm().getDiskMap().put(Guid.newGuid(), lunDisk1);
        command.getVm().getDiskMap().put(Guid.newGuid(), lunDisk2);
        List<StorageServerConnections> connections = new ArrayList<>();

        // luns have the same backing targets
        connections.add(new StorageServerConnections("/path/to/con1", "id1", null, null, StorageType.ISCSI, null, null, null));
        when(storageServerConnectionDao.getAllForLun("id1")).thenReturn(connections);
        when(storageServerConnectionDao.getAllForLun("id2")).thenReturn(connections);
        ArgumentCaptor<StorageServerConnectionManagementVDSParameters> captor =
                ArgumentCaptor.forClass(StorageServerConnectionManagementVDSParameters.class);
        doReturn(succesfull())
                .when(command).runVdsCommand(eq(ConnectStorageServer), any(StorageServerConnectionManagementVDSParameters.class));

        boolean connectSucceeded = command.connectLunDisks(Guid.newGuid());

        // for same targets, connect only once
        verify(command).runVdsCommand(
                eq(ConnectStorageServer),
                captor.capture());
        assertThat(captor.getValue().getConnectionList().size(), is(1));
        assertTrue(connectSucceeded);
    }

    private VDSReturnValue succesfull() {
        VDSReturnValue v = new VDSReturnValue();
        v.setSucceeded(true);
        return v;
    }

    @Test
    public void connectAllTargets() {
        command.setVm(new VM());
        // create 2 lun disks
        LunDisk lunDisk1 = new LunDisk();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("id1");
        lunDisk1.setLun(lun1);
        LunDisk lunDisk2 = new LunDisk();
        LUNs lun2 = new LUNs();
        lun2.setLUNId("id2");
        lunDisk2.setLun(lun2);

        // add luns to the vm
        command.getVm().getDiskMap().put(Guid.newGuid(), lunDisk1);
        command.getVm().getDiskMap().put(Guid.newGuid(), lunDisk2);
        List<StorageServerConnections> connections1 = new ArrayList<>();
        List<StorageServerConnections> connections2 = new ArrayList<>();

        // luns have the separate backing targets
        connections1.add(new StorageServerConnections("/path/to/con1", "id1", null, null, StorageType.ISCSI, null, null, null));
        connections2.add(new StorageServerConnections("/path/to/con2", "id2", null, null, StorageType.ISCSI, null, null, null));
        when(storageServerConnectionDao.getAllForLun("id1")).thenReturn(connections1);
        when(storageServerConnectionDao.getAllForLun("id2")).thenReturn(connections2);
        ArgumentCaptor<StorageServerConnectionManagementVDSParameters> captor =
                ArgumentCaptor.forClass(StorageServerConnectionManagementVDSParameters.class);
        doReturn(succesfull())
            .when(command).runVdsCommand(eq(ConnectStorageServer), any(StorageServerConnectionManagementVDSParameters.class));

        boolean connectSucceeded = command.connectLunDisks(Guid.newGuid());

        // for different targets, make sure all of them are connected
        verify(command).runVdsCommand(
                eq(ConnectStorageServer),
                captor.capture());
        assertThat(captor.getValue().getConnectionList().size(), is(2));
        assertTrue(connectSucceeded);
    }

    @Test
    public void dontConnectFCLuns() {
        // FC luns are connected physically, they don't have StorageServerConnection set.
        // Make sure if we have an FC lun connection we don't try to connect
        // otherwise NPE will be thrown.
        command.setVm(new VM());

        // create 2 FC lun disks
        LunDisk fcLunDisk = new LunDisk();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("id1");
        fcLunDisk.setLun(lun1);
        LunDisk isciDisk = new LunDisk();
        LUNs lun2 = new LUNs();
        lun2.setLUNId("id2");
        isciDisk.setLun(lun2);

        // add luns to the vm
        command.getVm().getDiskMap().put(Guid.newGuid(), fcLunDisk);
        command.getVm().getDiskMap().put(Guid.newGuid(), isciDisk);
        List<StorageServerConnections> iscsiLunConnections = new ArrayList<>();
        iscsiLunConnections.add(new StorageServerConnections("path/to/iscsi/connection", "id1", null, null, StorageType.ISCSI, null, null, null));
        when(storageServerConnectionDao.getAllForLun("id1")).thenReturn(Collections.emptyList());
        when(storageServerConnectionDao.getAllForLun("id2")).thenReturn(iscsiLunConnections);
        ArgumentCaptor<StorageServerConnectionManagementVDSParameters> captor =
                ArgumentCaptor.forClass(StorageServerConnectionManagementVDSParameters.class);
        doReturn(succesfull())
            .when(command).runVdsCommand(eq(ConnectStorageServer), any(StorageServerConnectionManagementVDSParameters.class));

        boolean connectSucceeded = command.connectLunDisks(Guid.newGuid());

        // for different targets, make sure we connect all but not FC.
        verify(command).runVdsCommand(
                eq(ConnectStorageServer),
                captor.capture());
        assertThat(captor.getValue().getConnectionList().size(), is(1));
        assertEquals(StorageType.ISCSI, captor.getValue().getStorageType());
        assertTrue(connectSucceeded);
    }

    @Test
    public void oneConnectFailed() {
        command.setVm(new VM());
        // create 2 lun disks
        LunDisk lunDisk1 = new LunDisk();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("id1");
        lunDisk1.setLun(lun1);

        // add luns to the vm
        command.getVm().getDiskMap().put(Guid.newGuid(), lunDisk1);
        List<StorageServerConnections> connections = new ArrayList<>();

        // luns have the same backing targets
        connections.add(new StorageServerConnections("/path/to/con1", "id1", null, null, StorageType.ISCSI, null, null, null));
        when(storageServerConnectionDao.getAllForLun("id1")).thenReturn(connections);
        ArgumentCaptor<StorageServerConnectionManagementVDSParameters> captor =
                ArgumentCaptor.forClass(StorageServerConnectionManagementVDSParameters.class);
        doReturn(new VDSReturnValue())
                .when(command).runVdsCommand(eq(ConnectStorageServer), any(StorageServerConnectionManagementVDSParameters.class));

        boolean connectSucceeded = command.connectLunDisks(Guid.newGuid());

        // for same targets, connect only once
        verify(command).runVdsCommand(
                eq(ConnectStorageServer),
                captor.capture());
        assertThat(captor.getValue().getConnectionList().size(), is(1));
        assertFalse(connectSucceeded);

    }
}
