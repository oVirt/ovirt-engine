package org.ovirt.engine.core.bll.exportimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
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
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.bll.validator.ImportValidator;
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
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class ImportVmCommandTest extends BaseCommandTest {
    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    OsRepository osRepository;

    @Mock
    private MacPool macPool;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private MacPoolPerDc macPoolPerDc;

    @Mock
    private ExternalVmMacsFinder externalVmMacsFinder;

    @Before
    public void setUp() {
        // init the injector with the osRepository instance
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        injectorRule.bind(MacPoolPerDc.class, macPoolPerDc);

        final int osId = 0;

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<>());
        displayTypeMap.get(osId).put(null, Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    @Test
    public void insufficientDiskSpaceWithCollapse() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        doReturn(true).when(command).validateImages(anyMap());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void insufficientDiskSpaceWithSnapshots() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        doReturn(true).when(command).validateImages(anyMap());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    void addBalloonToVm(VM vm) {
        Guid deviceId = Guid.newGuid();
        Map<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, VdsProperties.Virtio);
        VmDevice balloon = new VmDevice(new VmDeviceId(deviceId, vm.getId()),
                VmDeviceGeneralType.BALLOON, VmDeviceType.MEMBALLOON.toString(), null, 0, specParams,
                true, true, true, null, null, null, null);

        vm.getManagedVmDeviceMap().put(deviceId, balloon);
    }

    private void addSoundDeviceToVm(VM vm) {
        Guid deviceId = Guid.newGuid();
        Map<String, Object> specParams = new HashMap<>();
        VmDevice sound = new VmDevice(new VmDeviceId(deviceId, vm.getId()),
                VmDeviceGeneralType.SOUND, "", null, 0, specParams,
                true, true, true, null, null, null, null);

        vm.getManagedVmDeviceMap().put(deviceId, sound);
    }

    private ImportVmCommand<ImportVmParameters> setupCanImportPpcTest() {
        final ImportVmCommand<ImportVmParameters> cmd = setupDiskSpaceTest(createParameters());

        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        Cluster cluster = new Cluster();
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(cmd).getCluster();
        doReturn(true).when(cmd).validateImages(anyMap());

        return cmd;
    }

    @Test
    public void refuseBalloonOnPPC() {
        final ImportVmCommand<ImportVmParameters> cmd = setupCanImportPpcTest();

        addBalloonToVm(cmd.getVmFromExportDomain(null));
        when(osRepository.isBalloonEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH.toString()));
    }

    @Test
    public void refuseSoundDeviceOnPPC() {
        final ImportVmCommand<ImportVmParameters> cmd = setupCanImportPpcTest();

        addSoundDeviceToVm(cmd.getVmFromExportDomain(null));
        when(osRepository.isSoundDeviceEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH.toString()));
    }

    @Test
    public void acceptBalloon() {
        final ImportVmCommand<ImportVmParameters> c = setupDiskSpaceTest(createParameters());

        addBalloonToVm(c.getParameters().getVm());

        c.getParameters().getVm().setClusterArch(ArchitectureType.x86_64);
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(c).getCluster();
        c.setClusterId(cluster.getId());
        c.getParameters().setClusterId(cluster.getId());
        osRepository.getGraphicsAndDisplays().get(0).put(Version.getLast(),
                Collections.singletonList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.isBalloonEnabled(c.getParameters().getVm().getVmOsId(), cluster.getCompatibilityVersion())).thenReturn(true);
        c.initEffectiveCompatibilityVersion();
        assertTrue(c.validateBallonDevice());
    }

    @Test
    public void lowThresholdStorageSpace() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        doReturn(true).when(command).validateImages(anyMap());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private ImportVmCommand<ImportVmParameters> setupDiskSpaceTest(ImportVmParameters parameters) {
        final ImportValidator validator = spy(new ImportValidator(parameters));
        ImportVmCommand<ImportVmParameters> cmd =
                spy(new ImportVmCommandStub(parameters, validator, macPoolPerDc, externalVmMacsFinder));
        cmd.init();
        parameters.setCopyCollapse(true);
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUsbPolicy();
        doReturn(true).when(cmd).validateUniqueVmName();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).setAndValidateCpuProfile();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any(Iterable.class));
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any(Guid.class));
        doReturn(parameters.getVm()).when(cmd).getVmFromExportDomain(any(Guid.class));
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        doReturn(clusterDao).when(cmd).getClusterDao();
        Cluster cluster = new Cluster();
        cluster.setClusterId(parameters.getClusterId());
        doReturn(cluster).when(cmd).getCluster();
        doReturn(macPool).when(cmd).getMacPool();

        when(macPoolPerDc.getMacPoolForDataCenter(any(Guid.class))).thenReturn(macPool);

        ArrayList<Guid> sdIds = new ArrayList<>(Collections.singletonList(Guid.newGuid()));
        for (DiskImage image : parameters.getVm().getImages()) {
            image.setStorageIds(sdIds);
        }

        doReturn(Collections.<DiskImage> emptyList()).when(cmd).createDiskDummiesForSpaceValidations(anyList());

        return cmd;
    }

    protected ImportVmParameters createParameters() {
        final VM vm = createVmWithSnapshots();
        vm.setName("testVm");
        Guid clusterId = Guid.newGuid();
        vm.setClusterId(clusterId);
        return new ImportVmParameters(vm, Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), clusterId);
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

        v.setDiskMap(Collections.<Guid, Disk>singletonMap(activeImage.getId(), activeImage));
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
        v.setDiskMap(Collections.<Guid, Disk> singletonMap(activeImage.getId(), activeImage));
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
        ImportVmParameters parameters = createParameters();
        parameters.getVm().setName(name);
        parameters.setImportAsNewEntity(isImportAsNewEntity);
        ImportVmCommand<ImportVmParameters> command =
                new ImportVmCommandStub(parameters, macPoolPerDc, externalVmMacsFinder);
        command.init();
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertEquals(validate.isEmpty(), !isImportAsNewEntity);
    }

    /**
     * Checking that other fields in {@link org.ovirt.engine.core.common.businessentities.VmStatic#VmStatic} don't get
     * validated when importing a VM.
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        ImportVmParameters parameters = createParameters();
        String tooLongString =
                RandomUtils.instance().nextPropertyString(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE + 1);
        parameters.getVm().setUserDefinedProperties(tooLongString);
        parameters.setImportAsNewEntity(true);
        ImportVmCommand<ImportVmParameters> command =
                new ImportVmCommandStub(parameters, macPoolPerDc, externalVmMacsFinder);
        command.init();
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
        parameters.getVm().setUserDefinedProperties(tooLongString);
        parameters.setImportAsNewEntity(false);
        command = new ImportVmCommandStub(parameters, macPoolPerDc, externalVmMacsFinder);
        command.init();
        validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }

    /**
     * Checking that managed device are sync with the new Guids of disk
     */
    @Test
    public void testManagedDeviceSyncWithNewDiskId() {
        ImportVmParameters parameters = createParameters();
        ImportVmCommand<ImportVmParameters> command =
                new ImportVmCommandStub(parameters, macPoolPerDc, externalVmMacsFinder);
        command.init();
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setStorageIds(new ArrayList<>());
        DiskImage diskImage2 = new DiskImage();
        diskImage2.setStorageIds(new ArrayList<>());
        diskList.add(diskImage);
        diskList.add(diskImage2);
        DiskImage disk = command.getActiveVolumeDisk(diskList);
        Map<Guid, VmDevice> managedDevices = new HashMap<>();
        managedDevices.put(disk.getId(), new VmDevice());
        Guid beforeOldDiskId = disk.getId();
        command.generateNewDiskId(diskList, disk);
        command.updateManagedDeviceMap(disk, managedDevices);
        Guid oldDiskId = command.newDiskIdForDisk.get(disk.getId()).getId();
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
        ImportVmParameters params = createParameters();
        params.setCopyCollapse(true);
        ImportVmCommand<ImportVmParameters> cmd =
                spy(new ImportVmCommandStub(params, macPoolPerDc, externalVmMacsFinder));
        cmd.init();

        DiskImage collapsedDisk = params.getVm().getImages().get(1);

        doNothing().when(cmd).saveImage(collapsedDisk);
        doNothing().when(cmd).saveBaseDisk(collapsedDisk);
        doNothing().when(cmd).saveDiskImageDynamic(collapsedDisk);
        doNothing().when(cmd).saveDiskVmElement(any(Guid.class), any(Guid.class), any(DiskVmElement.class));
        doReturn(new Snapshot()).when(cmd).addActiveSnapshot(any(Guid.class));
        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", collapsedDisk.getDiskAlias());
    }

    /* Test import images with empty Guid is failing */

    @Test
    public void testEmptyGuidFails() {
        ImportVmParameters params = createParameters();
        params.setCopyCollapse(Boolean.TRUE);
        DiskImage diskImage = params.getVm().getImages().get(0);
        diskImage.setVmSnapshotId(Guid.Empty);
        ImportVmCommand<ImportVmParameters> cmd =
                spy(new ImportVmCommandStub(params, macPoolPerDc, externalVmMacsFinder));
        doReturn(macPool).when(cmd).getMacPool();
        cmd.init();
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUsbPolicy();
        doReturn(true).when(cmd).validateUniqueVmName();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any(Iterable.class));
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any(Guid.class));
        doReturn(params.getVm()).when(cmd).getVmFromExportDomain(any(Guid.class));
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        Cluster cluster = new Cluster();
        cluster.setId(params.getClusterId());
        doReturn(cluster).when(cmd).getCluster();

        assertFalse(cmd.validate());
        assertTrue(cmd.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID.toString()));
    }

    @Test
    public void testAliasGenerationByAddVmImagesAndSnapshotsWithoutCollapse() {
        ImportVmParameters params = createParameters();
        params.setCopyCollapse(false);
        ImportVmCommand<ImportVmParameters> cmd =
                spy(new ImportVmCommandTest.ImportVmCommandStub(params, macPoolPerDc, externalVmMacsFinder));
        cmd.init();

        for (DiskImage image : params.getVm().getImages()) {
            doNothing().when(cmd).saveImage(image);
            doNothing().when(cmd).saveSnapshotIfNotExists(any(Guid.class), eq(image));
            doNothing().when(cmd).saveDiskImageDynamic(image);
        }
        DiskImage activeDisk = params.getVm().getImages().get(1);

        doNothing().when(cmd).updateImage(activeDisk);
        doNothing().when(cmd).saveBaseDisk(activeDisk);
        doNothing().when(cmd).updateActiveSnapshot(any(Guid.class));
        doNothing().when(cmd).saveDiskVmElement(any(Guid.class), any(Guid.class), any(DiskVmElement.class));

        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", activeDisk.getDiskAlias());
    }

    @Test
    public void testCDANoCollapseNoSnapshots() {
        final VM v = createVmWithNoSnapshots();
        v.setName("testVm");
        ImportVmParameters params =
                new ImportVmParameters(v, Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid());

        params.setCopyCollapse(false);
        ImportVmCommand<ImportVmParameters> cmd =
                spy(new ImportVmCommandStub(params, macPoolPerDc, externalVmMacsFinder));
        cmd.init();

        DiskImage activeDisk = params.getVm().getImages().get(0);

        doNothing().when(cmd).saveImage(activeDisk);
        doNothing().when(cmd).saveDiskImageDynamic(activeDisk);
        doNothing().when(cmd).saveBaseDisk(activeDisk);
        doNothing().when(cmd).saveDiskVmElement(any(Guid.class), any(Guid.class), any(DiskVmElement.class));
        doReturn(new Snapshot()).when(cmd).addActiveSnapshot(any(Guid.class));

        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", activeDisk.getDiskAlias());
    }

    private static class ImportVmCommandStub extends ImportVmCommand<ImportVmParameters> {

        private final ImportValidator validator;

        public ImportVmCommandStub(ImportVmParameters parameters,
                MacPoolPerDc macPoolPerDc,
                ExternalVmMacsFinder externalVmMacsFinder) {
            this(parameters, null, macPoolPerDc, externalVmMacsFinder);
        }

        public ImportVmCommandStub(ImportVmParameters parameters,
                ImportValidator validator,
                MacPoolPerDc MacPoolPerDc,
                ExternalVmMacsFinder externalVmMacsFinder) {
            this(parameters, CommandContext.createContext(parameters.getSessionId()),
                    validator,
                    MacPoolPerDc,
                    externalVmMacsFinder);
        }

        public ImportVmCommandStub(ImportVmParameters parameters,
                CommandContext commandContext,
                ImportValidator validator,
                MacPoolPerDc macPoolPerDc,
                ExternalVmMacsFinder externalVmMacsFinder) {
            super(parameters, commandContext);
            this.validator = validator;
            poolPerDc = macPoolPerDc;
            this.externalVmMacsFinder = externalVmMacsFinder;
        }

        @Override
        protected void initUser() {
        }

        @Override
        public Cluster getCluster() {
            return null;
        }

        @Override
        protected ImportValidator getImportValidator() {
            return validator != null ? validator : super.getImportValidator();
        }
    }
}
