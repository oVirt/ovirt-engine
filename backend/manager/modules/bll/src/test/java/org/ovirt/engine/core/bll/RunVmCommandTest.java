package org.ovirt.engine.core.bll;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.RunVmParams.RunVmFlow;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Theories.class)
public class RunVmCommandTest extends BaseCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

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
    private SnapshotDao snapshotDAO;

    @Mock
    private VmDeviceDao deviceDao;

    @Mock
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    @Mock
    OsRepository osRepository;

    @Mock
    CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    private SnapshotsValidator snapshotsValidator;

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
        when(vdsBrokerFrontend.runVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class))).thenReturn(vdsReturnValue);

        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(ACTIVE_ISO_PREFIX);

        // Set create Vm.
        setCreateVmVDSMethod();
    }

    /**
     * Set create VM to return VM with status Up.
     */
    private void setCreateVmVDSMethod() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(VMStatus.Up);
        when(vdsBrokerFrontend.runAsyncVdsCommand(eq(VDSCommandType.Create),
                any(VdsAndVmIDVDSParametersBase.class),
                any(IVdsAsyncCommand.class))).thenReturn(returnValue);
    }

    /**
     * Set the Iso prefix.
     *
     * @param isoPrefix
     *            - Valid Iso patch or blank (when the Iso is not active.
     */
    private void setIsoPrefixVDSMethod(final String isoPrefix) {
        doReturn(isoPrefix).when(command).getIsoPrefix(any(Guid.class), any(Guid.class));
    }

    @Test
    public void validateSimpleInitrdAndKernelName() throws Exception {
        String Initrd = "/boot/initrd.initrd";
        String Kernel = "/boot/kernel.image";
        VM vm = createVmForTesting(Initrd, Kernel);
        assertEquals(Initrd, vm.getInitrdUrl());
        assertEquals(Kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefix() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertEquals(ACTIVE_ISO_PREFIX + "/" + kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixForKernelAndNoPrefixForInitrd() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(ACTIVE_ISO_PREFIX + "/" + kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixForInitrdAndNoPrefixForKernel() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, kernel);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertEquals(kernel, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixNameForKernelAndNullForInitrd() throws Exception {
        String kernel = "kernel";
        VM vm = createVmForTesting(null, RunVmCommand.ISO_PREFIX + kernel);
        assertNull(vm.getInitrdUrl());
        assertEquals(vm.getKernelUrl(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixCaseSensitive() throws Exception {
        String initrd = "ISO://";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixForOnlyIsoPrefixInKernelAndInitrd() throws Exception {
        String initrd = RunVmCommand.ISO_PREFIX;
        String kernelUrl = RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals("", vm.getInitrdUrl());
        assertEquals("", vm.getKernelUrl());
    }

    @Test
    public void checkIsoPrefixForNastyCharacters() throws Exception {
        String initrd = "@#$!";
        String kernelUrl = "    ";
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelUrl, vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixNameForInitrdAndNullForKernel() throws Exception {
        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(ACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
        assertNull(vm.getKernelUrl());
    }

    @Test
    public void validateIsoPrefixWhenNoActiveIso() throws Exception {
        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(INACTIVE_ISO_PREFIX);

        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(INACTIVE_ISO_PREFIX + "/" + initrd, vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixWithTrippleSlash() throws Exception {
        String initrd = RunVmCommand.ISO_PREFIX + "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(ACTIVE_ISO_PREFIX + "/", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixInTheMiddleOfTheInitrdAndKerenelName() throws Exception {
        String initrd = "initrd " + RunVmCommand.ISO_PREFIX;
        String kernelUrl = "kernelUrl " + RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelUrl, vm.getKernelUrl());
    }

    @Test
    public void validateInitrdWithSlashOnly() throws Exception {
        String initrd = "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("/", vm.getInitrdUrl());
    }

    @Test
    public void validateIsoPrefixWithBackSlash() throws Exception {
        String initrd = "iso:\\";
        VM vm = createVmForTesting(initrd, null);
        assertEquals("iso:\\", vm.getInitrdUrl());
    }

    @Test
    public void validateBootPrefixForInitrdAndKernelImage() throws Exception {
        String initrd = "/boot";
        String kernelImage = "/boot";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(initrd, vm.getInitrdUrl());
        assertEquals(kernelImage, vm.getKernelUrl());
    }

    @Test
    public void validateInitrdAndKernelImageWithOneCharacter() throws Exception {
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
        when(vmDao.get(command.getParameters().getVmId())).thenReturn(vm);
        command.setCluster(new Cluster());
        // Avoid referencing the unmockable static VmHandler.updateCurrentCd
        doNothing().when(command).updateCurrentCd(anyString());
        doReturn(null).when(command).getMemoryFromActiveSnapshot();
        when(snapshotDAO.exists(any(Guid.class), any(SnapshotStatus.class))).thenReturn(false);
        return vm;
    }

    @Before
    public void setUp() {
        mockCpuFlagsManagerHandler();
        when(osRepository.isWindows(anyInt())).thenReturn(false);
        when(osRepository.isCpuSupported(anyInt(), any(), any())).thenReturn(true);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        vmHandler.init();

        mockSuccessfulRunVmValidator();
        doNothing().when(command).initParametersForExternalNetworks();
        doReturn(Collections.emptyMap()).when(command).flushPassthroughVnicToVfMap();
        mockBackend();
    }

    private void mockCpuFlagsManagerHandler() {
        when(cpuFlagsManagerHandler.getCpuId(any(), any())).thenReturn(CPU_ID);
    }

    @Test
    public void testValidate() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        command.setVm(vm);
        command.setStoragePool(new StoragePool());
        doReturn(true).when(command).checkRngDeviceClusterCompatibility();
        doReturn(true).when(command).checkPayload(any(VmPayload.class), anyString());
        command.setCluster(new Cluster());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @DataPoints
    public static VmRngDevice.Source[] rngSources = VmRngDevice.Source.values();
    @DataPoints
    public static Set<VmRngDevice.Source>[] rngSourcesSubsets;

    static {
        HashSet<VmRngDevice.Source> sourcesEmpty = new HashSet<>();
        HashSet<VmRngDevice.Source> sourcesRandom = new HashSet<>();
        sourcesRandom.add(VmRngDevice.Source.RANDOM);
        HashSet<VmRngDevice.Source> sourcesHwRng = new HashSet<>();
        sourcesHwRng.add(VmRngDevice.Source.HWRNG);
        HashSet<VmRngDevice.Source> sourcesAll = new HashSet<>();
        sourcesAll.add(VmRngDevice.Source.RANDOM);
        sourcesAll.add(VmRngDevice.Source.HWRNG);

        rngSourcesSubsets = new Set[] { sourcesEmpty, sourcesRandom, sourcesHwRng, sourcesAll };
    }

    @Theory
    public void testValidateUnsupportedRng(VmRngDevice.Source vmRngSource, Set<VmRngDevice.Source> clusterReqSources) {
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
        when(deviceDao.getVmDeviceByVmIdTypeAndDevice(command.getVmId(), VmDeviceGeneralType.RNG, VmDeviceType.VIRTIO.getName()))
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

    @Test
    public void testEffectiveEmulatedMachineWithCustomSet() {
        final VM vm = new VM();
        final Cluster cluster = new Cluster();
        cluster.setEmulatedMachine("cluster-pc-i440fx-rhel7.3.0");
        vm.setCustomEmulatedMachine("testpc-i440fx-rhel7.3.0");
        command.setCluster(cluster);
        command.setVm(vm);
        assertEquals("testpc-i440fx-rhel7.3.0", command.getEffectiveEmulatedMachine());
    }

    @Test
    public void testEffectiveEmulatedMachineWithoutCustomSet() {
        final VM vm = new VM();
        final Cluster cluster = new Cluster();
        cluster.setEmulatedMachine("cluster-pc-i440fx-rhel7.3.0");
        command.setCluster(cluster);
        command.setVm(vm);
        assertEquals("cluster-pc-i440fx-rhel7.3.0", command.getEffectiveEmulatedMachine());
    }

    @Test
    public void testEffectiveEmulatedMachineCCV() {
        final VM vm = new VM();
        final Cluster cluster = new Cluster();
        cluster.setEmulatedMachine("pc-i440fx-rhel7.3.0");
        vm.setCustomCompatibilityVersion(Version.v4_0);
        command.setCluster(cluster);
        command.setVm(vm);
        List<String> supported = Arrays.asList("pc-i440fx-rhel7.2.0", "pc-i440fx-2.1", "pseries-rhel7.2.0");
        mcr.mockConfigValue(ConfigValues.ClusterEmulatedMachines, Version.v4_0, supported);
        assertEquals("pc-i440fx-rhel7.2.0", command.getEffectiveEmulatedMachine());
    }

    private RunVmValidator mockSuccessfulRunVmValidator() {
        RunVmValidator runVmValidator = mock(RunVmValidator.class);
        when(runVmValidator.canRunVm(
                anyList(), any(StoragePool.class), anyList(), anyList(), any(Cluster.class), anyBoolean()))
                .thenReturn(true);
        doReturn(runVmValidator).when(command).getRunVmValidator();
        return runVmValidator;
    }

    @Test
    public void testFindBestMatchForEmulateMachine() {
        String original = "pc-i440fx-rhel7.3.0";
        String bestMatch = "pc-i440fx-rhel7.2.0";
        List<String> candidates = Arrays.asList("pc-i440fx-2.1", bestMatch, "pseries-rhel7.2.0");
        assertEquals(bestMatch, command.findBestMatchForEmulatedMachine(original, candidates));
    }

    @Test
    public void testFindBestMatchForEmulateMachineKeepsCurrent() {
        String original = "pc-i440fx-rhel7.3.0";
        List<String> candidates = Arrays.asList("pc-i440fx-2.1", original, "pseries-rhel7.2.0");
        assertEquals(original, command.findBestMatchForEmulatedMachine(original, candidates));
    }
}
