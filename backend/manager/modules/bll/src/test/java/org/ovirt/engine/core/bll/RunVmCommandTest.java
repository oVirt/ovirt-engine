package org.ovirt.engine.core.bll;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.RunVmCommand.RunVmFlow;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.RunVmValidator;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Theories.class)
public class RunVmCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GuestToolsSetupIsoPrefix, "General", "")
            );

    /**
     * The command under test.
     */
    private RunVmCommand<RunVmParams> command;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private StoragePoolDAO spDao;

    @Mock
    private VmDeviceDAO deviceDao;

    @Mock
    private BackendInternal backend;

    @Mock
    private IsoDomainListSyncronizer isoDomainListSyncronizer;

    @Mock
    OsRepository osRepository;

    private static final String ACTIVE_ISO_PREFIX =
            "/rhev/data-center/mnt/some_computer/f6bccab4-e2f5-4e02-bba0-5748a7bc07b6/images/11111111-1111-1111-1111-111111111111";
    private static final String INACTIVE_ISO_PREFIX = "";

    public void mockBackend() {
        doReturn(backend).when(command).getBackend();

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(true);
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class))).thenReturn(vdsReturnValue);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);

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
        when(backend.getResourceManager().RunAsyncVdsCommand(eq(VDSCommandType.CreateVm),
                any(VdsAndVmIDVDSParametersBase.class),
                any(IVdsAsyncCommand.class))).thenReturn(returnValue);
    }

    private static DiskImage createImage() {
        final DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(Guid.newGuid())));
        return diskImage;
    }

    /**
     * Set the Iso prefix.
     *
     * @param isoPrefix
     *            - Valid Iso patch or blank (when the Iso is not active.
     */
    private void setIsoPrefixVDSMethod(final String isoPrefix) {
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return ImagesHandler.cdPathWindowsToLinux(invocation.getArguments()[0].toString(), isoPrefix);
            }

        }).when(command).cdPathWindowsToLinux(anyString());
    }

    @Test
    public void validateSimpleInitrdAndKernelName() throws Exception {
        String Initrd = "/boot/initrd.initrd";
        String Kernel = "/boot/kernel.image";
        VM vm = createVmForTesting(Initrd, Kernel);
        assertEquals(vm.getInitrdUrl(), Initrd);
        assertEquals(vm.getKernelUrl(), Kernel);
    }

    @Test
    public void validateIsoPrefix() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(vm.getInitrdUrl(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getKernelUrl(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixForKernelAndNoPrefixForInitrd() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(initrd, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(vm.getInitrdUrl(), initrd);
        assertEquals(vm.getKernelUrl(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixForInitrdAndNoPrefixForKernel() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, kernel);
        assertEquals(vm.getInitrdUrl(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getKernelUrl(), kernel);
    }

    @Test
    public void validateIsoPrefixNameForKernelAndNullForInitrd() throws Exception {
        String kernel = "kernel";
        VM vm = createVmForTesting(null, RunVmCommand.ISO_PREFIX + kernel);
        assertEquals(vm.getInitrdUrl(), null);
        assertEquals(vm.getKernelUrl(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixCaseSensitive() throws Exception {
        String initrd = "ISO://";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getInitrdUrl(), "");
    }

    @Test
    public void validateIsoPrefixForOnlyIsoPrefixInKernelAndInitrd() throws Exception {
        String initrd = RunVmCommand.ISO_PREFIX;
        String kernelUrl = RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getInitrdUrl(), "");
        assertEquals(vm.getKernelUrl(), "");
    }

    @Test
    public void checkIsoPrefixForNastyCharacters() throws Exception {
        String initrd = "@#$!";
        String kernelUrl = "    ";
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getInitrdUrl(), initrd);
        assertEquals(vm.getKernelUrl(), kernelUrl);
    }

    @Test
    public void validateIsoPrefixNameForInitrdAndNullForKernel() throws Exception {
        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(vm.getInitrdUrl(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getKernelUrl(), null);
    }

    @Test
    public void validateIsoPrefixWhenNoActiveIso() throws Exception {
        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(INACTIVE_ISO_PREFIX);

        String initrd = "initrd";
        VM vm = createVmForTesting(RunVmCommand.ISO_PREFIX + initrd, null);
        assertEquals(vm.getInitrdUrl(), INACTIVE_ISO_PREFIX + "/" + initrd);
    }

    @Test
    public void validateIsoPrefixWithTrippleSlash() throws Exception {
        String initrd = RunVmCommand.ISO_PREFIX + "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getInitrdUrl(), ACTIVE_ISO_PREFIX + "/");
    }

    @Test
    public void validateIsoPrefixInTheMiddleOfTheInitrdAndKerenelName() throws Exception {
        String initrd = "initrd " + RunVmCommand.ISO_PREFIX;
        String kernelUrl = "kernelUrl " + RunVmCommand.ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getInitrdUrl(), initrd);
        assertEquals(vm.getKernelUrl(), kernelUrl);
    }

    @Test
    public void validateInitrdWithSlashOnly() throws Exception {
        String initrd = "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getInitrdUrl(), "/");
    }

    @Test
    public void validateIsoPrefixWithBackSlash() throws Exception {
        String initrd = "iso:\\";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getInitrdUrl(), "iso:\\");
    }

    @Test
    public void validateBootPrefixForInitrdAndKernelImage() throws Exception {
        String initrd = "/boot";
        String kernelImage = "/boot";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(vm.getInitrdUrl(), initrd);
        assertEquals(vm.getKernelUrl(), kernelImage);
    }

    @Test
    public void validateInitrdAndKernelImageWithOneCharacter() throws Exception {
        String initrd = "i";
        String kernelImage = "k";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(vm.getInitrdUrl(), "i");
        assertEquals(vm.getKernelUrl(), "k");
    }

    private VM createVmForTesting(String initrd, String kernel) {
        mockVm(command);

        // Set parameter
        command.getVm().setInitrdUrl(initrd);
        command.getVm().setKernelUrl(kernel);
        command.createVm();

        // Check Vm
        VM vm = vmDAO.get(command.getParameters().getVmId());
        return vm;
    }

    /**
     * Mock a VM.
     */
    private VM mockVm(RunVmCommand<RunVmParams> spyVmCommand) {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vmDAO).when(command).getVmDAO();
        when(vmDAO.get(command.getParameters().getVmId())).thenReturn(vm);
        doReturn(new VDSGroup()).when(command).getVdsGroup();
        // Avoid referencing the unmockable static VmHandler.updateCurrentCd
        doNothing().when(command).updateCurrentCd(any(String.class));
        return vm;
    }

    @Before
    public void initMockitoAnnotations() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void createCommand() {

        when(osRepository.isWindows(Mockito.anyInt())).thenReturn(false);
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        RunVmParams param = new RunVmParams(Guid.newGuid());
        command = spy(new RunVmCommand<RunVmParams>(param) {
            @Override
            protected void loadPayloadDevice() {
            }
        });
        mockIsoDomainListSyncronizer();
        mockSuccessfulRunVmValidator();
        doNothing().when(command).initParametersForExternalNetworks();
        mockSuccessfulSnapshotValidator();
        mockBackend();
    }

    private void mockIsoDomainListSyncronizer() {
        doNothing().when(isoDomainListSyncronizer).init();
        doReturn(isoDomainListSyncronizer).when(command).getIsoDomainListSyncronizer();
    }

    @Test
    public void testCanDoAction() {
        final ArrayList<Disk> disks = new ArrayList<Disk>();
        final DiskImage diskImage = createImage();
        disks.add(diskImage);
        initDAOMocks(disks);
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(new StoragePool()).when(command).getStoragePool();
        doReturn(vm).when(command).getVm();
        doReturn(true).when(command).checkRngDeviceClusterCompatibility();
        doReturn(true).when(command).checkPayload(any(VmPayload.class), anyString());
        doReturn(new VDSGroup()).when(command).getVdsGroup();
        assertTrue(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().isEmpty());
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
    public void testCanDoActionUnsupportedRng(VmRngDevice.Source vmRngSource, Set<VmRngDevice.Source> clusterReqSources) {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vm).when(command).getVm();

        VDSGroup cluster = mock(VDSGroup.class);
        when(cluster.getRequiredRngSources()).thenReturn(clusterReqSources);
        doReturn(cluster).when(command).getVdsGroup();

        VmRngDevice rngDevice = new VmRngDevice();
        rngDevice.setSource(vmRngSource);
        VmDevice rngAsDevice = new VmDevice();
        rngAsDevice.setSpecParams(rngDevice.getSpecParams());
        when(deviceDao.getVmDeviceByVmIdTypeAndDevice(command.getVmId(), VmDeviceGeneralType.RNG, VmDeviceType.VIRTIO.getName()))
                .thenReturn(Collections.singletonList(rngAsDevice));

        doReturn(deviceDao).when(command).getVmDeviceDao();

        Assert.assertThat(command.canDoAction(), is(clusterReqSources.contains(vmRngSource)));
    }

    @Test
    public void testFlowOnResume() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Paused);
        doReturn(vm).when(command).getVm();
        assertEquals(RunVmFlow.RESUME_PAUSE, command.getFlow());
    }

    @Test
    public void testFlowOnDehibernate() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Suspended);
        doReturn(vm).when(command).getVm();
        assertEquals(RunVmFlow.RESUME_HIBERNATE, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessNoDisks() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.getDiskList().clear();
        doReturn(vm).when(command).getVm();
        final RunVmParams params = new RunVmParams();
        params.setRunAsStateless(true);
        doReturn(params).when(command).getParameters();
        doNothing().when(command).fetchVmDisksFromDb();
        assertEquals(RunVmFlow.RUN, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessWithDisks() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.getDiskList().add(new DiskImage());
        doReturn(vm).when(command).getVm();
        final RunVmParams params = new RunVmParams();
        params.setRunAsStateless(true);
        doReturn(params).when(command).getParameters();
        doNothing().when(command).fetchVmDisksFromDb();
        assertEquals(RunVmFlow.CREATE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnRemoveStatelessImages() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vm).when(command).getVm();
        final RunVmParams params = new RunVmParams();
        params.setRunAsStateless(false);
        doReturn(params).when(command).getParameters();
        doReturn(false).when(command).isInternalExecution();
        doReturn(true).when(command).isStatelessSnapshotExistsForVm();
        doReturn(false).when(command).isVmPartOfManualPool();
        assertEquals(RunVmFlow.REMOVE_STATELESS_IMAGES, command.getFlow());
    }

    @Test
    public void testFlowOnStatelessExistsForManualPool() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(vm).when(command).getVm();
        final RunVmParams params = new RunVmParams();
        params.setRunAsStateless(false);
        doReturn(params).when(command).getParameters();
        doReturn(false).when(command).isInternalExecution();
        doReturn(true).when(command).isStatelessSnapshotExistsForVm();
        doReturn(true).when(command).isVmPartOfManualPool();
        assertEquals(RunVmFlow.RUN, command.getFlow());
    }

    /**
     * @param disks
     * @param guid
     */
    protected void initDAOMocks(final List<Disk> disks) {
        final DiskDao diskDao = mock(DiskDao.class);
        when(diskDao.getAllForVm(Guid.Empty, true)).thenReturn(disks);
        doReturn(diskDao).when(command).getDiskDao();

        final StorageDomainDAO storageDomainDAO = mock(StorageDomainDAO.class);
        when(storageDomainDAO.getAllForStoragePool(Guid.Empty))
                .thenReturn(new ArrayList<StorageDomain>());
        doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
    }

    private SnapshotsValidator mockSuccessfulSnapshotValidator() {
        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        when(snapshotsValidator.vmNotDuringSnapshot(any(Guid.class))).thenReturn(ValidationResult.VALID);
        when(snapshotsValidator.vmNotInPreview(any(Guid.class))).thenReturn(ValidationResult.VALID);
        doReturn(snapshotsValidator).when(command).getSnapshotsValidator();
        return snapshotsValidator;
    }

    private RunVmValidator mockSuccessfulRunVmValidator() {
        RunVmValidator runVmValidator = mock(RunVmValidator.class);
        when(runVmValidator.canRunVm(
                Matchers.anyListOf(String.class),
                any(StoragePool.class),
                Matchers.anyListOf(Guid.class),
                Matchers.anyListOf(Guid.class),
                any(Guid.class),
                any(VDSGroup.class))).thenReturn(true);
        when(runVmValidator.validateNetworkInterfaces()).thenReturn(ValidationResult.VALID);
        doReturn(runVmValidator).when(command).getRunVmValidator();
        return runVmValidator;
    }
}
