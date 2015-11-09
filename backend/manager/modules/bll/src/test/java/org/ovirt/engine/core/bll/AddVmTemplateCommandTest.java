package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for {@link AddVmTemplateCommand}
 */
public class AddVmTemplateCommandTest extends BaseCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.VmPriorityMaxValue, 100));

    private AddVmTemplateCommand<AddVmTemplateParameters> cmd;
    private VM vm;
    private VDSGroup vdsGroup;
    private Guid spId;
    @Mock
    private VmDao vmDao;
    @Mock
    private VdsGroupDao vdsGroupDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private OsRepository osRepository;
    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;
    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Before
    public void setUp() {
        // The VM to use
        Guid vmId = Guid.newGuid();
        Guid vdsGroupId = Guid.newGuid();
        spId = Guid.newGuid();

        vm = new VM();
        vm.setId(vmId);
        vm.setVdsGroupId(vdsGroupId);
        vm.setStoragePoolId(spId);
        vm.setVmOs(14);
        when(vmDao.get(vmId)).thenReturn(vm);

        // The cluster to use
        vdsGroup = new VDSGroup();
        vdsGroup.setCpuName("Intel Conroe Family");
        vdsGroup.setArchitecture(ArchitectureType.x86_64);
        vdsGroup.setId(vdsGroupId);
        vdsGroup.setStoragePoolId(spId);
        vdsGroup.setCompatibilityVersion(Version.v3_2);
        when(vdsGroupDao.get(vdsGroupId)).thenReturn(vdsGroup);
        AddVmTemplateParameters params = new AddVmTemplateParameters(vm, "templateName", "Template for testing");

        mockOsRepository();

        // Using the compensation constructor since the normal one contains DB access
        cmd = spy(new AddVmTemplateCommand<AddVmTemplateParameters>(params) {

            @Override
            protected void initUser() {
            }

            @Override
            protected List<DiskImage> getVmDisksFromDB() {
                return getDisksList(spId);
            }

            @Override
            protected void updateVmDevices() {
            }

            @Override
            public VM getVm() {
                return vm;
            }

            @Override
            public void separateCustomProperties(VmStatic parameterMasterVm) {
            }
        });

        doReturn(vmDao).when(cmd).getVmDao();
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDao();
        cmd.postConstruct();
        cmd.setVmId(vmId);
        cmd.setVdsGroupId(vdsGroupId);
    }

    protected void mockOsRepository() {
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        VmHandler.init();
        when(osRepository.isWindows(0)).thenReturn(true);
        when(osRepository.getMinimumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(0);
        when(osRepository.getMaximumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(100);
        when(osRepository.getArchitectureFromOS(14)).thenReturn(ArchitectureType.x86_64);
    }

    @Test
    public void testCanDoAction() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, EngineMessage.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
    }

    @Test
    // When Template by the same name already exists in the datacenter - fail.
    public void testCanDoActionDuplicateTemplateName() {
        doReturn(true).when(cmd).isVmTemlateWithSameNameExist("templateName", spId);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
    }

    @Test
    // When Instance by same name exists - fail (regardless of datacenter).
    public void testCanDoActionInstanceNameDuplicate() {
        cmd.getParameters().setTemplateType(VmEntityType.INSTANCE_TYPE);
        doReturn(true).when(cmd).isInstanceWithSameNameExists("templateName");
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
    }

    @Test
    public void sufficientStorageSpace() {
        setupForStorageTests();
        assertTrue(cmd.imagesRelatedChecks());
    }

    @Test
    public void storageSpaceNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(cmd.imagesRelatedChecks());
    }

    @Test
    public void insufficientStorageSpace() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        assertFalse(cmd.imagesRelatedChecks());
    }


    @Test
    public void testBeanValidations() {
        assertTrue(cmd.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        cmd.getParameters().setName("aa-??bb");
        assertFalse("Pattern-based name should not be supported for Template", cmd.validateInputs());
    }


    @Test
    public void testOneEmptyDiskAlias() {
        Map<Guid, DiskImage> diskInfoDestinationMap = new HashMap<>();
        DiskImage disk1 = new DiskImage();
        disk1.setDiskAlias("");

        diskInfoDestinationMap.put(Guid.newGuid(), disk1);
        cmd.diskInfoDestinationMap = diskInfoDestinationMap;
        assertFalse(cmd.isDisksAliasNotEmpty());
    }

    @Test
    public void testOneOfManyEmptyDiskAlias() {
        Map<Guid, DiskImage> diskInfoDestinationMap = new HashMap<>();
        DiskImage disk1 = new DiskImage();
        DiskImage disk2 = new DiskImage();

        disk1.setDiskAlias("");
        disk2.setDiskAlias("disk");

        diskInfoDestinationMap.put(Guid.newGuid(), disk1);
        diskInfoDestinationMap.put(Guid.newGuid(), disk2);
        cmd.diskInfoDestinationMap = diskInfoDestinationMap;
        assertFalse(cmd.isDisksAliasNotEmpty());
    }


    @Test
    public void testDiskAliasNotEmpty() {
        Map<Guid, DiskImage> diskInfoDestinationMap = new HashMap<>();

        DiskImage disk1 = new DiskImage();
        DiskImage disk2 = new DiskImage();

        disk1.setDiskAlias("disk");
        disk2.setDiskAlias("disk");

        diskInfoDestinationMap.put(Guid.newGuid(), disk1);
        diskInfoDestinationMap.put(Guid.newGuid(), disk2);
        cmd.diskInfoDestinationMap = diskInfoDestinationMap;

        assertTrue(cmd.isDisksAliasNotEmpty());
    }


    private void setupForStorageTests() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Down);
        doReturn(multipleSdValidator).when(cmd).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotIllegal();

        setupStoragePool();
    }

    private void setupStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(spId);
        storagePool.setStatus(StoragePoolStatus.Up);
        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        when(storagePoolDao.get(spId)).thenReturn(storagePool);
    }

    private List<DiskImage> getDisksList(Guid spId) {
        List disksList = new ArrayList(1);
        DiskImage disk = new DiskImage();
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(spId)));
        disksList.add(disk);
        return disksList;
    }
}
