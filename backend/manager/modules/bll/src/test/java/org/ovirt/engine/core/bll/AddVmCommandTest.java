package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.QuotaDao;

public class AddVmCommandTest extends AddVmCommandTestBase<AddVmCommand<AddVmParameters>> {
    @Mock
    private DiskDao diskDao;

    @Mock
    private QuotaDao quotaDao;

    @Override
    protected AddVmCommand<AddVmParameters> createCommand() {
        initVM();
        return new AddVmCommand<>(new AddVmParameters(vm), null);
    }

    @Override
    public void setUp() {
        super.setUp();

        generateStorageToDisksMap();
        initDestSDs();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
    }

    @Test
    public void canAddVm() {
        List<String> reasons = new ArrayList<>();
        initCommandMethods();
        cmd.init();

        doReturn(true).when(cmd).validateCustomProperties(any(VmStatic.class), anyListOf(String.class));
        doReturn(true).when(cmd).validateSpaceRequirements();
        assertTrue("vm could not be added", cmd.canAddVm(reasons, Collections.singletonList(createStorageDomain())));
    }

    @Test
    public void isVirtioScsiEnabledDefaultedToTrue() {
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.initEffectiveCompatibilityVersion();
        when(osRepository.getDiskInterfaces(any(Integer.class), any(Version.class))).thenReturn(
                new ArrayList<>(Collections.singletonList("VirtIO_SCSI")));
        assertTrue("isVirtioScsiEnabled hasn't been defaulted to true on cluster >= 3.3.", cmd.isVirtioScsiEnabled());
    }

    @Test
    public void validateSpaceAndThreshold() {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForNewDisks(anyListOf(DiskImage.class));
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForNewDisks(anyListOf(DiskImage.class));
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(anyListOf(DiskImage.class));
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForNewDisks(anyListOf(DiskImage.class));
        assertFalse(cmd.validateSpaceRequirements());
        verify(storageDomainValidator).hasSpaceForNewDisks(anyListOf(DiskImage.class));
        verify(storageDomainValidator, never()).hasSpaceForClonedDisks(anyListOf(DiskImage.class));
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
               when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    private void initPpcCluster() {
        initCluster();
        cluster.setCpuName("PPC8");
        cluster.setArchitecture(ArchitectureType.ppc64);
    }

    protected List<DiskImage> createDiskSnapshot(Guid diskId, int numOfImages) {
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

    @Test
    public void testBeanValidations() {
        initializeMock();
        assertTrue(cmd.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        initializeMock();
        cmd.getParameters().getVm().setName("aa-??bb");
        assertFalse("Pattern-based name should not be supported for VM", cmd.validateInputs());
    }

    @Test
    public void refuseBalloonOnPPC() {
        setupCanAddPpcTest();
        cmd.getParameters().setBalloonEnabled(true);
        when(osRepository.isBalloonEnabled(cmd.getParameters().getVm().getVmOsId(), cmd.getCluster().getCompatibilityVersion())).thenReturn(false);
        cmd.init();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
    }

    @Test
    public void refuseSoundDeviceOnPPC() {
        setupCanAddPpcTest();
        cmd.getParameters().setSoundDeviceEnabled(true);
        cmd.init();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
    }

    private void setupCanAddPpcTest() {
        doReturn(true).when(cmd).validateSpaceRequirements();
        doReturn(true).when(cmd).buildAndCheckDestStorageDomains();
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        cluster.setArchitecture(ArchitectureType.ppc64);
        cluster.setCompatibilityVersion(Version.getLast());
    }

    @Test
    public void testStoragePoolDoesntExist() {
        doReturn(null).when(cmd).getStoragePool();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testBlockUseHostCpuWithPPCArch() {
        setupCanAddPpcTest();
        cmd.setEffectiveCompatibilityVersion(Version.v4_0);
        doReturn(Collections.emptyList()).when(cmd).getImagesToCheckDestinationStorageDomains();
        initPpcCluster();
        when(clusterDao.get(any(Guid.class))).thenReturn(cluster);
        doReturn(true).when(cmd).areParametersLegal(Collections.emptyList());
        doReturn(true).when(cmd).validateAddVmCommand();
        doReturn(true).when(cmd).isVmNameValidLength(any(VM.class));
        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.ppc64);
        cmd.getParameters().getVm().setClusterArch(ArchitectureType.ppc64);
        cmd.getParameters().getVm().setUseHostCpuFlags(true);
        cmd.getParameters().getVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        cmd.getParameters().getVm().setClusterId(cluster.getId());
        cmd.getParameters().getVm().setVmOs(OsType.Other.ordinal());
        cmd.init();

        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
    }

    @Test
    public void testValidQuota() {
        Guid quotaId = Guid.newGuid();

        Quota quota = new Quota();
        quota.setId(quotaId);

        when(quotaDao.getById(quotaId)).thenReturn(quota);

        quota.setStoragePoolId(storagePool.getId());

        cmd.getParameters().getVm().setQuotaId(quotaId);

        assertTrue(cmd.validateQuota(quotaId));
        assertTrue(cmd.getReturnValue().getValidationMessages().isEmpty());
    }

    @Test
    public void testNonExistingQuota() {
        Guid quotaId = Guid.newGuid();
        cmd.getParameters().getVm().setQuotaId(quotaId);

        assertFalse(cmd.validateQuota(quotaId));
        ValidateTestUtils.assertValidationMessages("", cmd, EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
    }
}
