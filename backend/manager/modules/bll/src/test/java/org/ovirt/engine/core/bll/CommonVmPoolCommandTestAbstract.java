package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import  org.ovirt.engine.core.utils.MockedConfig;

public abstract class CommonVmPoolCommandTestAbstract extends BaseCommandTest {
    private final Guid clusterId = Guid.newGuid();
    protected final Guid firstStorageDomainId = Guid.newGuid();
    private final Guid secondStorageDomainId = Guid.newGuid();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid vmTemplateId = Guid.newGuid();
    protected Guid vmPoolId = Guid.newGuid();
    private Cluster cluster = mockCluster();
    protected VM testVm = mockVm();
    protected VmPool vmPools = mockVmPools();
    protected List<VM>  vms = mockVms();
    protected static int VM_COUNT = 5;
    protected VmTemplate vmTemplate = mockVmTemplate();
    protected StoragePool storagePool = mockStoragePool();
    protected List<StorageDomain> storageDomainsList;
    protected static final int MAX_MEMORY_SIZE = 4096;

    @Mock
    protected ClusterDao clusterDao;

    @Mock
    protected DiskImageDao diskImageDao;

    @Mock
    protected VmPoolDao vmPoolDao;

    @Mock
    protected VmDao vmDao;

    @Mock
    protected StoragePoolDao storagePoolDao;

    @Mock
    protected VmTemplateDao vmTemplateDao;

    @Mock
    protected StorageDomainDao storageDomainDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private MacPoolPerCluster macPoolPerCluster;

    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @Mock
    private VmHandler vmHandler;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    @MockedConfig("mockConfiguration")
    protected CommonVmPoolCommand<AddVmPoolParameters> command = createCommand();

    protected abstract CommonVmPoolCommand<AddVmPoolParameters> createCommand();

    @Test
    @MockedConfig("mockConfiguration")
    public void validateSufficientSpaceOnDestinationDomains() {
        setupForStorageTests();
        command.ensureDestinationImageMap();
        assertTrue(command.checkDestDomains());
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(any());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateInsufficientSpaceOnDomains() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForNewDisks(any());
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(any());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validateDomainNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(any());
    }

    @BeforeEach
    @MockedConfig("mockConfiguration")
    public void setupMocks() {
        setUpCommand();
        mockGetStorageDomainList();
        mockDbDao();
        command.init();
    }

    protected void setUpCommand() {
        doNothing().when(command).initTemplate();
        doReturn(true).when(command).areTemplateImagesInStorageReady(any());
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).setAndValidateCpuProfile();
    }

    protected void mockGetStorageDomainList() {
        // Mock Dao
        storageDomainsList = getStorageDomainList();
        mockDiskImageDao();
        mockStorageDomainDao(storageDomainsList);
    }

    private void mockStoragePoolDao() {
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);
    }

    private void mockVMTemplateDao() {
        when(vmTemplateDao.get(vmTemplateId)).thenReturn(vmTemplate);
    }

    private void mockClusterDao() {
        when(clusterDao.get(clusterId)).thenReturn(cluster);
    }

    private void mockDiskImageDao() {
        when(diskImageDao.getSnapshotById(any())).thenReturn(getDiskImageList().get(0));
    }

    private void mockStorageDomainDao(List<StorageDomain> storageDomains) {
        for (StorageDomain storageDomain : storageDomains) {
            when(storageDomainDao.getForStoragePool(storageDomain.getId(), storagePoolId)).thenReturn(storageDomain);
        }
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setVmtGuid(vmTemplateId);
        vm.setStaticData(getVmStatic());
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setName("my_vm");
        return vm;
    }

    private List<VM> mockVms() {
        List<VM>  vms = new ArrayList<>();
        vms.add(mockVm());
        return vms;
    }

    private StorageDomain mockDomain(Guid id) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStatus(StorageDomainStatus.Active);
        storageDomain.setId(id);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        return storageDomain;
    }


    protected List<StorageDomain> getStorageDomainList() {
        return Stream.of(firstStorageDomainId, secondStorageDomainId).map(this::mockDomain).collect(Collectors.toList());
    }

    /**
     * Mock VDS Group.
     */
    private Cluster mockCluster() {
        Cluster group = new Cluster();
        group.setClusterId(clusterId);
        group.setCompatibilityVersion(new Version());
        group.setStoragePoolId(storagePoolId);
        group.setCpuName("Intel Conroe Family");
        group.setArchitecture(ArchitectureType.x86_64);
        return group;
    }

    /**
     * Mock VM Template.
     */
    private VmTemplate mockVmTemplate() {
        VmTemplate template = new VmTemplate();
        template.setId(vmTemplateId);
        template.setStoragePoolId(storagePoolId);
        template.setClusterArch(ArchitectureType.x86_64);
        setDiskList(template);

        return template;
    }

    /**
     * Mock Storage Pool
     */
    private StoragePool mockStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);

        return storagePool;
    }

    private static void setDiskList(VmTemplate vmTemplate) {
        for (DiskImage diskImage : getDiskImageList()) {
            vmTemplate.getDiskList().add(diskImage);
        }
        Map<Guid, DiskImage> diskImageTemplate = getDiskImageTempalteList();
        vmTemplate.getDiskTemplateMap().putAll(diskImageTemplate);
    }

    private static List<DiskImage> getDiskImageList() {
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>());
        diskList.add(diskImage);
        diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>());
        diskList.add(diskImage);
        return diskList;
    }

    private static Map<Guid, DiskImage> getDiskImageTempalteList() {
        Map<Guid, DiskImage> diskTemplateList = new HashMap<>();
        DiskImage diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.newGuid());
        diskImageTemplate.setImageId(Guid.newGuid());
        diskImageTemplate.setStorageIds(new ArrayList<>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.newGuid());
        diskImageTemplate.setImageId(Guid.newGuid());
        diskImageTemplate.setStorageIds(new ArrayList<>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        return diskTemplateList;
    }

    private VmStatic getVmStatic() {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setOsId(OsRepository.DEFAULT_X86_OS);
        vmStatic.setNumOfMonitors(1);
        vmStatic.setMemSizeMb(300);
        vmStatic.setMaxMemorySizeMb(MAX_MEMORY_SIZE);
        vmStatic.setStateless(false);
        vmStatic.setVmtGuid(vmTemplateId);
        return vmStatic;
    }

    /**
     * Mock VM pools.
     */
    private VmPool mockVmPools() {
        VmPool pool = new VmPool();
        pool.setName("simplePoolName");
        pool.setClusterId(clusterId);
        pool.setVmPoolId(vmPoolId);
        return pool;
    }

    private void mockDbDao() {
        mockClusterDao();
        mockVMTemplateDao();
        mockStoragePoolDao();
    }


    protected void setupForStorageTests() {
        doReturn(multipleSdValidator).when(command).getStorageDomainsValidator(any(), any());
    }
}
