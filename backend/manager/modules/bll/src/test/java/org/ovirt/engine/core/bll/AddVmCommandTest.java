package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, VmHandler.class, Config.class, VmTemplateHandler.class })
public class AddVmCommandTest {

    private final int REQUIRED_DISK_SIZE_GB = 10;
    private final int AVAILABLE_SPACE_GB = 11;
    private final int USED_SPACE_GB = 4;
    private final Guid STORAGE_POOL_ID = Guid.NewGuid();
    private final Guid STORAGE_DOMAIN_ID = Guid.NewGuid();
    private VmTemplate vmTemplate = null;

    @Mock
    DbFacade db;

    @Mock
    StorageDomainDAO sdDAO;

    @Mock
    VmTemplateDAO vmTemplateDAO;

    @Mock
    VmDAO vmDAO;

    @Mock
    DiskImageDAO diskImageDAO;

    @Mock
    StorageDomainDynamicDAO storageDomainDynamicDAO;

    @Mock
    BackendInternal backend;

    @Mock
    VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    VdsGroupDAO vdsGroupDAO;

    @Mock
    SnapshotDao snapshotDao;

    public AddVmCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
        mockStatic(VmHandler.class);
        mockStatic(Config.class);
        mockStatic(VmTemplateHandler.class);
    }

    @Before
    public void testSetup() {
        mockBackend();
        mockDbFacade();
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        setupAllMocks();
        VM vm = createVm(REQUIRED_DISK_SIZE_GB);
        AddVmFromTemplateCommand<AddVmFromTemplateParameters> cmd = createVmFromTemplateCommand(vm);
        mockStorageDomainDaoGetAllStoragesForPool(AVAILABLE_SPACE_GB);
        mockUninterestingMethods(cmd);
        assertFalse("If the disk is too big, canDoAction should fail", cmd.canDoAction());
        assertTrue("canDoAction failed for the wrong reason",
                cmd.getReturnValue()
                        .getCanDoActionMessages()
                        .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddVm() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 20;
        final int sizeRequired = 5;
        final int pctRequired = 10;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertTrue("vm could not be added", cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
    }

    @Test
    public void canAddVmFailSpaceThreshold() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int sizeRequired = 10;
        final int pctRequired = 0;
        final int domainSizeGB = 4;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertFalse("vm could not be added", cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
        assertTrue("canDoAction failed for the wrong reason",
                reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddVmFailPctThreshold() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int sizeRequired = 0;
        final int pctRequired = 95;
        final int domainSizeGB = 10;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertFalse("vm could not be added", cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
        assertTrue("canDoAction failed for the wrong reason",
                reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddThinVmFromTemplateWithManyDisks() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 20;
        final int sizeRequired = 10;
        final int pctRequired = 10;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);

        // Adding 10 disks, which each one should consume the default sparse size (which is 1GB).
        setNewDisksForTemplate(10, cmd.getVmTemplate().getDiskMap());
        doReturn(createVmTemplate()).when(cmd).getVmTemplate();
        assertFalse("Thin vm could not be added due to storage sufficient", cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
        assertTrue("canDoAction failed for insufficient disk size",
                 reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }


    @Test
    public void canAddCloneVmFromTemplate() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 15;
        final int sizeRequired = 4;
        final int pctRequired = 10;
        AddVmFromTemplateCommand<AddVmFromTemplateParameters> cmd = setupCanAddVmFromTemplateTests(domainSizeGB, sizeRequired, pctRequired);

        // Set new Disk Image map with 3GB.
        setNewImageDiskMapForTemplate(cmd, new Long("3000000000"), cmd.getVmTemplate().getDiskImageMap());
        assertFalse("Clone vm could not be added due to storage sufficient", cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
        assertTrue("canDoAction failed for insufficient disk size",
                 reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddCloneVmFromTemplateInvalidPercentage() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 30;
        final int sizeRequired = 6;
        final int pctRequired = 53;
        AddVmFromTemplateCommand<AddVmFromTemplateParameters> cmd =
                setupCanAddVmFromTemplateTests(domainSizeGB, sizeRequired, pctRequired);
        setNewImageDiskMapForTemplate(cmd, new Long("3000000000"), cmd.getVmTemplate().getDiskImageMap());
        assertFalse("Thin vm could not be added due to storage sufficient",
                cmd.CanAddVm(reasons, Arrays.asList(createStorageDomain(domainSizeGB))));
        assertTrue("canDoAction failed for insufficient disk size",
                 reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddCloneVmFromSnapshotSnapshotDoesNotExist() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 15;
        final int sizeRequired = 4;
        final int pctRequired = 10;
        final Guid sourceSnapshotId = Guid.NewGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = setupCanAddVmFromSnapshotTests(domainSizeGB, sizeRequired, pctRequired,sourceSnapshotId);
        cmd.getVm().setvm_name("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        assertFalse("Clone vm should have failed due to non existing snapshot id", cmd.canDoAction());
        reasons = cmd.getReturnValue().getCanDoActionMessages();
        assertTrue("Clone vm should have failed due to non existing snapshot id",
                 reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST.toString()));
    }

    @Test
    public void canAddCloneVmFromSnapshotNoConfiguration() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 15;
        final int sizeRequired = 4;
        final int pctRequired = 10;
        final Guid sourceSnapshotId = Guid.NewGuid();
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = setupCanAddVmFromSnapshotTests(domainSizeGB, sizeRequired, pctRequired,sourceSnapshotId);
        cmd.getVm().setvm_name("vm1");
        mockNonInterestingMethodsForCloneVmFromSnapshot(cmd);
        when(snapshotDao.get(sourceSnapshotId)).thenReturn(new Snapshot());
        assertFalse("Clone vm should have failed due to non existing vm configuration", cmd.canDoAction());
        reasons = cmd.getReturnValue().getCanDoActionMessages();
        assertTrue("Clone vm should have failed due to no configuration id",
                 reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION.toString()));

    }

    protected void mockNonInterestingMethodsForCloneVmFromSnapshot(AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd) {
        mockUninterestingMethods(cmd);
        doReturn(true).when(cmd).checkCpuSockets();
        doReturn(null).when(cmd).getVmFromConfiguration();
    }

    private VmTemplate setupSelectStorageDomainTests(final int domainSpaceGB,
            final int sizeRequired,
            final int pctRequired) {
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDAOGetForStoragePool(domainSpaceGB);
        mockGetImageDomainsListVdsCommand();
        mockConfig();
        mockConfigSizeRequirements(sizeRequired, pctRequired);
        VmTemplate template = new VmTemplate();
        template.setstorage_pool_id(Guid.NewGuid());
        DiskImage image = new DiskImage();
        template.addDiskImage(image);
        return template;
    }

    private AddVmFromTemplateCommand<AddVmFromTemplateParameters> createVmFromTemplateCommand(VM vm) {
        AddVmFromTemplateParameters param = new AddVmFromTemplateParameters();
        param.setVm(vm);
        AddVmFromTemplateCommand<AddVmFromTemplateParameters> concrete = new AddVmFromTemplateCommand<AddVmFromTemplateParameters>(param);
        return spy(concrete);
    }

    private AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> createVmFromSnapshotCommand(VM vm,Guid sourceSnapshotId) {
        AddVmFromSnapshotParameters param = new AddVmFromSnapshotParameters();
        param.setVm(vm);
        param.setSourceSnapshotId(sourceSnapshotId);
        param.setStorageDomainId(Guid.NewGuid());
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> concrete = new AddVmFromSnapshotCommand<AddVmFromSnapshotParameters>(param);
        return spy(concrete);
    }

    private AddVmFromTemplateCommand<AddVmFromTemplateParameters> setupCanAddVmFromTemplateTests(final int domainSizeGB,
            final int sizeRequired,
            final int pctRequired) {
        VM vm = initializeMock(domainSizeGB, sizeRequired, pctRequired);
        AddVmFromTemplateCommand<AddVmFromTemplateParameters> cmd = createVmFromTemplateCommand(vm);
        initCommandMethods(cmd);
        return cmd;
    }

    private AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> setupCanAddVmFromSnapshotTests(final int domainSizeGB,
            final int sizeRequired,
            final int pctRequired, Guid sourceSnapshotId) {
        VM vm = initializeMock(domainSizeGB, sizeRequired, pctRequired);
        initializeVmDAOMock(vm);
        AddVmFromSnapshotCommand<AddVmFromSnapshotParameters> cmd = createVmFromSnapshotCommand(vm,sourceSnapshotId);
        initCommandMethods(cmd);
        return cmd;
    }

    private void initializeVmDAOMock(VM vm) {
        when(vmDAO.getById(Matchers.<Guid> any(Guid.class))).thenReturn(vm);
    }

    private AddVmCommand<VmManagementParametersBase> setupCanAddVmTests(final int domainSizeGB,
            final int sizeRequired,
            final int pctRequired) {
        VM vm = initializeMock(domainSizeGB, sizeRequired, pctRequired);
        AddVmCommand<VmManagementParametersBase> cmd = createCommand(vm);
        initCommandMethods(cmd);
        return cmd;
    }

    private <T extends VmManagementParametersBase> void initCommandMethods(AddVmCommand<T> cmd) {
        doReturn(Guid.NewGuid()).when(cmd).getStoragePoolId();
        doReturn(true).when(cmd).canAddVm(Matchers.<ArrayList> any(ArrayList.class),
                anyInt(), anyString(), Matchers.<Guid> any(Guid.class), anyInt());
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
    }

    private VM initializeMock(final int domainSizeGB, final int sizeRequired, final int pctRequired) {
        mockVmTemplateDAOReturnVmTemplate();
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDAOGetForStoragePool(domainSizeGB);
        mockStorageDomainDAOGet(domainSizeGB);
        mockStorageDomainDynamicDAOGet(domainSizeGB, USED_SPACE_GB);
        mockGetImageDomainsListVdsCommand();
        mockConfig();
        mockConfigSizeRequirements(sizeRequired, pctRequired);
        VM vm = createVm(8);
        return vm;
    }

     private void setNewDisksForTemplate(int numberOfNewDisks, Map<String, DiskImage> disksMap) {
         for (int newDiskInd = 0; newDiskInd < numberOfNewDisks; newDiskInd++) {
             DiskImage diskImageTempalte = new DiskImage();
             diskImageTempalte.setId(Guid.NewGuid());
             disksMap.put(Guid.NewGuid().toString(), diskImageTempalte);
         }
     }

    private void setNewImageDiskMapForTemplate(AddVmFromTemplateCommand<AddVmFromTemplateParameters> cmd,
            long diskSize,
            Map<String, DiskImage> diskImageMap) {
        DiskImage diskImage = new DiskImage();
        diskImage.setactual_size(diskSize);
        diskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(STORAGE_DOMAIN_ID)));
        diskImageMap.put(Guid.NewGuid().toString(), diskImage);
        cmd.storageToDisksMap = new HashMap<Guid, List<DiskImage>>();
        cmd.storageToDisksMap.put(STORAGE_DOMAIN_ID, new ArrayList<DiskImage>(diskImageMap.values()));
    }

    private void setupAllMocks() {
        mockVmDAOGetById();
        mockStorageDomainDAOGetForStoragePool();
        mockVmTemplateDAOReturnVmTemplate();
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDynamicDAOGet();
        mockVdsGroupDAOGet();
        mockGetImageDomainsListVdsCommand();
        mockVmHandler();
        mockConfig();
        mockConfigSizeDefaults();
    }

    private void mockBackend() {
        when(Backend.getInstance()).thenReturn(backend);
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setReturnValue(Boolean.FALSE);
        when(backend.runInternalQuery(Matchers.<VdcQueryType> any(VdcQueryType.class),
                Matchers.any(VdcQueryParametersBase.class))).thenReturn(returnValue);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
    }

    private void mockDbFacade() {
        when(db.getVmDAO()).thenReturn(vmDAO);
        when(db.getStorageDomainDAO()).thenReturn(sdDAO);
        when(db.getVmTemplateDAO()).thenReturn(vmTemplateDAO);
        when(db.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(db.getStorageDomainDynamicDAO()).thenReturn(storageDomainDynamicDAO);
        when(db.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
        when(db.getSnapshotDao()).thenReturn(snapshotDao);
        when(DbFacade.getInstance()).thenReturn(db);
    }

    private void mockVmDAOGetById() {
        when(vmDAO.getById(any(Guid.class))).thenReturn(null);
    }

    private void mockStorageDomainDAOGetForStoragePool(int domainSpaceGB) {
        when(sdDAO.getForStoragePool(Matchers.<Guid> any(Guid.class), Matchers.<NGuid> any(NGuid.class))).thenReturn(createStorageDomain(domainSpaceGB));
    }

    private void mockStorageDomainDAOGet(final int domainSpaceGB) {
        doAnswer(new Answer<storage_domains>() {

            @Override
            public storage_domains answer(InvocationOnMock invocation) throws Throwable {
                storage_domains result = createStorageDomain(domainSpaceGB);
                result.setId((Guid)invocation.getArguments()[0]);
                return result;
            }

        }).when(sdDAO).get(any(Guid.class));
    }

    private void mockStorageDomainDaoGetAllStoragesForPool(int domainSpaceGB) {
        when(sdDAO.getAllForStoragePool(any(Guid.class))).thenReturn(Arrays.asList(createStorageDomain(domainSpaceGB)));
    }

    private void mockStorageDomainDAOGetForStoragePool() {
        mockStorageDomainDAOGetForStoragePool(AVAILABLE_SPACE_GB);
    }

    private void mockVmTemplateDAOReturnVmTemplate() {
        when(vmTemplateDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(createVmTemplate());
    }

    private VmTemplate createVmTemplate() {
        if (vmTemplate == null) {
            vmTemplate = new VmTemplate();
            vmTemplate.setstorage_pool_id(STORAGE_POOL_ID);
            vmTemplate.addDiskImage(createDiskImageTemplate());
            Map<String, DiskImage> diskImageMap = new HashMap<String, DiskImage>(1);
            diskImageMap.put("disk1", createDiskImage(REQUIRED_DISK_SIZE_GB));
            vmTemplate.setDiskImageMap(diskImageMap);
        }
        return vmTemplate;
    }

    private DiskImage createDiskImageTemplate() {
        DiskImage i = new DiskImage();
        i.setSizeInGigabytes(USED_SPACE_GB + AVAILABLE_SPACE_GB);
        i.setactual_size((long) REQUIRED_DISK_SIZE_GB * 1024L * 1024L * 1024L);
        i.setId(Guid.NewGuid());
        i.setstorage_ids(new ArrayList<Guid>(Arrays.asList(STORAGE_DOMAIN_ID)));
        return i;
    }

    private void mockDiskImageDAOGetSnapshotById() {
        when(diskImageDAO.getSnapshotById(Matchers.<Guid> any(Guid.class))).thenReturn(createDiskImage(REQUIRED_DISK_SIZE_GB));
    }

    private DiskImage createDiskImage(int size) {
        DiskImage img = new DiskImage();
        img.setSizeInGigabytes(size);
        img.setActualSize(size);
        img.setimage_group_id(Guid.NewGuid());
        img.setstorage_ids(new ArrayList<Guid>(Arrays.asList(STORAGE_DOMAIN_ID)));
        return img;
    }

    private void mockStorageDomainDynamicDAOGet(int freeSpace, int usedSpace) {
        when(storageDomainDynamicDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(createStorageDomainDynamic(freeSpace,
                usedSpace));
    }

    private void mockStorageDomainDynamicDAOGet() {
        mockStorageDomainDynamicDAOGet(AVAILABLE_SPACE_GB, USED_SPACE_GB);
    }

    public void mockVdsGroupDAOGet() {
        when(vdsGroupDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(new VDSGroup());
    }

    private storage_domain_dynamic createStorageDomainDynamic(final int freeSpace, final int usedSpace) {
        return new storage_domain_dynamic(freeSpace, Guid.NewGuid(), usedSpace);
    }

    private void mockGetImageDomainsListVdsCommand() {
        ArrayList<Guid> guids = new ArrayList<Guid>(1);
        guids.add(Guid.NewGuid());
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(guids);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GetImageDomainsList),
                Matchers.<VDSParametersBase> any(VDSParametersBase.class))).thenReturn(returnValue);
    }

    private storage_domains createStorageDomain(int availableSpace) {
        storage_domains sd = new storage_domains();
        sd.setstorage_domain_type(StorageDomainType.Master);
        sd.setstatus(StorageDomainStatus.Active);
        sd.setavailable_disk_size(availableSpace);
        sd.setused_disk_size(USED_SPACE_GB);
        sd.setId(STORAGE_DOMAIN_ID);
        return sd;
    }

    private void mockVmHandler() {
        when(
                VmHandler.VerifyAddVm(
                        Matchers.<ArrayList> any(ArrayList.class),
                        anyInt(),
                        Matchers.<VmTemplate> anyObject(),
                        Matchers.<Guid> any(Guid.class),
                        anyInt()
                        )).thenReturn(Boolean.TRUE);

    }

    private void mockConfig() {
        when(Config.<Object> GetValue(ConfigValues.PredefinedVMProperties, "3.0")).thenReturn("");
        when(Config.<Object> GetValue(ConfigValues.UserDefinedVMProperties, "3.0")).thenReturn("");
        when(Config.<Object> GetValue(ConfigValues.InitStorageSparseSizeInGB)).thenReturn(new Integer("1"));
    }

    private void mockConfigSizeRequirements(int requiredSpaceBufferInGB, int requiredSpacePercent) {
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(requiredSpaceBufferInGB);
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(requiredSpacePercent);
    }

    private void mockConfigSizeDefaults() {
        int requiredSpaceBufferInGB = 5;
        int requiredSpacePercent = 0;
        mockConfigSizeRequirements(requiredSpaceBufferInGB, requiredSpacePercent);
    }

    private VM createVm(int diskSize) {
        VM vm = new VM();
        VmDynamic dynamic = new VmDynamic();
        VmStatic stat = new VmStatic();
        stat.setvmt_guid(Guid.NewGuid());
        stat.setvm_name("testVm");
        stat.setpriority(1);
        vm.setStaticData(stat);
        vm.setDynamicData(dynamic);
        return vm;
    }

    private AddVmCommand<VmManagementParametersBase> createCommand(VM vm) {
        VmManagementParametersBase param = new VmManagementParametersBase(vm);
        AddVmCommand<VmManagementParametersBase> concrete = new AddVmCommandDummy(param);
        return spy(concrete);
    }

    private <T extends VmManagementParametersBase> void mockUninterestingMethods(AddVmCommand<T> spy) {
        doReturn(true).when(spy).isVmNameValidLength(Matchers.<VM> any(VM.class));
        doReturn(STORAGE_POOL_ID).when(spy).getStoragePoolId();
        doReturn(createVmTemplate()).when(spy).getVmTemplate();
        doReturn(true).when(spy).areParametersLegal(Matchers.<ArrayList> any(ArrayList.class));
        doReturn(Collections.<VmNetworkInterface> emptyList()).when(spy).getVmInterfaces();
        doReturn(Collections.<DiskImageBase> emptyList()).when(spy).getVmDisks();
        spy.setVmTemplateId(Guid.NewGuid());
    }

    private class AddVmCommandDummy extends AddVmCommand<VmManagementParametersBase> {

        private static final long serialVersionUID = -5873465232404820067L;

        public AddVmCommandDummy(VmManagementParametersBase parameters) {
            super(parameters);
        }

        @Override
        protected int getNeededDiskSize(Guid domainId) {
            return getBlockSparseInitSizeInGB() * getVmTemplate().getDiskMap().size();
        }

    }
}
