package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public abstract class CommonVmPoolWithVmsCommandTestAbstract {
    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxVmNameLengthWindows, 15),
            mockConfig(ConfigValues.MaxVmNameLengthNonWindows, 64),
            mockConfig(ConfigValues.MaxVmsInPool, 87),
            mockConfig(ConfigValues.VM32BitMaxMemorySizeInMB, 2048),
            mockConfig(ConfigValues.VM64BitMaxMemorySizeInMB, 262144),
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, 1),
            mockConfig(ConfigValues.InitStorageSparseSizeInGB, 1),
            mockConfig(ConfigValues.ValidNumOfMonitors, Arrays.asList("1,2,4".split(",")))
            );

    private final Guid vdsGroupId = Guid.newGuid();
    protected final Guid firstStorageDomainId = Guid.newGuid();
    private final Guid secondStorageDomainId = Guid.newGuid();
    private final Guid storagePoolId = Guid.newGuid();
    private final Guid vmTemplateId = Guid.newGuid();
    protected Guid vmPoolId = Guid.newGuid();
    private VDSGroup vdsGroup;
    protected VM testVm;
    protected VmPool vmPools;
    protected static int VM_COUNT = 5;
    protected static int DISK_SIZE = 100000;
    protected VmTemplate vmTemplate;
    protected StoragePool storagePool;
    protected List<StorageDomain> storageDomainsList;

    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    protected BackendInternal backend;

    @Mock
    protected DbFacade dbFacada;

    @Mock
    protected VdsGroupDAO vdsGroupDAO;

    @Mock
    protected DiskImageDAO diskImageDAO;

    @Mock
    protected VmPoolDAO vmPoolDAO;

    @Mock
    protected StoragePoolDAO storagePoolDAO;

    @Mock
    protected VmTemplateDAO vmTemplateDAO;

    @Mock
    protected VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Mock
    protected StorageDomainDAO storageDomainDAO;

    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    /**
     * The command under test.
     */
    protected CommonVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command;

    protected abstract CommonVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand();

    @Test
    public void validateSufficientSpaceOnDestinationDomains() {
        setupForStorageTests();
        assertTrue(command.checkDestDomains());
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void validateInsufficientSpaceOnDomains() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void validateDomainNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
        mockGlobalParameters();
        setUpCommand();
        mockVds();
        mockDbDAO();
    }

    protected void setUpCommand() {
        command = createCommand();
        doReturn(true).when(command).areTemplateImagesInStorageReady(any(Guid.class));
        doReturn(true).when(command).verifyAddVM();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).setAndValidateCpuProfile();
    }

    private void mockVds() {
        mockGetStorageDomainList(100, 100);
        mockGetImagesList();
    }

    private void mockGlobalParameters() {
        testVm = mockVm();
        vmPools = mockVmPools();
        vdsGroup = mockVdsGroup();
        vmTemplate = mockVmTemplate();
        storagePool = mockStoragePool();
    }

    private void mockGetImagesList() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(new ArrayList<Guid>());
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GetImagesList),
                Matchers.<VDSParametersBase> any(VDSParametersBase.class))).thenReturn(returnValue);
    }

    protected void mockGetStorageDomainList
            (int availableDiskSizeFirstDomain, int availableDiskSizeSecondDomain) {
        // Mock Dao
        storageDomainsList =
                getStorageDomainList(availableDiskSizeFirstDomain, availableDiskSizeSecondDomain);
        mockDiskImageDAO();
        mockStorageDomainDAO(storageDomainsList);
    }

    private void mockVMPoolDAO() {
        doReturn(vmPoolDAO).when(command).getVmPoolDAO();
    }

    private void mockStoragePoolDAO() {
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        when(storagePoolDAO.get(storagePoolId)).thenReturn(storagePool);
    }

    private void mockVMTemplateDAO() {
        doReturn(vmTemplateDAO).when(command).getVmTemplateDAO();
        when(vmTemplateDAO.get(vmTemplateId)).thenReturn(vmTemplate);
    }

    private void mockVmNetworkInterfaceDao() {
        when(dbFacada.getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDao);
        when(vmNetworkInterfaceDao.getAllForTemplate(vmTemplateId))
                .thenReturn(Collections.<VmNetworkInterface> emptyList());
    }

    private void mockVdsGroupDAO() {
        doReturn(vdsGroupDAO).when(command).getVdsGroupDAO();
        when(vdsGroupDAO.get(vdsGroupId)).thenReturn(vdsGroup);
    }

    private void mockDiskImageDAO() {
        when(diskImageDAO.getSnapshotById(Matchers.<Guid> any(Guid.class))).thenReturn(getDiskImageList().get(0));
    }

    private void mockStorageDomainDAO(List<StorageDomain> storageDomains) {
        doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
        for (StorageDomain storageDomain : storageDomains) {
            when(storageDomainDAO.getForStoragePool(storageDomain.getId(), storagePoolId)).thenReturn(storageDomain);
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

    private StorageDomain mockFirstStorageDomain(int availabeDiskSize) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setAvailableDiskSize(availabeDiskSize);
        storageDomain.setStatus(StorageDomainStatus.Active);
        storageDomain.setId(firstStorageDomainId);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        return storageDomain;
    }

    private StorageDomain mockSecondStorageDomain(int availabeDiskSize) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setAvailableDiskSize(availabeDiskSize);
        storageDomain.setStatus(StorageDomainStatus.Active);
        storageDomain.setId(secondStorageDomainId);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        return storageDomain;
    }

    protected List<StorageDomain> getStorageDomainList(int availableDiskSizeFirstDomain,
            int availableDiskSizeSecondDomain) {
        List<StorageDomain> storageDomainList = new ArrayList<StorageDomain>();
        storageDomainList.add(mockFirstStorageDomain(availableDiskSizeFirstDomain));
        storageDomainList.add(mockSecondStorageDomain(availableDiskSizeSecondDomain));
        return storageDomainList;
    }

    /**
     * Mock VDS Group.
     */
    private VDSGroup mockVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setvds_group_id(vdsGroupId);
        group.setcompatibility_version(new Version());
        group.setStoragePoolId(storagePoolId);
        group.setcpu_name("Intel Conroe Family");
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
        List<DiskImage> diskList = new ArrayList<DiskImage>();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<Guid>());
        diskList.add(diskImage);
        diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<Guid>());
        diskList.add(diskImage);
        return diskList;
    }

    private static Map<Guid, DiskImage> getDiskImageTempalteList() {
        Map<Guid, DiskImage> diskTemplateList = new HashMap<Guid, DiskImage>();
        DiskImage diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.newGuid());
        diskImageTemplate.setImageId(Guid.newGuid());
        diskImageTemplate.setStorageIds(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.newGuid());
        diskImageTemplate.setImageId(Guid.newGuid());
        diskImageTemplate.setStorageIds(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        return diskTemplateList;
    }

    private VmStatic getVmStatic() {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setOsId(OsRepository.DEFAULT_X86_OS);
        vmStatic.setNumOfMonitors(1);
        vmStatic.setMemSizeMb(300);
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
        pool.setVdsGroupId(vdsGroupId);
        pool.setVmPoolId(vmPoolId);
        return pool;
    }

    private void mockDbDAO() {
        mockVdsGroupDAO();
        mockVMPoolDAO();
        mockVMTemplateDAO();
        mockVmNetworkInterfaceDao();
        mockStoragePoolDAO();
    }


    protected void setupForStorageTests() {
        doReturn(multipleSdValidator).when(command).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }
}
