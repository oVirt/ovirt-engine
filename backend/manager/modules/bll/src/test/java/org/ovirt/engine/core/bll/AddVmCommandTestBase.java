package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public abstract class AddVmCommandTestBase<T extends AddVmCommand<?>> extends BaseCommandTest {
    protected static final int TOTAL_NUM_DOMAINS = 2;
    private static final Guid STORAGE_DOMAIN_ID_1 = Guid.newGuid();
    private static final Guid STORAGE_DOMAIN_ID_2 = Guid.newGuid();
    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final int NUM_DISKS_STORAGE_DOMAIN_1 = 3;
    private static final int NUM_DISKS_STORAGE_DOMAIN_2 = 3;
    protected static final String CPU_ID = "0";
    private static final int MAX_MEMORY_SIZE = 4096;

    @Mock
    CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    OsRepository osRepository;

    @Mock
    VmDeviceUtils vmDeviceUtils;

    @Mock
    MacPoolPerCluster macPoolPerCluster;

    @Mock
    protected StorageDomainValidator storageDomainValidator;
    private VmTemplate vmTemplate;
    protected VM vm;
    protected Cluster cluster;
    protected StoragePool storagePool;

    @Mock
    SnapshotDao snapshotDao;

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
    VmDeviceDao vmDeviceDao;

    @Mock
    DiskImageDao diskImageDao;

    @Spy
    @InjectMocks
    VmHandler vmHandler;

    @Spy
    @InjectMocks
    protected T cmd = createCommand();

    protected abstract T createCommand();

    private void initOsRepository() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    @Before
    public void setUp() {
        initOsRepository();

        vmHandler.init();

        initVmTemplate();
        cmd.setVmTemplate(vmTemplate);
        cmd.setVmTemplateId(vmTemplate.getId());

        initCluster();
        cmd.setClusterId(cluster.getId());
        cmd.setCluster(cluster);

        initStoragePool();
        cmd.setStoragePoolId(STORAGE_POOL_ID);
        cmd.setStoragePool(storagePool);

        mockOtherDependencies();
    }

    protected void mockOtherDependencies() {
        doReturn(storageDomainValidator).when(cmd).createStorageDomainValidator(any(StorageDomain.class));
    }

    protected void generateStorageToDisksMap() {
        cmd.storageToDisksMap = new HashMap<>();
        cmd.storageToDisksMap.put(STORAGE_DOMAIN_ID_1, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_1));
        cmd.storageToDisksMap.put(STORAGE_DOMAIN_ID_2, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_2));
    }

    private static List<DiskImage> generateDisksList(int size) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            DiskImage diskImage = createDiskImage();
            disksList.add(diskImage);
        }
        return disksList;
    }

    protected static DiskImage createDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID_1)));
        return diskImage;
    }

    private List<DiskImage> createDiskSnapshot(Guid diskId, int numOfImages) {
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

    protected void initDestSDs() {
        StorageDomain sd1 = new StorageDomain();
        StorageDomain sd2 = new StorageDomain();
        sd1.setId(STORAGE_DOMAIN_ID_1);
        sd2.setId(STORAGE_DOMAIN_ID_2);
        sd1.setStatus(StorageDomainStatus.Active);
        sd2.setStatus(StorageDomainStatus.Active);
        cmd.destStorages.put(STORAGE_DOMAIN_ID_1, sd1);
        cmd.destStorages.put(STORAGE_DOMAIN_ID_2, sd2);
    }

    protected void initVM() {
        vm = new VM();
        VmDynamic dynamic = new VmDynamic();
        VmStatic stat = new VmStatic();
        stat.setVmtGuid(Guid.newGuid());
        stat.setName("testVm");
        stat.setPriority(1);
        stat.setMaxMemorySizeMb(MAX_MEMORY_SIZE);
        vm.setStaticData(stat);
        vm.setDynamicData(dynamic);
        vm.setSingleQxlPci(false);
    }

    protected void initVmTemplate() {
        vmTemplate = new VmTemplate();
        vmTemplate.setStoragePoolId(STORAGE_POOL_ID);
        DiskImage image = createDiskImageTemplate();
        vmTemplate.getDiskTemplateMap().put(image.getImageId(), image);
        HashMap<Guid, DiskImage> diskImageMap = new HashMap<>();
        DiskImage diskImage = createDiskImage();
        diskImageMap.put(diskImage.getId(), diskImage);
        vmTemplate.setDiskImageMap(diskImageMap);
    }

    protected void initCluster() {
        cluster = new Cluster();
        cluster.setClusterId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.v4_0);
        cluster.setCpuName("Intel Conroe Family");
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setStoragePoolId(STORAGE_POOL_ID);
    }

    protected void initStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(STORAGE_POOL_ID);
        storagePool.setStatus(StoragePoolStatus.Up);
    }

    private static DiskImage createDiskImageTemplate() {
        DiskImage i = new DiskImage();
        i.setImageId(Guid.newGuid());
        i.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID_1)));
        return i;
    }

    protected void initCommandMethods() {
        doReturn(true).when(cmd).canAddVm(anyList(), anyString(), any(Guid.class), anyInt());
    }

    protected void mockStorageDomainDaoGetAllForStoragePool() {
        when(sdDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(createStorageDomain()));
    }

    protected StorageDomain createStorageDomain() {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.Master);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setId(STORAGE_DOMAIN_ID_1);
        return sd;
    }

    protected void mockGetAllSnapshots() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Guid arg = (Guid) args[0];
            return createDiskSnapshot(arg, 3);
        }).when(diskImageDao).getAllSnapshotsForLeaf(any(Guid.class));
    }
}
