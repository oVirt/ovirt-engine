package org.ovirt.engine.core.bll.exportimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.bll.validator.VmNicMacsUtils;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.VmInitToOpenStackMetadataAdapter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class ImportVmCommandTest extends BaseCommandTest {
    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    private OsRepository osRepository;

    @Mock
    private MacPool macPool;

    @Mock
    private MacPoolPerCluster poolPerCluster;

    @Mock
    private VmNicMacsUtils vmNicMacsUtils;

    @Mock
    private VmHandler vmHandler;

    @Mock
    private ImportUtils importUtils;

    @Spy
    @InjectMocks
    private ImportVmCommand<ImportVmParameters> cmd = new ImportVmCommand<>(createParameters(), null);

    @Mock
    private VmInitToOpenStackMetadataAdapter openStackMetadataAdapter;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Before
    public void setUpOsRepository() {
        final int osId = 0;

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    @Before
    public void setUp() {
        doReturn(null).when(cmd).getCluster();
    }

    @Test
    public void insufficientDiskSpaceWithCollapse() {
        setupDiskSpaceTest();
        doReturn(true).when(cmd).validateImages(any());
        when(cmd.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        mockMemorySizeConfigValues(Version.getLast());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void insufficientDiskSpaceWithSnapshots() {
        setupDiskSpaceTest();
        doReturn(true).when(cmd).validateImages(any());
        when(cmd.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        mockMemorySizeConfigValues(Version.getLast());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    void addBalloonToVm(VM vm) {
        Guid deviceId = Guid.newGuid();
        Map<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, VdsProperties.Virtio);
        VmDevice balloon = new VmDevice(new VmDeviceId(deviceId, vm.getId()),
                VmDeviceGeneralType.BALLOON, VmDeviceType.MEMBALLOON.toString(), null, specParams,
                true, true, true, null, null, null, null);

        vm.getManagedVmDeviceMap().put(deviceId, balloon);
    }

    private void addSoundDeviceToVm(VM vm) {
        Guid deviceId = Guid.newGuid();
        Map<String, Object> specParams = new HashMap<>();
        VmDevice sound = new VmDevice(new VmDeviceId(deviceId, vm.getId()),
                VmDeviceGeneralType.SOUND, "", null, specParams,
                true, true, true, null, null, null, null);

        vm.getManagedVmDeviceMap().put(deviceId, sound);
    }

    private void setupCanImportPpcTest() {
        setupDiskSpaceTest();

        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        Cluster cluster = new Cluster();
        cluster.setStoragePoolId(cmd.getParameters().getStoragePoolId());
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(cmd).getCluster();
        doReturn(true).when(cmd).validateImages(any());
    }

    @Test
    public void refuseBalloonOnPPC() {
        setupCanImportPpcTest();

        addBalloonToVm(cmd.getVmFromExportDomain(null));
        when(osRepository.isBalloonEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);
        mockMemorySizeConfigValues(cmd.getCluster().getCompatibilityVersion());

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH.toString()));
    }

    @Test
    public void refuseSoundDeviceOnPPC() {
        setupCanImportPpcTest();

        addSoundDeviceToVm(cmd.getVmFromExportDomain(null));
        when(osRepository.isSoundDeviceEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);
        mockMemorySizeConfigValues(cmd.getCluster().getCompatibilityVersion());

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH.toString()));
    }

    @Test
    public void acceptBalloon() {
        setupDiskSpaceTest();

        addBalloonToVm(cmd.getParameters().getVm());

        cmd.getParameters().getVm().setClusterArch(ArchitectureType.x86_64);
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setStoragePoolId(cmd.getParameters().getStoragePoolId());
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(cmd).getCluster();
        cmd.setClusterId(cluster.getId());
        cmd.getParameters().setClusterId(cluster.getId());
        osRepository.getGraphicsAndDisplays().get(0).put(Version.getLast(),
                Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.isBalloonEnabled(cmd.getParameters().getVm().getVmOsId(), cluster.getCompatibilityVersion())).thenReturn(true);
        cmd.initEffectiveCompatibilityVersion();
        assertTrue(cmd.validateBallonDevice());
    }

    @Test
    public void lowThresholdStorageSpace() {
        setupDiskSpaceTest();
        doReturn(true).when(cmd).validateImages(any());
        when(cmd.getImportValidator().validateSpaceRequirements(anyCollection()))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        mockMemorySizeConfigValues(Version.getLast());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private void setupDiskSpaceTest() {
        final ImportValidator validator = spy(new ImportValidator(cmd.getParameters()));
        doReturn(validator).when(cmd).getImportValidator();
        cmd.init();
        cmd.getParameters().setCopyCollapse(true);
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUniqueVmName();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).setAndValidateCpuProfile();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any());
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any());
        doReturn(cmd.getParameters().getVm()).when(cmd).getVmFromExportDomain(any());
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        Cluster cluster = new Cluster();
        cluster.setStoragePoolId(cmd.getParameters().getStoragePoolId());
        cluster.setClusterId(cmd.getParameters().getClusterId());
        doReturn(cluster).when(cmd).getCluster();
        doReturn(macPool).when(cmd).getMacPool();

        when(poolPerCluster.getMacPoolForCluster(any())).thenReturn(macPool);

        ArrayList<Guid> sdIds = new ArrayList<>(Collections.singletonList(Guid.newGuid()));
        for (DiskImage image : cmd.getParameters().getVm().getImages()) {
            image.setStorageIds(sdIds);
        }

        doReturn(Collections.emptyList()).when(cmd).createDiskDummiesForSpaceValidations(any());
    }

    protected ImportVmParameters createParameters() {
        final VM vm = createVmWithSnapshots();
        vm.setName("testVm");
        Guid clusterId = Guid.newGuid();
        vm.setClusterId(clusterId);
        Guid spId = Guid.newGuid();
        return new ImportVmParameters(vm, Guid.newGuid(), Guid.newGuid(), spId, clusterId);
    }

    protected VM createVmWithSnapshots() {
        final VM v = new VM();
        v.setId(Guid.newGuid());

        Snapshot baseSnapshot = new Snapshot();
        baseSnapshot.setVmId(v.getId());

        Snapshot activeSnapshot = new Snapshot();
        activeSnapshot.setVmId(v.getId());

        DiskImage baseImage = createDiskImage(Guid.newGuid(), Guid.newGuid(), baseSnapshot.getId(), false);
        DiskImage activeImage =
                createDiskImage(baseImage.getId(), baseImage.getImageId(), activeSnapshot.getId(), true);

        baseSnapshot.setDiskImages(Collections.singletonList(baseImage));
        activeSnapshot.setDiskImages(Collections.singletonList(activeImage));

        v.setDiskMap(Collections.singletonMap(activeImage.getId(), activeImage));
        v.setImages(new ArrayList<>(Arrays.asList(baseImage, activeImage)));
        v.setSnapshots(new ArrayList<>(Arrays.asList(baseSnapshot, activeSnapshot)));
        v.setClusterId(Guid.Empty);

        return v;
    }

    protected VM createVmWithNoSnapshots() {
        final VM v = new VM();
        v.setId(Guid.newGuid());

        Snapshot activeSnapshot = new Snapshot();
        activeSnapshot.setVmId(v.getId());
        DiskImage activeImage = createDiskImage(Guid.newGuid(), Guid.newGuid(), activeSnapshot.getId(), true);
        activeSnapshot.setDiskImages(Collections.singletonList(activeImage));

        v.setImages(new ArrayList<>(Collections.singletonList(activeImage)));
        v.setSnapshots(new ArrayList<>(Collections.singletonList(activeSnapshot)));
        v.setDiskMap(Collections.singletonMap(activeImage.getId(), activeImage));
        v.setClusterId(Guid.Empty);

        return v;
    }

    private DiskImage createDiskImage(Guid imageGroupId, Guid parentImageId, Guid vmSnapshoId, boolean active) {
        DiskImage disk = new DiskImage();
        disk.setId(imageGroupId);
        disk.setImageId(Guid.newGuid());
        disk.setSizeInGigabytes(1);
        disk.setVmSnapshotId(vmSnapshoId);
        disk.setActive(active);
        disk.setParentId(parentImageId);

        return disk;
    }

    protected StorageDomain createSourceDomain() {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.ImportExport);
        sd.setStatus(StorageDomainStatus.Active);
        return sd;
    }

    protected StorageDomain createStorageDomain() {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setAvailableDiskSize(2);
        return sd;
    }

    @Test
    public void testValidateNameSizeImportAsCloned() {
        checkVmName(true, RandomUtils.instance().nextPropertyString(300));
    }

    @Test
    public void testDoNotValidateNameSizeImport() {
        checkVmName(false, RandomUtils.instance().nextPropertyString(300));
    }

    @Test
    public void testValidateNameSpecialCharsImportAsCloned() {
        checkVmName(true, "vm_#$@%$#@@");
    }

    @Test
    public void testDoNotValidateNameSpecialCharsImport() {
        checkVmName(false, "vm_#$@%$#@@");
    }

    private void checkVmName(boolean isImportAsNewEntity, String name) {
        cmd.getParameters().getVm().setName(name);
        cmd.getParameters().setImportAsNewEntity(isImportAsNewEntity);
        cmd.init();
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(cmd.getParameters(),
                        cmd.getValidationGroups().toArray(new Class<?>[0]));
        assertEquals(validate.isEmpty(), !isImportAsNewEntity);
    }

    /**
     * Checking that other fields in {@link org.ovirt.engine.core.common.businessentities.VmStatic#VmStatic} don't get
     * validated when importing a VM.
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        String tooLongString =
                RandomUtils.instance().nextPropertyString(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE + 1);
        cmd.getParameters().getVm().setUserDefinedProperties(tooLongString);
        cmd.getParameters().setImportAsNewEntity(true);
        cmd.init();
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(cmd.getParameters(),
                        cmd.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
        cmd.getParameters().getVm().setUserDefinedProperties(tooLongString);
        cmd.getParameters().setImportAsNewEntity(false);
        cmd.init();
        validate = ValidationUtils.getValidator()
                .validate(cmd.getParameters(), cmd.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }

    /**
     * Checking that managed device are sync with the new Guids of disk
     */
    @Test
    public void testManagedDeviceSyncWithNewDiskId() {
        cmd.init();
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setStorageIds(new ArrayList<>());
        DiskImage diskImage2 = new DiskImage();
        diskImage2.setStorageIds(new ArrayList<>());
        diskList.add(diskImage);
        diskList.add(diskImage2);
        DiskImage disk = cmd.getActiveVolumeDisk(diskList);
        Map<Guid, VmDevice> managedDevices = new HashMap<>();
        managedDevices.put(disk.getId(), new VmDevice());
        Guid beforeOldDiskId = disk.getId();
        cmd.generateNewDiskId(diskList, disk);
        cmd.updateManagedDeviceMap(disk, managedDevices);
        Guid oldDiskId = cmd.newDiskIdForDisk.get(disk.getId()).getId();
        assertEquals("The old disk id should be similar to the value at the newDiskIdForDisk.",
                beforeOldDiskId,
                oldDiskId);
        assertNotNull("The manged device should return the disk device by the new key",
                managedDevices.get(disk.getId()));
        assertNull("The manged device should not return the disk device by the old key",
                managedDevices.get(beforeOldDiskId));
    }

    /* Tests for alias generation in addVmImagesAndSnapshots() */

    @Test
    public void testAliasGenerationByAddVmImagesAndSnapshotsWithCollapse() {
        cmd.getParameters().setCopyCollapse(true);
        cmd.init();

        DiskImage collapsedDisk = cmd.getParameters().getVm().getImages().get(1);

        doNothing().when(cmd).saveImage(collapsedDisk);
        doNothing().when(cmd).saveBaseDisk(collapsedDisk);
        doNothing().when(cmd).saveDiskImageDynamic(collapsedDisk);
        doNothing().when(cmd).saveDiskVmElement(any(), any(), any());
        doReturn(new Snapshot()).when(cmd).addActiveSnapshot(any());
        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", collapsedDisk.getDiskAlias());
    }

    /* Test import images with empty Guid is failing */

    @Test
    public void testEmptyGuidFails() {
        cmd.getParameters().setCopyCollapse(Boolean.TRUE);
        DiskImage diskImage = cmd.getParameters().getVm().getImages().get(0);
        diskImage.setVmSnapshotId(Guid.Empty);
        doReturn(macPool).when(cmd).getMacPool();
        cmd.init();
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUniqueVmName();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any());
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any());
        doReturn(cmd.getParameters().getVm()).when(cmd).getVmFromExportDomain(any());
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        Cluster cluster = new Cluster();
        cluster.setStoragePoolId(cmd.getParameters().getStoragePoolId());
        cluster.setId(cmd.getParameters().getClusterId());
        doReturn(cluster).when(cmd).getCluster();

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID.toString()));
    }

    @Test
    public void testAliasGenerationByAddVmImagesAndSnapshotsWithoutCollapse() {
        cmd.getParameters().setCopyCollapse(false);
        cmd.init();

        for (DiskImage image : cmd.getParameters().getVm().getImages()) {
            doNothing().when(cmd).saveImage(image);
            doNothing().when(cmd).saveSnapshotIfNotExists(any(), eq(image));
            doNothing().when(cmd).saveDiskImageDynamic(image);
        }
        DiskImage activeDisk = cmd.getParameters().getVm().getImages().get(1);

        doNothing().when(cmd).updateImage(activeDisk);
        doNothing().when(cmd).saveBaseDisk(activeDisk);
        doNothing().when(cmd).updateActiveSnapshot(any());
        doNothing().when(cmd).saveDiskVmElement(any(), any(), any());

        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", activeDisk.getDiskAlias());
    }

    @Test
    public void testCDANoCollapseNoSnapshots() {
        final VM v = createVmWithNoSnapshots();
        v.setName("testVm");
        cmd.getParameters().setVm(v);
        cmd.getParameters().setCopyCollapse(false);
        cmd.init();

        DiskImage activeDisk = cmd.getParameters().getVm().getImages().get(0);

        doNothing().when(cmd).saveImage(activeDisk);
        doNothing().when(cmd).saveDiskImageDynamic(activeDisk);
        doNothing().when(cmd).saveBaseDisk(activeDisk);
        doNothing().when(cmd).saveDiskVmElement(any(), any(), any());
        doReturn(new Snapshot()).when(cmd).addActiveSnapshot(any());

        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", activeDisk.getDiskAlias());
    }

    private void mockMemorySizeConfigValues(Version version) {
        mcr.mockConfigValue(ConfigValues.VM32BitMaxMemorySizeInMB, version, 20480);
        mcr.mockConfigValue(ConfigValues.VM64BitMaxMemorySizeInMB, version, 4194304);
        mcr.mockConfigValue(ConfigValues.VMPpc64BitMaxMemorySizeInMB, version, 1048576);
    }
}
