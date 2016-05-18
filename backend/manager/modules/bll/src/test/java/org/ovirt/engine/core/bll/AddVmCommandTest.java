package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

@SuppressWarnings("serial")
public class AddVmCommandTest extends BaseCommandTest {

    private static final Guid STORAGE_DOMAIN_ID_1 = Guid.newGuid();
    private static final Guid STORAGE_DOMAIN_ID_2 = Guid.newGuid();
    protected static final int TOTAL_NUM_DOMAINS = 2;
    private static final int NUM_DISKS_STORAGE_DOMAIN_1 = 3;
    private static final int NUM_DISKS_STORAGE_DOMAIN_2 = 3;
    private static final int REQUIRED_DISK_SIZE_GB = 10;
    private static final int AVAILABLE_SPACE_GB = 11;
    private static final int USED_SPACE_GB = 4;
    private static final int MAX_PCI_SLOTS = 26;
    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final String CPU_ID = "0";
    private VmTemplate vmTemplate;
    private Cluster cluster;
    private StoragePool storagePool;
    protected StorageDomainValidator storageDomainValidator;

    private static final Map<String, String> migrationMap = new HashMap<>();

    static {
        migrationMap.put("undefined", "true");
        migrationMap.put("x86_64", "true");
        migrationMap.put("ppc64", "false");
    }

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private MacPoolPerDc macPoolPerDc;

    @Mock
    StorageDomainDao sdDao;

    @Mock
    VmTemplateDao vmTemplateDao;

    @Mock
    VmDao vmDao;

    @Mock
    VmStaticDao vmStaticDao;

    @Mock
    ClusterDao clusterDao;

    @Mock
    BackendInternal backend;

    @Mock
    VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    SnapshotDao snapshotDao;

    @Mock
    CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    OsRepository osRepository;

    @Mock
    VmDeviceDao deviceDao;

    @Mock
    DbFacade dbFacade;

    @Before
    public void initTest() {
        mockCpuFlagsManagerHandler();
        mockOsRepository();
        SimpleDependencyInjector.getInstance().bind(DbFacade.class, dbFacade);
        doReturn(deviceDao).when(dbFacade).getVmDeviceDao();
        VmDeviceUtils.init();
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        VM vm = createVm();
        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);

        mockStorageDomainDaoGetForStoragePool();
        mockClusterDaoReturnCluster();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockConfig();
        mockMaxPciSlots();

        mockOsRepository();
        mockOsRepositoryGraphics(0, Version.v4_0, new Pair<>(GraphicsType.SPICE, DisplayType.qxl));
        mockGraphicsDevices(vm.getId());

        mockStorageDomainDaoGetAllStoragesForPool(AVAILABLE_SPACE_GB);
        mockUninterestingMethods(cmd);
        mockGetAllSnapshots(cmd);
        doReturn(createStoragePool()).when(cmd).getStoragePool();

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private void mockGraphicsDevices(Guid vmId) {
        VmDevice graphicsDevice = new GraphicsDevice(VmDeviceType.SPICE);
        graphicsDevice.setDeviceId(Guid.Empty);
        graphicsDevice.setVmId(vmId);

        when(deviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.GRAPHICS)).thenReturn(Collections.singletonList(graphicsDevice));
    }

    private void mockOsRepositoryGraphics(int osId, Version ver, Pair<GraphicsType, DisplayType> supportedGraphicsAndDisplay) {
        Map<Version, List<Pair<GraphicsType, DisplayType>>> value = new HashMap<>();
        value.put(ver, Collections.singletonList(supportedGraphicsAndDisplay));

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> g = new HashMap<>();
        g.put(osId, value);
        when(osRepository.getGraphicsAndDisplays()).thenReturn(g);
    }

    protected void mockCpuFlagsManagerHandler() {
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        when(cpuFlagsManagerHandler.getCpuId(anyString(), any(Version.class))).thenReturn(CPU_ID);
    }

    protected void mockOsRepository() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        VmHandler.init();
        when(osRepository.isWindows(0)).thenReturn(true);
        when(osRepository.isCpuSupported(anyInt(), any(Version.class), anyString())).thenReturn(true);
    }

    @Test
    public void canAddVm() {
        List<String> reasons = new ArrayList<>();
        final int domainSizeGB = 20;
        AddVmCommand<AddVmParameters> cmd = setupCanAddVmTests(domainSizeGB);
        cmd.postConstruct();
        doReturn(true).when(cmd).validateCustomProperties(any(VmStatic.class), anyList());
        doReturn(true).when(cmd).validateSpaceRequirements();
        assertTrue("vm could not be added", cmd.canAddVm(reasons, Collections.singletonList(createStorageDomain(domainSizeGB))));
    }

    @Test
    public void canAddCloneVmFromSnapshotSnapshotDoesNotExist() {
        final int domainSizeGB = 15;
        final Guid sourceSnapshotId = Guid.newGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd =
                setupCanAddVmFromSnapshotTests(domainSizeGB, sourceSnapshotId);
        cmd.getVm().setName("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
    }

    @Test
    public void canAddCloneVmFromSnapshotNoConfiguration() {
        final int domainSizeGB = 15;
        final Guid sourceSnapshotId = Guid.newGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd =
                setupCanAddVmFromSnapshotTests(domainSizeGB, sourceSnapshotId);
        cmd.getVm().setName("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        SnapshotsValidator sv = spy(new SnapshotsValidator());
        doReturn(ValidationResult.VALID).when(sv).vmNotDuringSnapshot(any(Guid.class));
        doReturn(sv).when(cmd).createSnapshotsValidator();
        when(snapshotDao.get(sourceSnapshotId)).thenReturn(new Snapshot());
        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
    }

    @Test
    public void canAddVmWithVirtioScsiControllerNotSupportedOs() {
        VM vm = createVm();
        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);
        cluster = createCluster();
        vm.setClusterId(cluster.getId());

        mockStorageDomainDaoGetForStoragePool();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockConfig();
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllStoragesForPool(20);
        mockUninterestingMethods(cmd);
        mockDisplayTypes(vm.getOs());
        mockGraphicsDevices(vm.getId());
        doReturn(true).when(cmd).checkCpuSockets();

        doReturn(cluster).when(cmd).getCluster();
        doReturn(createStoragePool()).when(cmd).getStoragePool();
        cmd.getParameters().setVirtioScsiEnabled(true);
        when(osRepository.isSoundDeviceEnabled(any(Integer.class), any(Version.class))).thenReturn(true);
        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO")));
        mockGetAllSnapshots(cmd);

        cmd.initEffectiveCompatibilityVersion();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_OS_TYPE_DOES_NOT_SUPPORT_VIRTIO_SCSI);
    }

    @Test
    public void isVirtioScsiEnabledDefaultedToTrue() {
        AddVmCommand<AddVmParameters> cmd = setupCanAddVmTests(0);
        Cluster cluster = createCluster();
        doReturn(cluster).when(cmd).getCluster();
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.initEffectiveCompatibilityVersion();
        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO_SCSI")));
        assertTrue("isVirtioScsiEnabled hasn't been defaulted to true on cluster >= 3.3.", cmd.isVirtioScsiEnabled());
    }

    @Test
    public void validateSpaceAndThreshold() {
        AddVmCommand<AddVmParameters> command = setupCanAddVmTests(0);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForNewDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        assertTrue(command.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForNewDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(anyList());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        AddVmCommand<AddVmParameters> command = setupCanAddVmTests(0);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        assertFalse(command.validateSpaceRequirements());
        verify(storageDomainValidator).hasSpaceForNewDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(anyList());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        AddVmCommand<AddVmParameters> command = setupCanAddVmTests(0);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
               when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        assertFalse(command.validateSpaceRequirements());
    }

    @Test
    public void testUnsupportedCpus() {
        // prepare a command to pass validate
        VM vm = createVm();
        vm.setVmOs(OsRepository.DEFAULT_X86_OS);
        cluster = createCluster();
        vm.setClusterId(cluster.getId());
        when(clusterDao.get(cluster.getId())).thenReturn(cluster);

        AddVmFromTemplateCommand<AddVmParameters> cmd = createVmFromTemplateCommand(vm);

        mockStorageDomainDaoGetForStoragePool();
        mockVmTemplateDaoReturnVmTemplate();
        mockVerifyAddVM(cmd);
        mockConfig();
        mockMaxPciSlots();
        mockStorageDomainDaoGetAllStoragesForPool(20);
        mockDisplayTypes(vm.getOs());
        mockUninterestingMethods(cmd);
        mockGetAllSnapshots(cmd);
        when(osRepository.getArchitectureFromOS(0)).thenReturn(ArchitectureType.x86_64);
        doReturn(createStoragePool()).when(cmd).getStoragePool();

        // prepare the mock values
        Map<Pair<Integer, Version>, Set<String>> unsupported = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add(CPU_ID);
        unsupported.put(new Pair<>(vm.getVmOsId(), cluster.getCompatibilityVersion()), value);

        when(osRepository.isCpuSupported(vm.getVmOsId(), cluster.getCompatibilityVersion(), CPU_ID)).thenReturn(false);
        when(osRepository.getUnsupportedCpus()).thenReturn(unsupported);

        ValidateTestUtils.runAndAssertValidateFailure(
                cmd,
                EngineMessage.CPU_TYPE_UNSUPPORTED_FOR_THE_GUEST_OS);
    }


    private void mockDisplayTypes(int osId) {
        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    protected void mockNonInterestingMethodsForCloneVmFromSnapshot(AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd) {
        mockUninterestingMethods(cmd);
        doReturn(true).when(cmd).checkCpuSockets();
        doReturn(null).when(cmd).getVmFromConfiguration();
    }

    private void mockMaxPciSlots() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any(Version.class));
    }

    protected AddVmFromTemplateCommand<AddVmParameters> createVmFromTemplateCommand(VM vm) {
        AddVmParameters param = new AddVmParameters();
        param.setVm(vm);
        AddVmFromTemplateCommand<AddVmParameters> concrete =
                new AddVmFromTemplateCommand<AddVmParameters>(param, CommandContext.createContext(param.getSessionId())) {
                    @Override
                    protected void initUser() {
                        // Stub for testing
                    }

                    @Override
                    protected void initTemplateDisks() {
                        // Stub for testing
                    }

                    @Override
                    protected void initStoragePoolId() {
                        // Stub for testing
                    }

                    @Override
                    public VmTemplate getVmTemplate() {
                        return createVmTemplate();
                    }
                };
        AddVmFromTemplateCommand<AddVmParameters> result = spy(concrete);
        doReturn(true).when(result).checkNumberOfMonitors();
        doReturn(createVmTemplate()).when(result).getVmTemplate();
        doReturn(true).when(result).validateCustomProperties(any(VmStatic.class), anyList());
        mockDaos(result);
        mockBackend(result);
        initCommandMethods(result);
        result.poolPerDc = this.macPoolPerDc;
        result.postConstruct();
        return result;
    }

    private AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> createVmFromSnapshotCommand(VM vm,
            Guid sourceSnapshotId) {
        AddVmFromSnapshotParameters param = new AddVmFromSnapshotParameters();
        param.setVm(vm);
        param.setSourceSnapshotId(sourceSnapshotId);
        param.setStorageDomainId(Guid.newGuid());
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd =
                new AddVmFromSnapshotCommand<AddVmFromSnapshotParameters>(param, null) {
                    @Override
                    protected void initUser() {
                        // Stub for testing
                    }

                    @Override
                    protected void initTemplateDisks() {
                        // Stub for testing
                    }

                    @Override
                    protected void initStoragePoolId() {
                        // Stub for testing
                    }

                    @Override
                    public VmTemplate getVmTemplate() {
                        return createVmTemplate();
                    }
                };
        cmd = spy(cmd);
        doReturn(vm).when(cmd).getVm();
        mockDaos(cmd);
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        mockBackend(cmd);
        return cmd;
    }

    protected AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> setupCanAddVmFromSnapshotTests
            (final int domainSizeGB, Guid sourceSnapshotId) {
        VM vm = initializeMock(domainSizeGB);
        initializeVmDaoMock(vm);
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = createVmFromSnapshotCommand(vm, sourceSnapshotId);
        initCommandMethods(cmd);
        return cmd;
    }

    private void initializeVmDaoMock(VM vm) {
        when(vmDao.get(any(Guid.class))).thenReturn(vm);
    }

    private void initializeVmStaticDaoMock(VM vm) {
        when(vmStaticDao.get(any(Guid.class))).thenReturn(vm.getStaticData());
    }

    private AddVmCommand<AddVmParameters> setupCanAddVmTests(final int domainSizeGB) {
        VM vm = initializeMock(domainSizeGB);
        AddVmCommand<AddVmParameters> cmd = createCommand(vm);
        cmd.poolPerDc = macPoolPerDc;
        initCommandMethods(cmd);
        initializeVmStaticDaoMock(vm);
        doReturn(createVmTemplate()).when(cmd).getVmTemplate();
        doReturn(createStoragePool()).when(cmd).getStoragePool();
        return cmd;
    }

    private static <T extends AddVmParameters> void initCommandMethods(AddVmCommand<T> cmd) {
        doReturn(Guid.newGuid()).when(cmd).getStoragePoolId();
        doReturn(true).when(cmd).canAddVm(anyListOf(String.class), anyString(), any(Guid.class), anyInt());
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
    }

    private VM initializeMock(final int domainSizeGB) {
        mockVmTemplateDaoReturnVmTemplate();
        mockStorageDomainDaoGetForStoragePool(domainSizeGB);
        mockStorageDomainDaoGet(domainSizeGB);
        mockConfig();
        return createVm();
    }

    private void mockBackend(AddVmCommand<?> cmd) {
        doReturn(backend).when(cmd).getBackend();
    }

    private void mockDaos(AddVmCommand<?> cmd) {
        doReturn(vmDao).when(cmd).getVmDao();
        doReturn(sdDao).when(cmd).getStorageDomainDao();
        doReturn(vmTemplateDao).when(cmd).getVmTemplateDao();
        doReturn(clusterDao).when(cmd).getClusterDao();
        doReturn(deviceDao).when(cmd).getVmDeviceDao();
        doReturn(vmStaticDao).when(cmd).getVmStaticDao();

    }

    private void mockStorageDomainDaoGetForStoragePool(int domainSpaceGB) {
        when(sdDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(createStorageDomain(domainSpaceGB));
    }

    private void mockStorageDomainDaoGet(final int domainSpaceGB) {
        doAnswer(new Answer<StorageDomain>() {

            @Override
            public StorageDomain answer(InvocationOnMock invocation) throws Throwable {
                StorageDomain result = createStorageDomain(domainSpaceGB);
                result.setId((Guid) invocation.getArguments()[0]);
                return result;
            }

        }).when(sdDao).get(any(Guid.class));
    }

    private void mockStorageDomainDaoGetAllStoragesForPool(int domainSpaceGB) {
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(createStorageDomain(domainSpaceGB)));
    }

    private void mockStorageDomainDaoGetForStoragePool() {
        mockStorageDomainDaoGetForStoragePool(AVAILABLE_SPACE_GB);
    }

    private void mockVmTemplateDaoReturnVmTemplate() {
        when(vmTemplateDao.get(any(Guid.class))).thenReturn(createVmTemplate());
    }

    private void mockClusterDaoReturnCluster() {
        when(clusterDao.get(any(Guid.class))).thenReturn(createCluster());
    }

    private VmTemplate createVmTemplate() {
        if (vmTemplate == null) {
            vmTemplate = new VmTemplate();
            vmTemplate.setStoragePoolId(STORAGE_POOL_ID);
            DiskImage image = createDiskImageTemplate();
            vmTemplate.getDiskTemplateMap().put(image.getImageId(), image);
            HashMap<Guid, DiskImage> diskImageMap = new HashMap<>();
            DiskImage diskImage = createDiskImage(REQUIRED_DISK_SIZE_GB);
            diskImageMap.put(diskImage.getId(), diskImage);
            vmTemplate.setDiskImageMap(diskImageMap);
        }
        return vmTemplate;
    }

    private Cluster createCluster() {
        if (cluster == null) {
            cluster = new Cluster();
            cluster.setClusterId(Guid.newGuid());
            cluster.setCompatibilityVersion(Version.v4_0);
            cluster.setCpuName("Intel Conroe Family");
            cluster.setArchitecture(ArchitectureType.x86_64);
        }

        return cluster;
    }

    private Cluster createPpcCluster() {
        if (cluster == null) {
            cluster = new Cluster();
            cluster.setClusterId(Guid.newGuid());
            cluster.setCompatibilityVersion(Version.v4_0);
            cluster.setCpuName("PPC8");
            cluster.setArchitecture(ArchitectureType.ppc64);
        }

        return cluster;
    }

    private StoragePool createStoragePool() {
        if (storagePool == null) {
            storagePool = new StoragePool();
            storagePool.setId(STORAGE_POOL_ID);
            storagePool.setStatus(StoragePoolStatus.Up);
        }
        return storagePool;
    }


    private static DiskImage createDiskImageTemplate() {
        DiskImage i = new DiskImage();
        i.setSizeInGigabytes(USED_SPACE_GB + AVAILABLE_SPACE_GB);
        i.setActualSizeInBytes(REQUIRED_DISK_SIZE_GB * 1024L * 1024L * 1024L);
        i.setImageId(Guid.newGuid());
        i.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID_1)));
        return i;
    }

    private static DiskImage createDiskImage(int size) {
        DiskImage diskImage = new DiskImage();
        diskImage.setSizeInGigabytes(size);
        diskImage.setActualSize(size);
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID_1)));
        return diskImage;
    }

    protected StorageDomain createStorageDomain(int availableSpace) {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.Master);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setAvailableDiskSize(availableSpace);
        sd.setUsedDiskSize(USED_SPACE_GB);
        sd.setId(STORAGE_DOMAIN_ID_1);
        return sd;
    }

    private static void mockVerifyAddVM(AddVmCommand<?> cmd) {
        doReturn(true).when(cmd).verifyAddVM(anyListOf(String.class), anyInt());
    }

    private void mockConfig() {
        mcr.mockConfigValue(ConfigValues.ValidNumOfMonitors, Arrays.asList("1,2,4".split(",")));
        mcr.mockConfigValue(ConfigValues.IsMigrationSupported, Version.v4_0, migrationMap);
        mcr.mockConfigValue(ConfigValues.MaxIoThreadsPerVm, 127);
    }

    protected static VM createVm() {
        VM vm = new VM();
        VmDynamic dynamic = new VmDynamic();
        VmStatic stat = new VmStatic();
        stat.setVmtGuid(Guid.newGuid());
        stat.setName("testVm");
        stat.setPriority(1);
        vm.setStaticData(stat);
        vm.setDynamicData(dynamic);
        vm.setSingleQxlPci(false);
        return vm;
    }

    private AddVmCommand<AddVmParameters> createCommand(VM vm) {
        AddVmParameters param = new AddVmParameters(vm);
        AddVmCommand<AddVmParameters> cmd =
                new AddVmCommand<AddVmParameters>(param, CommandContext.createContext(param.getSessionId())) {

            @Override
            protected void initUser() {
                // Stub for testing
            }

            @Override
            protected void initTemplateDisks() {
                // Stub for testing
            }

            @Override
            protected void initStoragePoolId() {
                // stub for testing
            }

            @Override
            public VmTemplate getVmTemplate() {
                return createVmTemplate();
            }
        };
        cmd = spy(cmd);
        mockDaos(cmd);
        mockBackend(cmd);
        doReturn(new Cluster()).when(cmd).getCluster();
        generateStorageToDisksMap(cmd);
        initDestSDs(cmd);
        storageDomainValidator = mock(StorageDomainValidator.class);
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(storageDomainValidator).when(cmd).createStorageDomainValidator(any(StorageDomain.class));
        return cmd;
    }

     protected void generateStorageToDisksMap(AddVmCommand<? extends AddVmParameters> command) {
        command.storageToDisksMap = new HashMap<>();
        command.storageToDisksMap.put(STORAGE_DOMAIN_ID_1, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_1));
        command.storageToDisksMap.put(STORAGE_DOMAIN_ID_2, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_2));
    }

    private static List<DiskImage> generateDisksList(int size) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            DiskImage diskImage = createDiskImage(REQUIRED_DISK_SIZE_GB);
            disksList.add(diskImage);
        }
        return disksList;
    }

    protected void initDestSDs(AddVmCommand<? extends AddVmParameters> command) {
        StorageDomain sd1 = new StorageDomain();
        StorageDomain sd2 = new StorageDomain();
        sd1.setId(STORAGE_DOMAIN_ID_1);
        sd2.setId(STORAGE_DOMAIN_ID_2);
        command.destStorages.put(STORAGE_DOMAIN_ID_1, sd1);
        command.destStorages.put(STORAGE_DOMAIN_ID_2, sd2);
    }

    protected List<DiskImage> createDiskSnapshot(Guid diskId, int numOfImages) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < numOfImages; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setActive(false);
            diskImage.setId(diskId);
            diskImage.setImageId(Guid.newGuid());
            diskImage.setParentId(Guid.newGuid());
            diskImage.setImageStatus(ImageStatus.OK);
            disksList.add(diskImage);
        }
        return disksList;
    }

    private void mockGetAllSnapshots(AddVmFromTemplateCommand<AddVmParameters> command) {
        doAnswer(new Answer<List<DiskImage>>() {
            @Override
            public List<DiskImage> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                DiskImage arg = (DiskImage) args[0];
                return createDiskSnapshot(arg.getId(), 3);
            }
        }).when(command).getAllImageSnapshots(any(DiskImage.class));
    }



    private <T extends AddVmParameters> void mockUninterestingMethods(AddVmCommand<T> spy) {
        doReturn(true).when(spy).isVmNameValidLength(any(VM.class));
        doReturn(false).when(spy).isVmWithSameNameExists(anyString(), any(Guid.class));
        doReturn(STORAGE_POOL_ID).when(spy).getStoragePoolId();
        doReturn(createVmTemplate()).when(spy).getVmTemplate();
        doReturn(createCluster()).when(spy).getCluster();
        doReturn(true).when(spy).areParametersLegal(anyListOf(String.class));
        doReturn(Collections.<VmNetworkInterface> emptyList()).when(spy).getVmInterfaces();
        doReturn(Collections.<DiskVmElement> emptyList()).when(spy).getDiskVmElements();
        doReturn(false).when(spy).isVirtioScsiControllerAttached(any(Guid.class));
        doReturn(true).when(osRepository).isSoundDeviceEnabled(any(Integer.class), any(Version.class));
        spy.setVmTemplateId(Guid.newGuid());
    }

    @Test
    public void testBeanValidations() {
        assertTrue(createCommand(initializeMock(1)).validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        AddVmCommand<AddVmParameters> cmd = createCommand(initializeMock(1));
        cmd.getParameters().getVm().setName("aa-??bb");
        assertFalse("Pattern-based name should not be supported for VM", cmd.validateInputs());
    }

    @Test
    public void refuseBalloonOnPPC() {
        AddVmCommand<AddVmParameters> cmd = setupCanAddPpcTest();
        cmd.getParameters().setBalloonEnabled(true);
        when(osRepository.isBalloonEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
    }

    @Test
    public void refuseSoundDeviceOnPPC() {
        AddVmCommand<AddVmParameters> cmd = setupCanAddPpcTest();
        cmd.getParameters().setSoundDeviceEnabled(true);
        when(osRepository.isSoundDeviceEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
    }

    private AddVmCommand<AddVmParameters> setupCanAddPpcTest() {
        final int domainSizeGB = 20;
        AddVmCommand<AddVmParameters> cmd = setupCanAddVmTests(domainSizeGB);

        doReturn(true).when(cmd).validateSpaceRequirements();
        doReturn(true).when(cmd).buildAndCheckDestStorageDomains();
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        Cluster cluster = new Cluster();
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(cmd).getCluster();

        return cmd;
    }

    @Test
    public void testStoragePoolDoesntExist() {
        final int domainSizeGB = 20;
        AddVmCommand<AddVmParameters> cmd = setupCanAddVmTests(domainSizeGB);

        doReturn(null).when(cmd).getStoragePool();

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testBlockUseHostCpuWithPPCArch() {
        mockConfig();
        AddVmCommand<AddVmParameters> cmd = setupCanAddPpcTest();
        cmd.setEffectiveCompatibilityVersion(Version.v4_0);
        doReturn(Collections.<DiskImageBase> emptyList()).when(cmd).getImagesToCheckDestinationStorageDomains();
        Cluster cluster = createPpcCluster();
        when(clusterDao.get(any(Guid.class))).thenReturn(cluster);
        doReturn(clusterDao).when(dbFacade).getClusterDao();
        doReturn(true).when(cmd).areParametersLegal(Collections.<String> emptyList());
        doReturn(true).when(cmd).validateAddVmCommand();
        doReturn(true).when(cmd).isVmNameValidLength(any(VM.class));
        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.ppc64);
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        cmd.getParameters().getVm().setUseHostCpuFlags(true);
        cmd.getParameters().getVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.getParameters().getVm().setVmOs(OsType.Other.ordinal());

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
    }
}
