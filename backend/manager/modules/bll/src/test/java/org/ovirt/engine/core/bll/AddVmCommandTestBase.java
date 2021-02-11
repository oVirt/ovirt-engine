package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.utils.InjectedMock;

public abstract class AddVmCommandTestBase<T extends AddVmCommand<?>> extends BaseCommandTest {
    protected static final int TOTAL_NUM_DOMAINS = 2;
    protected static final Guid STORAGE_DOMAIN_ID_1 = Guid.newGuid();
    protected static final Guid STORAGE_DOMAIN_ID_2 = Guid.newGuid();
    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final int NUM_DISKS_STORAGE_DOMAIN_1 = 3;
    private static final int NUM_DISKS_STORAGE_DOMAIN_2 = 3;
    private static final int MAX_MEMORY_SIZE = 4096;
    private static final int MEMORY_SIZE = 1024;

    @Mock
    @InjectedMock
    public OsRepository osRepository;

    @Mock
    private VmDeviceUtils vmDeviceUtils;

    @Mock
    protected StorageDomainValidator storageDomainValidator;
    private VmTemplate vmTemplate;
    protected VM vm;
    protected Cluster cluster;
    protected StoragePool storagePool;

    @Mock
    protected StorageDomainDao sdDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Spy
    @InjectMocks
    private VmHandler vmHandler;

    @Spy
    @InjectMocks
    protected T cmd = initCommand();

    private T initCommand() {
        initVM();
        return createCommand();
    }

    protected abstract T createCommand();

    @BeforeEach
    public void setUp() {
        initCluster();
        cmd.setClusterId(cluster.getId());
        cmd.setCluster(cluster);

        initStoragePool();
        cmd.setStoragePoolId(STORAGE_POOL_ID);
        cmd.setStoragePool(storagePool);

        mockOtherDependencies();

        generateStorageToDisksMap();
        initDestSDs();

        initVmTemplate();
        cmd.setVmTemplate(vmTemplate);
        cmd.setVmTemplateId(vmTemplate.getId());
    }

    protected void mockOtherDependencies() {
        doReturn(storageDomainValidator).when(cmd).createStorageDomainValidator(any());

        VmBase vmBase = new VmBase();
        vmBase.setBiosType(BiosType.Q35_SEA_BIOS);
        doReturn(vmBase).when(cmd).getVmBase(any());
    }

    private void generateStorageToDisksMap() {
        cmd.storageToDisksMap = new HashMap<>();
        cmd.storageToDisksMap.put(STORAGE_DOMAIN_ID_1, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_1, STORAGE_DOMAIN_ID_1));
        cmd.storageToDisksMap.put(STORAGE_DOMAIN_ID_2, generateDisksList(NUM_DISKS_STORAGE_DOMAIN_2, STORAGE_DOMAIN_ID_2));
    }

    protected static List<DiskImage> generateDisksList(int size, Guid sdId) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            DiskImage diskImage = createDiskImage(sdId);
            disksList.add(diskImage);
        }
        return disksList;
    }

    protected static DiskImage createDiskImage(Guid sdId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(sdId)));
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

    private void initDestSDs() {
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
        stat.setMemSizeMb(MEMORY_SIZE);
        stat.setMaxMemorySizeMb(MAX_MEMORY_SIZE);
        vm.setStaticData(stat);
        vm.setDynamicData(dynamic);
    }

    private void initVmTemplate() {
        vmTemplate = new VmTemplate();
        vmTemplate.setStoragePoolId(STORAGE_POOL_ID);
        vmTemplate.getDiskTemplateMap().putAll(cmd.storageToDisksMap.values().stream().flatMap(List::stream).collect(
                Collectors.toMap(DiskImage::getImageId, Function.identity())));
        DiskImage diskImage = createDiskImage(STORAGE_DOMAIN_ID_1);
        vmTemplate.setDiskImageMap(Collections.singletonMap(diskImage.getId(), diskImage));
    }

    protected void initCluster() {
        cluster = new Cluster();
        cluster.setClusterId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.v4_2);
        cluster.setCpuName("Intel Conroe Family");
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setStoragePoolId(STORAGE_POOL_ID);
        cluster.setBiosType(BiosType.I440FX_SEA_BIOS);
    }

    private void initStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(STORAGE_POOL_ID);
        storagePool.setStatus(StoragePoolStatus.Up);
    }

    protected void initCommandMethods() {
        doReturn(true).when(cmd).canAddVm(any(VmStatic.class), any(Guid.class), anyInt());
    }

    protected StorageDomain createStorageDomain(Guid sdId) {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setId(sdId);
        return sd;
    }

    protected void mockGetAllSnapshots() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Guid arg = (Guid) args[0];
            return createDiskSnapshot(arg, 3);
        }).when(diskImageDao).getAllSnapshotsForLeaf(any());
    }
}
