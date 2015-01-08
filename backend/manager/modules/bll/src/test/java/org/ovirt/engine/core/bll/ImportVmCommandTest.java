package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class ImportVmCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VirtIoScsiEnabled, Version.v3_2.toString(), false));

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    OsRepository osRepository;

    @Mock
    private MacPoolManagerStrategy macPoolManagerStrategy;

    @Before
    public void setUp() {
        // init the injector with the osRepository instance
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        final int osId = 0;

        Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> displayTypeMap = new HashMap<>();
        displayTypeMap.put(osId, new HashMap<Version, List<Pair<GraphicsType, DisplayType>>>());
        displayTypeMap.get(osId).put(null, Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.getGraphicsAndDisplays()).thenReturn(displayTypeMap);
    }

    @Test
    public void insufficientDiskSpaceWithCollapse() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void insufficientDiskSpaceWithSnapshots() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    void addBalloonToVm(VM vm) {
        Guid deviceId = Guid.newGuid();
        Map<String, Object> specParams = new HashMap<String, Object>();
        specParams.put(VdsProperties.Model, VdsProperties.Virtio);
        VmDevice balloon = new VmDevice(new VmDeviceId(deviceId, vm.getId()),
                VmDeviceGeneralType.BALLOON, VmDeviceType.MEMBALLOON.toString(), null, 0, specParams,
                true, true, true, null, null, null, null);

        vm.getManagedVmDeviceMap().put(deviceId, balloon);
    }

    @Test
    public void refuseBalloonOnPPC() {
        final ImportVmCommand<ImportVmParameters> c = setupDiskSpaceTest(createParameters());

        addBalloonToVm(c.getParameters().getVm());

        c.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        VDSGroup cluster = new VDSGroup();
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(c).getVdsGroup();
        when(osRepository.isBalloonEnabled(c.getParameters().getVm().getVmOsId(), cluster.getCompatibilityVersion())).thenReturn(false);
        assertFalse(c.canDoAction());
        assertTrue(c.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH.toString()));
    }

    @Test
    public void acceptBalloon() {
        final ImportVmCommand<ImportVmParameters> c = setupDiskSpaceTest(createParameters());

        addBalloonToVm(c.getParameters().getVm());

        c.getParameters().getVm().setClusterArch(ArchitectureType.x86_64);
        VDSGroup cluster = new VDSGroup();
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        doReturn(cluster).when(c).getVdsGroup();
        osRepository.getGraphicsAndDisplays().get(0).put(Version.getLast(),
                Arrays.asList(new Pair<>(GraphicsType.SPICE, DisplayType.qxl)));
        when(osRepository.isBalloonEnabled(c.getParameters().getVm().getVmOsId(), cluster.getCompatibilityVersion())).thenReturn(true);
        assertTrue(c.validateBallonDevice());
    }

    @Test
    public void lowThresholdStorageSpace() {
        final ImportVmCommand<ImportVmParameters> command = setupDiskSpaceTest(createParameters());
        when(command.getImportValidator().validateSpaceRequirements(anyCollection())).thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private ImportVmCommand<ImportVmParameters> setupDiskSpaceTest(ImportVmParameters parameters) {
        final ImportValidator validator = spy(new ImportValidator(parameters));
        ImportVmCommand<ImportVmParameters> cmd = spy(new ImportVmCommand<ImportVmParameters>(parameters) {
            @Override
            protected ImportValidator getImportValidator() {
                return validator;
            }

            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        });
        parameters.setCopyCollapse(true);
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUsbPolicy();
        doReturn(true).when(cmd).canAddVm();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).setAndValidateCpuProfile();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any(Iterable.class));
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any(Guid.class));
        doReturn(Collections.<VM> singletonList(createVM())).when(cmd).getVmsFromExportDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        doReturn(new VDSGroup()).when(cmd).getVdsGroup();
        doReturn(macPoolManagerStrategy).when(cmd).getMacPool();

        ArrayList<Guid> sdIds = new ArrayList<Guid>(Collections.singletonList(Guid.newGuid()));
        for (DiskImage image : parameters.getVm().getImages()) {
            image.setStorageIds(sdIds);
        }

        doReturn(Collections.<DiskImage>emptyList()).when(cmd).createDiskDummiesForSpaceValidations(anyList());

        return cmd;
    }

    protected ImportVmParameters createParameters() {
        final VM v = createVM();
        v.setName("testVm");
        return new ImportVmParameters(v, Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid());
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setId(Guid.newGuid());

        Guid imageGroupId = Guid.newGuid();
        DiskImage baseImage = new DiskImage();
        baseImage.setId(imageGroupId);
        baseImage.setImageId(Guid.newGuid());
        baseImage.setSizeInGigabytes(1);
        baseImage.setVmSnapshotId(Guid.newGuid());
        baseImage.setActive(false);

        DiskImage activeImage = new DiskImage();
        activeImage.setId(imageGroupId);
        activeImage.setImageId(Guid.newGuid());
        activeImage.setSizeInGigabytes(1);
        activeImage.setVmSnapshotId(Guid.newGuid());
        activeImage.setActive(true);
        activeImage.setParentId(baseImage.getImageId());

        v.setDiskMap(Collections.<Guid, Disk> singletonMap(activeImage.getId(), activeImage));
        v.setImages(new ArrayList<DiskImage>(Arrays.asList(baseImage, activeImage)));
        v.setVdsGroupId(Guid.Empty);

        return v;
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

    protected List<DiskImage> mockCreateDiskDummiesForSpaceValidations() {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setActive(false);
            diskImage.setId(Guid.newGuid());
            diskImage.setImageId(Guid.newGuid());
            diskImage.setParentId(Guid.newGuid());
            diskImage.setImageStatus(ImageStatus.OK);
            disksList.add(diskImage);
        }
        return disksList;
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
        ImportVmCommand<ImportVmParameters> command = new ImportVmCommand<ImportVmParameters>(parameters) {
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        };
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertEquals(validate.isEmpty(), !isImportAsNewEntity);
    }

    /**
     * Checking that other fields in
     * {@link org.ovirt.engine.core.common.businessentities.VmStatic.VmStatic}
     * don't get validated when importing a VM.
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        ImportVmParameters parameters = createParameters();
        String tooLongString =
                RandomUtils.instance().nextPropertyString(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE + 1);
        parameters.getVm().setUserDefinedProperties(tooLongString);
        parameters.setImportAsNewEntity(true);
        ImportVmCommand<ImportVmParameters> command = new ImportVmCommand<ImportVmParameters>(parameters) {
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        };
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
        parameters.getVm().setUserDefinedProperties(tooLongString);
        parameters.setImportAsNewEntity(false);
        command = new ImportVmCommand<ImportVmParameters>(parameters){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        };
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
        ImportVmCommand<ImportVmParameters> command = new ImportVmCommand<ImportVmParameters>(parameters){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        };
        List<DiskImage> diskList = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setStorageIds(new ArrayList<Guid>());
        DiskImage diskImage2 = new DiskImage();
        diskImage2.setStorageIds(new ArrayList<Guid>());
        diskList.add(diskImage);
        diskList.add(diskImage2);
        DiskImage disk = command.getActiveVolumeDisk(diskList);
        Map<Guid, VmDevice> managedDevices = new HashMap<>();
        managedDevices.put(disk.getId(), new VmDevice());
        Guid beforeOldDiskId = disk.getId();
        command.generateNewDiskId(diskList, disk);
        command.updateManagedDeviceMap(disk, managedDevices);
        Guid oldDiskId = command.newDiskIdForDisk.get(disk.getId()).getId();
        assertEquals("The old disk id should be similar to the value at the newDiskIdForDisk.", beforeOldDiskId, oldDiskId);
        assertNotNull("The manged device should return the disk device by the new key", managedDevices.get(disk.getId()));
        assertNull("The manged device should not return the disk device by the old key", managedDevices.get(beforeOldDiskId));
    }

    /* Tests for alias generation in addVmImagesAndSnapshots() */

    @Test
    public void testAliasGenerationByAddVmImagesAndSnapshotsWithCollapse() {
        ImportVmParameters params = createParameters();
        params.setCopyCollapse(true);
        ImportVmCommand<ImportVmParameters> cmd = spy(new ImportVmCommand<ImportVmParameters>(params){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        });

        DiskImage collapsedDisk = params.getVm().getImages().get(1);

        doNothing().when(cmd).saveImage(collapsedDisk);
        doNothing().when(cmd).saveBaseDisk(collapsedDisk);
        doNothing().when(cmd).saveDiskImageDynamic(collapsedDisk);
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
        ImportVmCommand<ImportVmParameters> cmd = spy(new ImportVmCommand<ImportVmParameters>(params){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        });
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUsbPolicy();
        doReturn(true).when(cmd).canAddVm();
        doReturn(true).when(cmd).checkTemplateInStorageDomain();
        doReturn(true).when(cmd).checkImagesGUIDsLegal();
        doReturn(true).when(cmd).setAndValidateDiskProfiles();
        doReturn(true).when(cmd).validateNoDuplicateDiskImages(any(Iterable.class));
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(createStorageDomain()).when(cmd).getStorageDomain(any(Guid.class));
        doReturn(Collections.<VM> singletonList(params.getVm())).when(cmd).getVmsFromExportDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new StoragePool()).when(cmd).getStoragePool();
        doReturn(new VDSGroup()).when(cmd).getVdsGroup();

        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID.toString()));
    }

    @Test
    public void testAliasGenerationByAddVmImagesAndSnapshotsWithoutCollapse() {
        ImportVmParameters params = createParameters();
        params.setCopyCollapse(false);
        ImportVmCommand<ImportVmParameters> cmd = spy(new ImportVmCommand<ImportVmParameters>(params){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        });

        for (DiskImage image : params.getVm().getImages()) {
            doNothing().when(cmd).saveImage(image);
            doNothing().when(cmd).saveSnapshotIfNotExists(any(Guid.class), eq(image));
            doNothing().when(cmd).saveDiskImageDynamic(image);
        }
        DiskImage activeDisk = params.getVm().getImages().get(1);

        doNothing().when(cmd).updateImage(activeDisk);
        doNothing().when(cmd).saveBaseDisk(activeDisk);
        doNothing().when(cmd).updateActiveSnapshot(any(Guid.class));

        cmd.addVmImagesAndSnapshots();
        assertEquals("Disk alias not generated", "testVm_Disk1", activeDisk.getDiskAlias());
    }

    @Test
    public void testValidateClusterSupportForVirtioScsi() {
        ImportVmCommand<ImportVmParameters> cmd = setupDiskSpaceTest(createParameters());
        cmd.getParameters().getVm().getDiskMap().values().iterator().next().setDiskInterface(DiskInterface.VirtIO_SCSI);
        cmd.getVdsGroup().setCompatibilityVersion(Version.v3_2);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
    }
}
