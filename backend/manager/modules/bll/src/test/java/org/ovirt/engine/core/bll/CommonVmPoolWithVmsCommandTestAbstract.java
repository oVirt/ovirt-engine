package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
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
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class CommonVmPoolWithVmsCommandTestAbstract {

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
    protected VmTemplateDAO vmTemplateDAO;

    @Mock
    protected StorageDomainDAO storageDomainDAO;

    public CommonVmPoolWithVmsCommandTestAbstract() {
    }

    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
        mockStaticClasses();
        mockGlobalParameters();
        mockVds();
        mockVmHandler();
        mockConfig();
        mockDbDAO();
    }

    private void mockVds() {
        mockVdsBroker();
        mockIsValidVdsCommand();
        mockGetImageDomainsListVdsCommand(100, 100);
    }

    private void mockGlobalParameters() {
        testVm = mockVm();
        vmPools = mockVmPools();
        vdsGroup = mockVdsGroup();
        vmTemplate = mockVmTemplate();
    }

    private void mockStaticClasses() {
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
        mockStatic(Config.class);
        mockStatic(VmHandler.class);
        mockStatic(VmTemplateHandler.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void mockVmHandler() {
        when(VmHandler.isMemorySizeLegal(Matchers.<VmOsType> anyObject(),
                anyInt(),
                Matchers.<ArrayList> any(ArrayList.class),
                Matchers.<String> anyObject())).thenReturn(true);

        when(VmHandler.VerifyAddVm(
                        Matchers.<ArrayList> any(ArrayList.class),
                        anyInt(),
                        Matchers.<VmTemplate> anyObject(),
                        Matchers.<Guid> any(Guid.class),
                        anyInt()
                        )).thenReturn(Boolean.TRUE);
    }

    private void mockVdsBroker() {
        when(Backend.getInstance()).thenReturn(backend);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
    }

    private void mockIsValidVdsCommand() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(Boolean.TRUE);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.IsValid),
                Matchers.<VDSParametersBase> any(VDSParametersBase.class))).thenReturn(returnValue);
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

    protected void mockGetStorageDomainList(int availableDiskSizeFirstDomain,
            int availableDiskSizeSecondDomain) {
        // Mock Dao
        storageDomainsList =
                getStorageDomainList(availableDiskSizeFirstDomain, availableDiskSizeSecondDomain);
        mockDiskImageDAO();
        mockStorageDomainDAO(storageDomainsList);
    }

    private void mockVMPoolDAO() {
        when(dbFacada.getVmPoolDAO()).thenReturn(vmPoolDAO);
    }

    private void mockVMTemplateDAO() {
        when(dbFacada.getVmTemplateDAO()).thenReturn(vmTemplateDAO);
        when(vmTemplateDAO.get(vmTemplateId)).thenReturn(vmTemplate);
    }

    private void mockVdsGroupDAO() {
        when(dbFacada.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
        when(vdsGroupDAO.get(vdsGroupId)).thenReturn(vdsGroup);
    }

    private void mockDiskImageDAO() {
        when(dbFacada.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(diskImageDAO.getSnapshotById(Matchers.<Guid> any(Guid.class))).thenReturn(getDiskImageList().get(0));
    }

    private void mockStorageDomainDAO(List<storage_domains> storageDomains) {
        when(dbFacada.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        for (storage_domains storageDomain : storageDomains) {
            when(storageDomainDAO.getForStoragePool(storageDomain.getId(), storagePoolId)).thenReturn(storageDomain);
        }
    }

    /**
     * Mock a VM.
     */
    private VM mockVm() {
        VM vm = new VM();
        vm.setstatus(VMStatus.Down);
        vm.setvmt_guid(vmTemplateId);
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

    private List<Guid> mockStorageGuidList(List<storage_domains> storageDomains) {
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
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setvds_group_id(vdsGroupId);
        vdsGroup.setcompatibility_version(new Version());
        vdsGroup.setstorage_pool_id(storagePoolId);
        return vdsGroup;
    }

    /**
     * Mock VM Tempalte.
     */
    private VmTemplate mockVmTemplate() {
        VmTemplate vmTemplate = new VmTemplate();
        vmTemplate.setId(vmTemplateId);
        vmTemplate.setstorage_pool_id(storagePoolId);
        setDiskList(vmTemplate);

        return vmTemplate;
    }

    private void setDiskList(VmTemplate vmTemplate) {
        for (DiskImage diskImage : getDiskImageList()) {
            vmTemplate.getDiskList().add(diskImage);
        }
        Map<String, DiskImage> diskImageTemplate = getDiskImageTempalteList();
        for (String key : diskImageTemplate.keySet()) {
            vmTemplate.getDiskMap().put(key, diskImageTemplate.get(key));
        }
    }

    private List<DiskImage> getDiskImageList() {
        List<DiskImage> diskList = new ArrayList<DiskImage>();
        DiskImage diskImage = new DiskImage();
        diskImage.setimage_group_id(Guid.NewGuid());
        diskImage.setstorage_ids(new ArrayList<Guid>());
        diskList.add(diskImage);
        diskImage = new DiskImage();
        diskImage.setimage_group_id(Guid.NewGuid());
        diskImage.setstorage_ids(new ArrayList<Guid>());
        diskList.add(diskImage);
        return diskList;
    }

    private Map<String, DiskImage> getDiskImageTempalteList() {
        Map<String, DiskImage> diskTemplateList = new HashMap<String, DiskImage>();
        DiskImage diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.NewGuid());
        diskImageTemplate.setstorage_ids(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId().toString(), diskImageTemplate);
        diskImageTemplate = new DiskImage();
        diskImageTemplate.setId(Guid.NewGuid());
        diskImageTemplate.setstorage_ids(new ArrayList<Guid>());
        diskTemplateList.put(diskImageTemplate.getId().toString(), diskImageTemplate);
        return diskTemplateList;
    }

    private void mockConfig() {
        when(Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthWindows)).thenReturn(15);
        when(Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthNonWindows)).thenReturn(64);
        when(Config.<Integer> GetValue(ConfigValues.MaxVmsInPool)).thenReturn(87);
        when(Config.<Integer> GetValue(ConfigValues.VMMinMemorySizeInMB)).thenReturn(256);
        when(Config.<Integer> GetValue(ConfigValues.VM32BitMaxMemorySizeInMB)).thenReturn(2048);
        when(Config.<Integer> GetValue(ConfigValues.VM64BitMaxMemorySizeInMB)).thenReturn(262144);
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(10);
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(1);
        when(Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB)).thenReturn(1);
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
        vm_pools vmPools = new vm_pools();
        vmPools.setvm_pool_name("simplePoolName");
        vmPools.setvds_group_id(vdsGroupId);
        vmPools.setvm_pool_id(vmPoolId);
        return vmPools;
    }

    private void mockDbFacade() {
        when(DbFacade.getInstance()).thenReturn(dbFacada);
    }

    private void mockDbDAO() {
        mockDbFacade();
        mockVdsGroupDAO();
        mockVMPoolDAO();
        mockVMTemplateDAO();
    }
}
