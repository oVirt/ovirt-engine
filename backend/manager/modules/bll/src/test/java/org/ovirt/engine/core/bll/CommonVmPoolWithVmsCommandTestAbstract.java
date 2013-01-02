package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
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
import org.ovirt.engine.core.dao.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

public abstract class CommonVmPoolWithVmsCommandTestAbstract {
    @Rule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxVmNameLengthWindows, 15),
            mockConfig(ConfigValues.MaxVmNameLengthNonWindows, 64),
            mockConfig(ConfigValues.MaxVmsInPool, 87),
            mockConfig(ConfigValues.VM32BitMaxMemorySizeInMB, 2048),
            mockConfig(ConfigValues.VM64BitMaxMemorySizeInMB, 262144),
            mockConfig(ConfigValues.FreeSpaceLow, 10),
            mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, 1),
            mockConfig(ConfigValues.InitStorageSparseSizeInGB, 1)
            );

    private final Guid vdsGroupId = Guid.NewGuid();
    protected final Guid firstStorageDomainId = Guid.NewGuid();
    private final Guid secondStorageDomainId = Guid.NewGuid();
    private final Guid storagePoolId = Guid.NewGuid();
    private final Guid vmTemplateId = Guid.NewGuid();
    protected Guid vmPoolId = Guid.NewGuid();
    private VDSGroup vdsGroup;
    protected VM testVm;
    protected vm_pools vmPools;
    protected static int VM_COUNT = 5;
    protected static int DISK_SIZE = 100000;
    protected VmTemplate vmTemplate;
    protected storage_pool storage_pool;
    protected List<storage_domains> storageDomainsList;

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

    /**
     * The command under test.
     */
    protected CommonVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command;

    protected abstract CommonVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand();

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
        doReturn(true).when(command).isMemorySizeLegal(any(Version.class));
        doReturn(true).when(command).verifyAddVM();
    }

    private void mockVds() {
        mockGetImageDomainsListVdsCommand(100, 100);
    }

    private void mockGlobalParameters() {
        testVm = mockVm();
        vmPools = mockVmPools();
        vdsGroup = mockVdsGroup();
        vmTemplate = mockVmTemplate();
        storage_pool = mockStoragePool();
    }

    protected void mockGetImageDomainsListVdsCommand(int availableDiskSizeFirstDomain,
            int availableDiskSizeSecondDomain) {
        mockGetStorageDomainList(availableDiskSizeFirstDomain, availableDiskSizeSecondDomain);

        // Mock VDS return value.
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(mockStorageGuidList(storageDomainsList));
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GetImageDomainsList),
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
        when(storagePoolDAO.get(storagePoolId)).thenReturn(storage_pool);
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

    private void mockStorageDomainDAO(List<storage_domains> storageDomains) {
        doReturn(storageDomainDAO).when(command).getStorageDomainDAO();
        for (storage_domains storageDomain : storageDomains) {
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
        return vm;
    }

    private storage_domains mockFirstStorageDomain(int availabeDiskSize) {
        storage_domains storageDomain = new storage_domains();
        storageDomain.setavailable_disk_size(availabeDiskSize);
        storageDomain.setstatus(StorageDomainStatus.Active);
        storageDomain.setId(firstStorageDomainId);
        storageDomain.setstorage_domain_type(StorageDomainType.Data);
        return storageDomain;
    }

    private storage_domains mockSecondStorageDomain(int availabeDiskSize) {
        storage_domains storageDomain = new storage_domains();
        storageDomain.setavailable_disk_size(availabeDiskSize);
        storageDomain.setstatus(StorageDomainStatus.Active);
        storageDomain.setId(secondStorageDomainId);
        storageDomain.setstorage_domain_type(StorageDomainType.Data);
        return storageDomain;
    }

    private static List<Guid> mockStorageGuidList(List<storage_domains> storageDomains) {
        List<Guid> storageGuidList = new ArrayList<Guid>();
        for (storage_domains storageDomain : storageDomains) {
            storageGuidList.add(storageDomain.getId());
        }
        return storageGuidList;
    }

    protected List<storage_domains> getStorageDomainList(int availableDiskSizeFirstDomain,
            int availableDiskSizeSecondDomain) {
        List<storage_domains> storageDomainList = new ArrayList<storage_domains>();
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
        return group;
    }

    /**
     * Mock VM Template.
     */
    private VmTemplate mockVmTemplate() {
        VmTemplate template = new VmTemplate();
        template.setId(vmTemplateId);
        template.setstorage_pool_id(storagePoolId);
        setDiskList(template);

        return template;
    }

    /**
     * Mock Storage Pool
     */
    private storage_pool mockStoragePool() {
        storage_pool storage_pool = new storage_pool();
        storage_pool.setstatus(StoragePoolStatus.Up);

        return storage_pool;
    }

    private static void setDiskList(VmTemplate vmTemplate) {
        for (DiskImage diskImage : getDiskImageList()) {
            vmTemplate.getDiskList().add(diskImage);
        }
        Map<Guid, DiskImage> diskImageTemplate = getDiskImageTempalteList();
        for (Guid key : diskImageTemplate.keySet()) {
            vmTemplate.getDiskMap().put(key, diskImageTemplate.get(key));
        }
    }

    private static List<DiskImage> getDiskImageList() {
        List<DiskImage> diskList = new ArrayList<DiskImage>();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.NewGuid());
        diskImage.setstorage_ids(new ArrayList<Guid>());
        diskList.add(diskImage);
        diskImage = new DiskImage();
        diskImage.setId(Guid.NewGuid());
        diskImage.setstorage_ids(new ArrayList<Guid>());
        diskList.add(diskImage);
        return diskList;
    }

    private static Map<Guid, DiskImage> getDiskImageTempalteList() {
        Map<Guid, DiskImage> diskTemplateList = new HashMap<Guid, DiskImage>();
        DiskImage diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.NewGuid());
        diskImageTemplate.setImageId(Guid.NewGuid());
        diskImageTemplate.setstorage_ids(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.NewGuid());
        diskImageTemplate.setImageId(Guid.NewGuid());
        diskImageTemplate.setstorage_ids(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId(), diskImageTemplate);
        return diskTemplateList;
    }

    private VmStatic getVmStatic() {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setos(VmOsType.Unassigned);
        vmStatic.setmem_size_mb(300);
        vmStatic.setis_stateless(false);
        vmStatic.setvmt_guid(vmTemplateId);
        return vmStatic;
    }

    /**
     * Mock VM pools.
     */
    private vm_pools mockVmPools() {
        vm_pools pool = new vm_pools();
        pool.setvm_pool_name("simplePoolName");
        pool.setvds_group_id(vdsGroupId);
        pool.setvm_pool_id(vmPoolId);
        return pool;
    }

    private void mockDbDAO() {
        mockVdsGroupDAO();
        mockVMPoolDAO();
        mockVMTemplateDAO();
        mockVmNetworkInterfaceDao();
        mockStoragePoolDAO();
    }
}
