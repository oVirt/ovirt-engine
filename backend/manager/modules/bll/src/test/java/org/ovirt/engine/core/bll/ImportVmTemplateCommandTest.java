package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.springframework.util.Assert;

public class ImportVmTemplateCommandTest {

    @Test
    public void insufficientDiskSpace() {
        // The following is enough since the validation is mocked out anyway. Just want to make sure the flow in CDA is correct.
        // Full test for the scenarios is done in the inherited class.
        final ImportVmTemplateCommand command = setupVolumeFormatAndTypeTest(VolumeFormat.RAW, VolumeType.Preallocated, StorageType.NFS);
        doReturn(false).when(command).validateSpaceRequirements(anyList());
        assertFalse(command.canDoAction());
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

    private void assertValidVolumeInfoCombination(VolumeFormat volumeFormat,
            VolumeType volumeType,
            StorageType storageType) {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(
                setupVolumeFormatAndTypeTest(volumeFormat, volumeType, storageType));
    }

    private void assertInvalidVolumeInfoCombination(VolumeFormat volumeFormat,
            VolumeType volumeType,
            StorageType storageType) {
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
                setupVolumeFormatAndTypeTest(volumeFormat, volumeType, storageType),
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_CONFIGURATION_NOT_SUPPORTED);
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
     * @return The command which can be called to test the given combination.
     */
    private ImportVmTemplateCommand setupVolumeFormatAndTypeTest(
            VolumeFormat volumeFormat,
            VolumeType volumeType,
            StorageType storageType) {
        ImportVmTemplateCommand command = spy(new ImportVmTemplateCommand(createParameters()){
            @Override
            public VDSGroup getVdsGroup() {
                return null;
            }
        });

        Backend backend = mock(Backend.class);
        doReturn(backend).when(command).getBackend();
        doReturn(false).when(command).isVmTemplateWithSameNameExist();
        doReturn(true).when(command).isVDSGroupCompatible();
        doReturn(true).when(command).validateNoDuplicateDiskImages(any(Iterable.class));
        mockGetTemplatesFromExportDomainQuery(volumeFormat, volumeType, command);
        mockStorageDomainStatic(command, storageType);
        doReturn(mock(VmTemplateDAO.class)).when(command).getVmTemplateDAO();
        doReturn(Mockito.mock(MacPoolManagerStrategy.class)).when(command).getMacPool();
        mockStoragePool(command);
        mockStorageDomains(command);
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(true).when(command).setAndValidateCpuProfile();
        doReturn(true).when(command).validateSpaceRequirements(anyList());

        return command;
    }

    private static void mockStorageDomains(ImportVmTemplateCommand command) {
        final ImportVmTemplateParameters parameters = command.getParameters();
        final StorageDomainDAO dao = mock(StorageDomainDAO.class);

        final StorageDomain srcDomain = new StorageDomain();
        srcDomain.setStorageDomainType(StorageDomainType.ImportExport);
        srcDomain.setStatus(StorageDomainStatus.Active);
        when(dao.getForStoragePool(parameters.getSourceDomainId(), parameters.getStoragePoolId()))
                .thenReturn(srcDomain);

        final StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageDomainType(StorageDomainType.Data);
        destDomain.setUsedDiskSize(0);
        destDomain.setAvailableDiskSize(1000);
        destDomain.setStatus(StorageDomainStatus.Active);
        when(dao.getForStoragePool(parameters.getDestDomainId(), parameters.getStoragePoolId()))
                .thenReturn(destDomain);

        doReturn(dao).when(command).getStorageDomainDAO();
    }

    private static void mockStoragePool(ImportVmTemplateCommand command) {
        final StoragePoolDAO dao = mock(StoragePoolDAO.class);

        final StoragePool pool = new StoragePool();
        pool.setId(command.getParameters().getStoragePoolId());
        when(dao.get(any(Guid.class))).thenReturn(pool);

        doReturn(dao).when(command).getStoragePoolDAO();
    }

    private static void mockGetTemplatesFromExportDomainQuery(VolumeFormat volumeFormat,
            VolumeType volumeType,
            ImportVmTemplateCommand command) {
        final VdcQueryReturnValue result = new VdcQueryReturnValue();
        Map<VmTemplate, List<DiskImage>> resultMap = new HashMap<VmTemplate, List<DiskImage>>();

        DiskImage image = new DiskImage();
        image.setActualSizeInBytes(2);
        image.setvolumeFormat(volumeFormat);
        image.setVolumeType(volumeType);

        resultMap.put(new VmTemplate(), Arrays.asList(image));
        result.setReturnValue(resultMap);
        result.setSucceeded(true);

        when(command.getBackend().runInternalQuery(eq(VdcQueryType.GetTemplatesFromExportDomain),
                any(VdcQueryParametersBase.class), any(EngineContext.class))).thenReturn(result);
    }

    private static void mockStorageDomainStatic(
            ImportVmTemplateCommand command,
            StorageType storageType) {
        final StorageDomainStaticDAO dao = mock(StorageDomainStaticDAO.class);

        final StorageDomainStatic domain = new StorageDomainStatic();
        domain.setStorageType(storageType);
        when(dao.get(any(Guid.class))).thenReturn(domain);

        doReturn(dao).when(command).getStorageDomainStaticDAO();
    }

    protected ImportVmTemplateParameters createParameters() {
        VmTemplate t = new VmTemplate();
        t.setName("testTemplate");
        final ImportVmTemplateParameters p =
                new ImportVmTemplateParameters(Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), t);
        return p;
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
        ImportVmTemplateParameters parameters = createParameters();
        parameters.getVmTemplate().setName(name);
        parameters.setImportAsNewEntity(isImportAsNewEntity);
        ImportVmTemplateCommand command =
                spy(new ImportVmTemplateCommand(parameters) {
                    @Override
                    public VDSGroup getVdsGroup() {
                        return null;
                    }
                });
        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        Assert.isTrue(validate.isEmpty() == !isImportAsNewEntity);
    }

    /**
     * Checking that other fields in VmTemplate aren't get validated in Import or
     * import as cloned.
     * testing that 100 char String is set in domain field
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        ImportVmTemplateParameters parameters = createParameters();

        assertFalse(BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE > string100.length());
        parameters.setImportAsNewEntity(true);
        ImportVmTemplateCommand command =
                spy(new ImportVmTemplateCommand(parameters) {
                    @Override
                    public VDSGroup getVdsGroup() {
                        return null;
                    }
                });
        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        Assert.isTrue(validate.isEmpty());
        parameters.setImportAsNewEntity(false);
        command = spy(new ImportVmTemplateCommand(parameters) {
                    @Override
                    public VDSGroup getVdsGroup() {
                        return null;
                    }
                });
        validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        Assert.isTrue(validate.isEmpty());
    }

    /**
     * Checking that managed device are sync with the new Guids of disk
     */
    @Test
    public void testManagedDeviceSyncWithNewDiskId() {
        ImportVmTemplateParameters parameters = createParameters();
        ImportVmTemplateCommand command = spy(new ImportVmTemplateCommand(parameters){
                    @Override
                    public VDSGroup getVdsGroup() {
                        return null;
                    }
                });
        DiskImage disk = new DiskImage();
        disk.setStorageIds(new ArrayList<Guid>());
        Map<Guid, VmDevice> managedDevices = new HashMap<>();
        managedDevices.put(disk.getId(), new VmDevice());
        Guid beforeOldDiskId = disk.getId();
        command.generateNewDiskId(disk);
        command.updateManagedDeviceMap(disk, managedDevices);
        Guid oldDiskId = command.newDiskIdForDisk.get(disk.getId()).getId();
        assertEquals("The old disk id should be similar to the value at the newDiskIdForDisk.", beforeOldDiskId, oldDiskId);
        assertNotNull("The manged deivce should return the disk device by the new key", managedDevices.get(disk.getId()));
        assertNull("The manged deivce should not return the disk device by the old key", managedDevices.get(beforeOldDiskId));
    }

}
