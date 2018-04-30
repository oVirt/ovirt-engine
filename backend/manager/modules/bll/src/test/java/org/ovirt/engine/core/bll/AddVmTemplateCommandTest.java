package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test case for {@link AddVmTemplateCommand}
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmTemplateCommandTest extends BaseCommandTest {
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
    private MultipleStorageDomainsValidator multipleSdValidator;
    @Mock
    private DiskHandler diskHandler;
    @Mock
    private ImagesHandler imagesHandler;
    @Mock
    private VmHandler vmHandler;
    @Mock
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

    @BeforeEach
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

        doNothing().when(cmd).separateCustomProperties(any());
        doReturn(getDisksList(vm.getStoragePoolId())).when(cmd).getVmDisksFromDB();

        cmd.init();
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
        assertTrue(cmd.validateImages());
    }

    @Test
    public void storageSpaceNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(cmd.validateImages());
    }

    @Test
    public void insufficientStorageSpace() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(any());
        assertFalse(cmd.validateImages());
    }

    @Test
    public void imagesRelatedChecksFailPassDiscardNotSupported() {
        setupForStorageTests();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE))
                .when(cmd).isPassDiscardSupportedForImagesDestSds();
        assertFalse(cmd.validateImages());
    }

    @Test
    public void passDiscardSupportedForDestSds() {
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
        assertFalse(cmd.validateInputs(), "Pattern-based name should not be supported for Template");
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
            assertNotEquals(ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES, permissionSubject.getActionGroup());
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
        doReturn(multipleSdValidator).when(cmd).getStorageDomainsValidator(any(), any());

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
        MultipleDiskVmElementValidator multipleDiskVmElementValidator = mock(MultipleDiskVmElementValidator.class);
        doReturn(multipleDiskVmElementValidator).when(cmd).createMultipleDiskVmElementValidator(any());
        when(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(any())).thenReturn(validationResult);
    }
}
