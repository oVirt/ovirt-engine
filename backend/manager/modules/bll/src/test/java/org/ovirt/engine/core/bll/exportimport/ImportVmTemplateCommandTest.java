package org.ovirt.engine.core.bll.exportimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.validator.VmNicMacsUtils;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class ImportVmTemplateCommandTest extends BaseCommandTest {

    @Mock
    private VmNicMacsUtils vmNicMacsUtils;

    @Mock
    private Backend backend;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VmTemplateDao vmTemplateDao;

    @Spy
    @InjectMocks
    private ImportVmTemplateCommand command =
            new ImportVmTemplateCommand(createParameters(), CommandContext.createContext(""));

    @Test
    public void insufficientDiskSpace() {
        // The following is enough since the validation is mocked out anyway. Just want to make sure the flow in CDA is correct.
        // Full test for the scenarios is done in the inherited class.
        setupVolumeFormatAndTypeTest(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.NFS);
        doReturn(false).when(command).validateSpaceRequirements(anyList());
        assertFalse(command.validate());
    }

    @Test
    public void validVolumeFormatAndTypeCombinations() throws Exception {
        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.NFS);
        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Sparse, StorageType.NFS);
        assertValidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Sparse, StorageType.NFS);

        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.ISCSI);
        assertValidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Sparse, StorageType.ISCSI);
        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Sparse, StorageType.ISCSI);

        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.FCP);
        assertValidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Sparse, StorageType.FCP);
        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Sparse, StorageType.FCP);

        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.LOCALFS);
        assertValidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Sparse, StorageType.LOCALFS);
        assertValidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Sparse, StorageType.LOCALFS);
    }

    @Test
    public void invalidVolumeFormatAndTypeCombinations() throws Exception {
        assertInvalidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Preallocated, StorageType.NFS);
        assertInvalidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Preallocated, StorageType.ISCSI);
        assertInvalidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Preallocated, StorageType.FCP);
        assertInvalidVolumeInfoCombination(VolumeFormat.COW, VolumeType.Preallocated, StorageType.LOCALFS);

        assertInvalidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Unassigned, StorageType.NFS);
        assertInvalidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Unassigned, StorageType.ISCSI);
        assertInvalidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Unassigned, StorageType.FCP);
        assertInvalidVolumeInfoCombination(VolumeFormat.RAW, VolumeType.Unassigned, StorageType.LOCALFS);

        assertInvalidVolumeInfoCombination(VolumeFormat.Unassigned, VolumeType.Preallocated, StorageType.NFS);
        assertInvalidVolumeInfoCombination(VolumeFormat.Unassigned, VolumeType.Preallocated, StorageType.ISCSI);
        assertInvalidVolumeInfoCombination(VolumeFormat.Unassigned, VolumeType.Preallocated, StorageType.FCP);
        assertInvalidVolumeInfoCombination(VolumeFormat.Unassigned, VolumeType.Preallocated, StorageType.LOCALFS);
    }

    @Test
    public void testValidateUniqueTemplateNameInDC() {
        setupVolumeFormatAndTypeTest(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.NFS);
        doReturn(true).when(command).isVmTemplateWithSameNameExist();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS);

    }

    private void assertValidVolumeInfoCombination(VolumeFormat volumeFormat,
            VolumeType volumeType,
            StorageType storageType) {
        setupVolumeFormatAndTypeTest(volumeFormat, volumeType, storageType);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void assertInvalidVolumeInfoCombination(VolumeFormat volumeFormat,
            VolumeType volumeType,
            StorageType storageType) {
        setupVolumeFormatAndTypeTest(volumeFormat, volumeType, storageType);
        ValidateTestUtils.runAndAssertValidateFailure(
                command, EngineMessage.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
    }

    /**
     * Prepare a command for testing the given volume format and type combination.
     *
     * @param volumeFormat
     *            The volume format of the "imported" image.
     * @param volumeType
     *            The volume type of the "imported" image.
     * @param storageType
     *            The target domain's storage type.
     */
    private void setupVolumeFormatAndTypeTest(
            VolumeFormat volumeFormat, VolumeType volumeType, StorageType storageType) {
        when(vmNicMacsUtils.validateMacAddress(any())).thenReturn(ValidationResult.VALID);

        doReturn(false).when(command).isVmTemplateWithSameNameExist();
        doReturn(true).when(command).isClusterCompatible();
        doReturn(true).when(command).validateNoDuplicateDiskImages(any(Iterable.class));
        mockGetTemplatesFromExportDomainQuery(volumeFormat, volumeType);
        mockStorageDomainStatic(storageType);
        mockStoragePool();
        mockStorageDomains();
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).setAndValidateCpuProfile();
        doReturn(true).when(command).validateSpaceRequirements(anyList());
    }

    private void mockStorageDomains() {
        final ImportVmTemplateParameters parameters = command.getParameters();

        final StorageDomain srcDomain = new StorageDomain();
        srcDomain.setStorageDomainType(StorageDomainType.ImportExport);
        srcDomain.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(parameters.getSourceDomainId(), parameters.getStoragePoolId()))
                .thenReturn(srcDomain);

        final StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageDomainType(StorageDomainType.Data);
        destDomain.setUsedDiskSize(0);
        destDomain.setAvailableDiskSize(1000);
        destDomain.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(parameters.getDestDomainId(), parameters.getStoragePoolId()))
                .thenReturn(destDomain);
    }

    private void mockStoragePool() {
        final StoragePool pool = new StoragePool();
        pool.setId(command.getParameters().getStoragePoolId());
        when(storagePoolDao.get(any(Guid.class))).thenReturn(pool);
    }

    private void mockGetTemplatesFromExportDomainQuery(VolumeFormat volumeFormat, VolumeType volumeType) {
        final VdcQueryReturnValue result = new VdcQueryReturnValue();
        Map<VmTemplate, List<DiskImage>> resultMap = new HashMap<>();

        DiskImage image = new DiskImage();
        image.setActualSizeInBytes(2);
        image.setVolumeFormat(volumeFormat);
        image.setVolumeType(volumeType);

        resultMap.put(new VmTemplate(), Collections.singletonList(image));
        result.setReturnValue(resultMap);
        result.setSucceeded(true);

        when(command.getBackend().runInternalQuery(eq(VdcQueryType.GetTemplatesFromExportDomain),
                any(VdcQueryParametersBase.class), any(EngineContext.class))).thenReturn(result);
    }

    private void mockStorageDomainStatic(StorageType storageType) {
        final StorageDomainStatic domain = new StorageDomainStatic();
        domain.setStorageType(storageType);
        when(storageDomainStaticDao.get(any(Guid.class))).thenReturn(domain);
    }

    protected ImportVmTemplateParameters createParameters() {
        VmTemplate t = new VmTemplate();
        t.setName("testTemplate");
        return new ImportVmTemplateParameters(Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), t);
    }

    private final String string100 = "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321" +
            "0987654321";

    @Test
    public void testValidateNameSizeImportAsCloned() {
        checkTemplateName(true, string100);
    }

    @Test
    public void testDoNotValidateNameSizeImport() {
        checkTemplateName(false, string100);
    }

    @Test
    public void testValidateNameSpecialCharImportAsCloned() {
        checkTemplateName(true, "vm_$%$#%#$");
    }

    @Test
    public void testDoNotValidateNameSpecialCharImport() {
        checkTemplateName(false, "vm_$%$#%#$");
    }

    private void checkTemplateName(boolean isImportAsNewEntity, String name) {
        command.getParameters().getVmTemplate().setName(name);
        command.getParameters().setImportAsNewEntity(isImportAsNewEntity);

        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(command.getParameters(),
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertNotEquals(validate.isEmpty(), isImportAsNewEntity);
    }

    /**
     * Checking that other fields in VmTemplate aren't get validated in Import or
     * import as cloned.
     * testing that 100 char String is set in domain field
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        assertFalse(BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE > string100.length());
        command.getParameters().setImportAsNewEntity(true);
        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(command.getParameters(),
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }

    @Test
    public void testOtherFieldsNotValidatedInImportNotNewEntity() {
        command.getParameters().setImportAsNewEntity(false);
        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(command.getParameters(),
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }

    /**
     * Checking that managed device are sync with the new Guids of disk
     */
    @Test
    public void testManagedDeviceSyncWithNewDiskId() {
        DiskImage disk = new DiskImage();
        disk.setStorageIds(new ArrayList<>());
        Map<Guid, VmDevice> managedDevices = new HashMap<>();
        managedDevices.put(disk.getId(), new VmDevice());
        Guid beforeOldDiskId = disk.getId();
        command.generateNewDiskId(disk);
        command.updateManagedDeviceMap(disk, managedDevices);
        Guid oldDiskId = command.getNewDiskIdForDisk(disk.getId()).getId();
        assertEquals("The old disk id should be similar to the value at the newDiskIdForDisk.", beforeOldDiskId, oldDiskId);
        assertNotNull("The manged deivce should return the disk device by the new key", managedDevices.get(disk.getId()));
        assertNull("The manged deivce should not return the disk device by the old key", managedDevices.get(beforeOldDiskId));
    }
}
