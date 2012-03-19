package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, ImagesHandler.class, QuotaManager.class })
public class RunVmCommandTest {

    /**
     * The command under test.
     */
    private RunVmCommand<RunVmParams> command;

    @Mock
    VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private QuotaManager quotaManager;

    @Mock
    BackendInternal backend;

    private static String ISO_PREFIX = "iso://";
    private static String ACTIVE_ISO_PREFIX =
            "/rhev/data-center/mnt/some_computer/f6bccab4-e2f5-4e02-bba0-5748a7bc07b6/images/11111111-1111-1111-1111-111111111111";
    private static String INACTIVE_ISO_PREFIX = "";

    public RunVmCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
    }

    @Before
    public void testBackendSetup() {
        when(Backend.getInstance()).thenReturn(backend);
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
        when(backend.getResourceManager().RunAsyncVdsCommand(Matchers.eq(VDSCommandType.CreateVm),
                Matchers.any(VdsAndVmIDVDSParametersBase.class), Matchers.any(IVdsAsyncCommand.class))).thenReturn(returnValue);
    }

    private DiskImage createImage() {
        final DiskImage diskImage = new DiskImage();
        diskImage.setimage_group_id(Guid.NewGuid());
        diskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(new Guid())));
        return diskImage;
    }

    private VmDevice createDiskVmDevice(final DiskImage diskImage) {
        final VmDevice vmDevice = new VmDevice();
        vmDevice.setIsPlugged(true);
        vmDevice.setId(new VmDeviceId(diskImage.getimage_group_id(), Guid.NewGuid()));
        return vmDevice;
    }

    /**
     * Set the Iso prefix.
     *
     * @param isoPrefix
     *            - Valid Iso patch or blank (when the Iso is not active.
     */
    private void setIsoPrefixVDSMethod(String isoPrefix) {
        VDSReturnValue isoPrefixReturnValue = new VDSReturnValue();
        isoPrefixReturnValue.setReturnValue(isoPrefix);
        when(backend.getResourceManager().RunVdsCommand(Matchers.eq(VDSCommandType.IsoPrefix),
                Matchers.any(IrsBaseVDSCommandParameters.class))).thenReturn(isoPrefixReturnValue);
    }

    @Test
    public void validateSimpleInitrdAndKernelName() throws Exception {
        String Initrd = "/boot/initrd.initrd";
        String Kernel = "/boot/kernel.image";
        VM vm = createVmForTesting(Initrd, Kernel);
        assertEquals(vm.getinitrd_url(), Initrd);
        assertEquals(vm.getkernel_url(), Kernel);
    }

    @Test
    public void validateIsoPrefix() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(ISO_PREFIX + initrd, ISO_PREFIX + kernel);
        assertEquals(vm.getinitrd_url(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getkernel_url(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixForKernelAndNoPrefixForInitrd() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(initrd, ISO_PREFIX + kernel);
        assertEquals(vm.getinitrd_url(), initrd);
        assertEquals(vm.getkernel_url(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixForInitrdAndNoPrefixForKernel() throws Exception {
        String initrd = "initrd";
        String kernel = "kernel";
        VM vm = createVmForTesting(ISO_PREFIX + initrd, kernel);
        assertEquals(vm.getinitrd_url(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getkernel_url(), kernel);
    }

    @Test
    public void validateIsoPrefixNameForKernelAndNullForInitrd() throws Exception {
        String kernel = "kernel";
        VM vm = createVmForTesting(null, ISO_PREFIX + kernel);
        assertEquals(vm.getinitrd_url(), null);
        assertEquals(vm.getkernel_url(), ACTIVE_ISO_PREFIX + "/" + kernel);
    }

    @Test
    public void validateIsoPrefixCaseSensitive() throws Exception {
        String initrd = "ISO://";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getinitrd_url(), "");
    }

    @Test
    public void validateIsoPrefixForOnlyIsoPrefixInKernelAndInitrd() throws Exception {
        String initrd = ISO_PREFIX;
        String kernelUrl = ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getinitrd_url(), "");
        assertEquals(vm.getkernel_url(), "");
    }

    @Test
    public void checkIsoPrefixForNastyCharacters() throws Exception {
        String initrd = "@#$!";
        String kernelUrl = "    ";
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getinitrd_url(), initrd);
        assertEquals(vm.getkernel_url(), kernelUrl);
    }

    @Test
    public void validateIsoPrefixNameForInitrdAndNullForKernel() throws Exception {
        String initrd = "initrd";
        VM vm = createVmForTesting(ISO_PREFIX + initrd, null);
        assertEquals(vm.getinitrd_url(), ACTIVE_ISO_PREFIX + "/" + initrd);
        assertEquals(vm.getkernel_url(), null);
    }

    @Test
    public void validateIsoPrefixWhenNoActiveIso() throws Exception {
        // Set Valid Iso Prefix
        setIsoPrefixVDSMethod(INACTIVE_ISO_PREFIX);

        String initrd = "initrd";
        VM vm = createVmForTesting(ISO_PREFIX + initrd, null);
        assertEquals(vm.getinitrd_url(), INACTIVE_ISO_PREFIX + "/" + initrd);
    }

    @Test
    public void validateIsoPrefixWithTrippleSlash() throws Exception {
        String initrd = ISO_PREFIX + "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getinitrd_url(), ACTIVE_ISO_PREFIX + "/");
    }

    @Test
    public void validateIsoPrefixInTheMiddleOfTheInitrdAndKerenelName() throws Exception {
        String initrd = "initrd " + ISO_PREFIX;
        String kernelUrl = "kernelUrl " + ISO_PREFIX;
        VM vm = createVmForTesting(initrd, kernelUrl);
        assertEquals(vm.getinitrd_url(), initrd);
        assertEquals(vm.getkernel_url(), kernelUrl);
    }

    @Test
    public void validateInitrdWithSlashOnly() throws Exception {
        String initrd = "/";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getinitrd_url(), "/");
    }

    @Test
    public void validateIsoPrefixWithBackSlash() throws Exception {
        String initrd = "iso:\\";
        VM vm = createVmForTesting(initrd, null);
        assertEquals(vm.getinitrd_url(), "iso:\\");
    }

    @Test
    public void validateBootPrefixForInitrdAndKernelImage() throws Exception {
        String initrd = "/boot";
        String kernelImage = "/boot";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(vm.getinitrd_url(), initrd);
        assertEquals(vm.getkernel_url(), kernelImage);
    }

    @Test
    public void validateInitrdAndKernelImageWithOneCharacter() throws Exception {
        String initrd = "i";
        String kernelImage = "k";
        VM vm = createVmForTesting(initrd, kernelImage);
        assertEquals(vm.getinitrd_url(), "i");
        assertEquals(vm.getkernel_url(), "k");
    }

    private VM createVmForTesting(String initrd, String kernel) {
        RunVmCommand<RunVmParams> spyCommand = createCommand();
        mockVm(spyCommand);

        // Set parameter
        RunVmParams runVmParams = command.getParameters();
        runVmParams.setinitrd_url(initrd);
        runVmParams.setkernel_url(kernel);
        spyCommand.CreateVm();

        // Check Vm
        VM vm = vmDAO.getById(command.getParameters().getVmId());
        return vm;
    }

    /**
     * Mock a VM.
     */
    private VM mockVm(RunVmCommand<RunVmParams> spyVmCommand) {
        VM vm = new VM();
        vm.setstatus(VMStatus.Down);
        AuditLogableBaseMockUtils.mockVmDao(spyVmCommand, vmDAO);
        when(vmDAO.getById(command.getParameters().getVmId())).thenReturn(vm);
        PowerMockito.mockStatic(QuotaManager.class);
        return vm;
    }

    private RunVmCommand<RunVmParams> createCommand() {
        RunVmParams param = new RunVmParams(Guid.NewGuid());
        command = new RunVmCommand<RunVmParams>(param);
        return spy(command);
    }

    @Test
    public void canRunVmFailNodisk() {
        initMocks(new ArrayList<DiskImage>(), new HashMap<VDSCommandType, Boolean>(), new ArrayList<VmDevice>());

        final VM vm = new VM();
        final ArrayList<String> messages = new ArrayList<String>();
        Assert.assertFalse(RunVmCommand.CanRunVm(vm,
                messages,
                new RunVmParams(),
                new VdsSelector(vm, new NGuid(), true),
                mockSuccessfulSnapshotValidator()));
        Assert.assertTrue(messages.contains("VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK"));
    }

    @Test
    public void canRunVmFailVmRunning() {
        final ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
        final DiskImage diskImage = createImage();
        disks.add(diskImage);
        final VmDevice vmDevice = createDiskVmDevice(diskImage);
        initMocks(disks, new HashMap<VDSCommandType, Boolean>(), Collections.singletonList(vmDevice));
        final VM vm = new VM();
        vm.setstatus(VMStatus.Up);
        final ArrayList<String> messages = new ArrayList<String>();
        Assert.assertFalse(RunVmCommand.CanRunVm(vm,
                messages,
                new RunVmParams(),
                new VdsSelector(vm, new NGuid(), true),
                mockSuccessfulSnapshotValidator()));
        Assert.assertTrue(messages.contains("ACTION_TYPE_FAILED_VM_IS_RUNNING"));
    }

    @Test
    public void canRunVmFailVmDuringSnapshot() {
        final ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
        final DiskImage diskImage = createImage();
        disks.add(diskImage);
        final VmDevice vmDevice = createDiskVmDevice(diskImage);
        initMocks(disks, new HashMap<VDSCommandType, Boolean>(), Collections.singletonList(vmDevice));
        final VM vm = new VM();
        final ArrayList<String> messages = new ArrayList<String>();
        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        when(snapshotsValidator.vmNotDuringSnapshot(vm.getId()))
                .thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT));
        Assert.assertFalse(RunVmCommand.CanRunVm(vm,
                messages,
                new RunVmParams(),
                new VdsSelector(vm, new NGuid(), true),
                snapshotsValidator));
        Assert.assertTrue(messages.contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_DURING_SNAPSHOT.name()));
    }

    private void canRunStatelessVmTest(boolean autoStartUp,
            boolean isVmStateless,
            Boolean isStatelessParam,
            boolean shouldPass) {
        final ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
        final DiskImage diskImage = createImage();
        disks.add(diskImage);
        final VmDevice vmDevice = createDiskVmDevice(diskImage);
        final HashMap<VDSCommandType, Boolean> calls = new HashMap<VDSCommandType, Boolean>();

        final VdsSelector vdsSelector = Mockito.mock(VdsSelector.class);
        Mockito.when(vdsSelector.CanFindVdsToRunOn(any(ArrayList.class), anyBoolean())).thenReturn(true);

        calls.put(VDSCommandType.IsVmDuringInitiating, false);
        initMocks(disks, calls, Collections.singletonList(vmDevice));

        final VM vm = new VM();
        // set stateless and HA
        vm.setis_stateless(isVmStateless);
        vm.setauto_startup(autoStartUp);

        final ArrayList<String> messages = new ArrayList<String>();
        final RunVmParams runParams = new RunVmParams();
        runParams.setRunAsStateless(isStatelessParam);
        boolean canRunVm =
                RunVmCommand.CanRunVm(vm, messages, runParams, vdsSelector, mockSuccessfulSnapshotValidator());

        if (shouldPass) {
            Assert.assertTrue(canRunVm);
            Assert.assertFalse(messages.contains("VM_CANNOT_RUN_STATELESS_HA"));
        } else {
            Assert.assertFalse(canRunVm);
            Assert.assertTrue(messages.contains("VM_CANNOT_RUN_STATELESS_HA"));
        }
    }

    @Test
    public void canRunVmFailStatelessWhenVmHA() {
        canRunStatelessVmTest(true, false, Boolean.TRUE, false);
    }

    @Test
    public void canRunVmPassStatelessWhenVmHAandStatelessFalse() {
        canRunStatelessVmTest(true, true, Boolean.FALSE, true);
    }

    @Test
    public void canRunVmFailStatelessWhenVmHAwithNullStatelessParam() {
        canRunStatelessVmTest(true, true, null, false);
    }

    @Test
    public void canRunVmPassStatelessWhenVmHAwithNullStatelessParam() {
        canRunStatelessVmTest(true, false, null, true);
    }

    @Test
    public void canRunVmPassStatelessWhenVmHAwithNegativeStatelessParam() {
        canRunStatelessVmTest(true, false, Boolean.FALSE, true);
    }

    @Test
    public void canRunVmPassStatelessWhenVmNotHAwithNegativeStatelessParam() {
        canRunStatelessVmTest(false, false, Boolean.TRUE, true);
    }

    /**
     * Workaround of the singleton design pattern with PowerMock and EasyMock.
     *
     * @param disks
     *            the disks for the VM
     */
    @SuppressWarnings("unchecked")
    private void initMocks(final List<DiskImage> disks, final Map<VDSCommandType, Boolean> calls,
            final List<VmDevice> vmDevices) {
        final Guid guid = new Guid("00000000-0000-0000-0000-000000000000");
        final IConfigUtilsInterface cfgUtils = Mockito.mock(IConfigUtilsInterface.class);
        Mockito.when(cfgUtils.GetValue(ConfigValues.VdsSelectionAlgorithm, "general")).thenReturn("0");
        Mockito.when(cfgUtils.GetValue(ConfigValues.PredefinedVMProperties, "3.0")).thenReturn("0");
        Mockito.when(cfgUtils.GetValue(ConfigValues.UserDefinedVMProperties, "3.0")).thenReturn("0");
        Config.setConfigUtils(cfgUtils);

        final DiskImageDAO diskImageDao = Mockito.mock(DiskImageDAO.class);
        Mockito.when(diskImageDao.getAllForVm(guid)).thenReturn(disks);

        final StorageDomainDAO storageDomainDAO = Mockito.mock(StorageDomainDAO.class);
        Mockito.when(storageDomainDAO.getAllForStoragePool(guid))
                .thenReturn(new ArrayList<storage_domains>());

        final VmDeviceDAO vmDeviceDao = Mockito.mock(VmDeviceDAO.class);
        Mockito.when(vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(guid,
                VmDeviceType.DISK.getName(),
                VmDeviceType.DISK.getName())).thenReturn(vmDevices);

        final DbFacade facadeMock = new DbFacade() {
            @Override
            public DiskImageDAO getDiskImageDAO() {
                return diskImageDao;
            }

            @Override
            public StorageDomainDAO getStorageDomainDAO() {
                return storageDomainDAO;
            }

            @Override
            public VmDeviceDAO getVmDeviceDAO() {
                return vmDeviceDao;
            }
        };

        final VDSBrokerFrontend vdsBrokerFrontendMock = new VDSBrokerFrontend() {
            @Override
            public VDSReturnValue RunVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
                final VDSReturnValue vdsReturnValue = new VDSReturnValue();
                if (calls.containsKey(commandType)) {
                    vdsReturnValue.setReturnValue(calls.get(commandType));
                } else {
                    vdsReturnValue.setReturnValue(Boolean.TRUE);
                }
                return vdsReturnValue;
            }

            @Override
            public VDSReturnValue RunAsyncVdsCommand(VDSCommandType commandType,
                    VdsAndVmIDVDSParametersBase parameters,
                    IVdsAsyncCommand command) {
                return null;
            }

            @Override
            public IVdsAsyncCommand GetAsyncCommandForVm(Guid vmId) {
                return null;
            }

            @Override
            public IVdsAsyncCommand RemoveAsyncRunningCommand(Guid vmId) {
                return null;
            }

            @Override
            public FutureVDSCall<VDSReturnValue> runFutureVdsCommand(FutureVDSCommandType commandType,
                    VdsIdVDSCommandParametersBase parameters) {
                return null;
            }

        };

        final Backend backendMock = new Backend() {
            @Override
            public VDSBrokerFrontend getResourceManager() {
                return vdsBrokerFrontendMock;
            }
        };
        PowerMockito.mockStatic(Backend.class);
        Mockito.when(Backend.getInstance()).thenReturn(backendMock);

        PowerMockito.mockStatic(DbFacade.class);
        Mockito.when(DbFacade.getInstance()).thenReturn(facadeMock);

        PowerMockito.mockStatic(ImagesHandler.class);
        Mockito.when(ImagesHandler.PerformImagesChecks(any(VM.class),
                any(ArrayList.class),
                any(Guid.class),
                any(Guid.class),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(), any(List.class)))
                .thenReturn(true);
        Mockito.when(ImagesHandler.isVmInPreview(any(Guid.class))).thenReturn(false);
    }

    private SnapshotsValidator mockSuccessfulSnapshotValidator() {
        SnapshotsValidator snapshotsValidator = mock(SnapshotsValidator.class);
        when(snapshotsValidator.vmNotDuringSnapshot(any(Guid.class))).thenReturn(new ValidationResult());
        return snapshotsValidator;
    }
}
