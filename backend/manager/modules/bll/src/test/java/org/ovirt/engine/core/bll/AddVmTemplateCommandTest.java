package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for {@link AddVmTemplateCommand}
 */
public class AddVmTemplateCommandTest extends BaseCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    private VM vm = createVM();

    @Spy
    @InjectMocks
    private AddVmTemplateCommand<AddVmTemplateParameters> cmd =
            new AddVmTemplateCommand<>(new AddVmTemplateParameters(vm, "templateName", "Template for testing"), null);

    @Mock
    private VmDao vmDao;
    @Mock
    private ClusterDao clusterDao;
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
    @Mock
    private VmDeviceDao deviceDao;
    @Mock
    private DiskVmElementDao diskVmElementDao;
    @Mock
    private DiskHandler diskHandler;

    @Spy
    @InjectMocks
    private VmHandler vmHandler;

    @Spy
    @InjectMocks
    private VmDeviceUtils vmDeviceUtils;

    private VM createVM() {
        Guid vmId = Guid.newGuid();
        Guid clusterId = Guid.newGuid();
        Guid spId = Guid.newGuid();

        VM vm = new VM();
        vm.setId(vmId);
        vm.setClusterId(clusterId);
        vm.setStoragePoolId(spId);
        vm.setVmOs(14);

        return vm;
    }

    @Before
    public void setUp() {
        when(vmDao.get(vm.getId())).thenReturn(vm);

        // The cluster to use
        Cluster cluster = new Cluster();
        cluster.setCpuName("Intel Conroe Family");
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setId(vm.getClusterId());
        cluster.setStoragePoolId(vm.getStoragePoolId());
        cluster.setCompatibilityVersion(Version.getLast());
        when(clusterDao.get(vm.getClusterId())).thenReturn(cluster);

        mockOsRepository();

        doNothing().when(cmd).separateCustomProperties(any(VmStatic.class));
        doReturn(getDisksList(vm.getStoragePoolId())).when(cmd).getVmDisksFromDB();
        doReturn(vmDeviceUtils).when(cmd).getVmDeviceUtils();

        cmd.init();
    }

    protected void mockOsRepository() {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        vmDeviceUtils.init();
        injectorRule.bind(VmDeviceUtils.class, vmDeviceUtils);
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        vmHandler.init();
        when(osRepository.isWindows(0)).thenReturn(true);
        when(osRepository.getMinimumRam(vm.getVmOsId(), Version.getLast())).thenReturn(0);
        when(osRepository.getMaximumRam(vm.getVmOsId(), Version.getLast())).thenReturn(100);
        when(osRepository.getArchitectureFromOS(14)).thenReturn(ArchitectureType.x86_64);
    }

    @Test
    public void testValidate() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
    }

    @Test
    // When Template by the same name already exists in the datacenter - fail.
    public void testValidateDuplicateTemplateName() {
        doReturn(true).when(cmd).isVmTemplateWithSameNameExist("templateName", vm.getStoragePoolId());
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
    }

    @Test
    // When Instance by same name exists - fail (regardless of datacenter).
    public void testValidateInstanceNameDuplicate() {
        cmd.getParameters().setTemplateType(VmEntityType.INSTANCE_TYPE);
        doReturn(true).when(cmd).isInstanceWithSameNameExists("templateName");
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
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
    public void imagesRelatedChecksFailPassDiscardNotSupported() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE))
                .when(cmd).isPassDiscardSupportedForImagesDestSds();
        assertFalse(cmd.imagesRelatedChecks());
    }

    @Test
    public void passDiscardSupportedForDestSds() {
        mockPassDiscardSupportedForDestSds(ValidationResult.VALID);
        assertThat(cmd.isPassDiscardSupportedForImagesDestSds(), ValidationResultMatchers.isValid());
    }

    @Test
    public void passDiscardNotSupportedForDestSds() {
        mockPassDiscardSupportedForDestSds(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE));
        assertThat(cmd.isPassDiscardSupportedForImagesDestSds(), ValidationResultMatchers.failsWith(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE
        ));
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

    @Test
    public void testPermissionsForAddingTemplateDedicatedHostNotChanged(){
        setupDedicatedHostForVmAndTemplate(true);

        List<PermissionSubject> permissionCheckSubjects = cmd.getPermissionCheckSubjects();
        for(PermissionSubject permissionSubject : permissionCheckSubjects){
            assertFalse(ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES.equals(permissionSubject.getActionGroup()));
        }
    }

    @Test
    public void testPermissionsForAddingTemplateDedicatedHostChanged(){
        setupDedicatedHostForVmAndTemplate(false);

        PermissionSubject editDefaultHostPermission = new PermissionSubject(vm.getStoragePoolId(),
                VdcObjectType.StoragePool,
                ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES);
        List<PermissionSubject> permissionCheckSubjects = cmd.getPermissionCheckSubjects();
        permissionCheckSubjects.stream()
                .filter(permissionSubject ->
                        ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES == permissionSubject.getActionGroup())
                .forEach(permissionSubject -> verifyPermissions(editDefaultHostPermission, permissionSubject));
    }

    private void verifyPermissions(PermissionSubject editDefaultHostPermission, PermissionSubject permissionSubject) {
        assertEquals(permissionSubject.getMessage(), editDefaultHostPermission.getMessage());
        assertEquals(permissionSubject.getActionGroup(), editDefaultHostPermission.getActionGroup());
        assertEquals(permissionSubject.getObjectId(), editDefaultHostPermission.getObjectId());
        assertEquals(permissionSubject.getObjectType(), editDefaultHostPermission.getObjectType());
    }

    private void setupDedicatedHostForVmAndTemplate(boolean setDefaultHostForTemplate){
        Guid hostId = Guid.newGuid();
        vm.setDedicatedVmForVdsList(Collections.singletonList(hostId));

        AddVmTemplateParameters parameters = new AddVmTemplateParameters();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setDedicatedVmForVdsList(setDefaultHostForTemplate ? Collections.singletonList(hostId) : Collections.emptyList());
        parameters.setMasterVm(vmStatic);
        parameters.setTemplateType(VmEntityType.TEMPLATE);
        doReturn(parameters).when(cmd).getParameters();
    }

    private void setupForStorageTests() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Down);
        doReturn(multipleSdValidator).when(cmd).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotIllegal();
        doReturn(ValidationResult.VALID).when(cmd).isPassDiscardSupportedForImagesDestSds();

        setupStoragePool();
    }

    private void setupStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(vm.getStoragePoolId());
        storagePool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(vm.getStoragePoolId())).thenReturn(storagePool);
    }

    private List<DiskImage> getDisksList(Guid spId) {
        DiskImage disk = new DiskImage();
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(spId)));
        return Collections.singletonList(disk);
    }

    private void mockPassDiscardSupportedForDestSds(ValidationResult validationResult) {
        cmd.diskInfoDestinationMap = Collections.emptyMap();
        when(diskHandler.getDiskToDiskVmElementMap(any(Guid.class), anyMapOf(Guid.class, DiskImage.class)))
                .thenReturn(Collections.emptyMap());
        MultipleDiskVmElementValidator multipleDiskVmElementValidator = mock(MultipleDiskVmElementValidator.class);
        doReturn(multipleDiskVmElementValidator).when(cmd)
                .createMultipleDiskVmElementValidator(anyMapOf(Disk.class, DiskVmElement.class));
        when(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(anyMapOf(Guid.class, Guid.class)))
                .thenReturn(validationResult);
    }
}
