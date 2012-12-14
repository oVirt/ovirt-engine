package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.springframework.util.Assert;

public class ImportVmTemplateCommandTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Test
    public void insufficientDiskSpace() {
        final int lotsOfSpaceRequired = 1073741824;
        final int pctOfSpaceRequired = 0;
        final ImportVmTemplateCommand c =
                setupDiskSpaceTest(lotsOfSpaceRequired, pctOfSpaceRequired);
        assertFalse(c.canDoAction());
        assertTrue(c.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void sufficientDiskSpace() {
        final int pctOfSpaceRequired = 0;
        final ImportVmTemplateCommand c =
                setupDiskSpaceTest(0, pctOfSpaceRequired);
        assertTrue(c.canDoAction());
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
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, 0);
        mcr.mockConfigValue(ConfigValues.FreeSpaceLow, 0);

        ImportVmTemplateCommand command =
                spy(new ImportVmTemplateCommand(createParameters()));

        Backend backend = mock(Backend.class);
        doReturn(backend).when(command).getBackend();
        doReturn(false).when(command).isVmTemplateWithSameNameExist();
        mockGetTemplatesFromExportDomainQuery(volumeFormat, volumeType, command);
        mockStorageDomainStatic(command, storageType);
        doReturn(mock(VmTemplateDAO.class)).when(command).getVmTemplateDAO();
        mockStoragePool(command);
        mockStorageDomains(command);

        return command;
    }

    private static void mockStorageDomains(ImportVmTemplateCommand command) {
        final ImportVmTemplateParameters parameters = command.getParameters();
        final StorageDomainDAO dao = mock(StorageDomainDAO.class);

        final storage_domains srcDomain = new storage_domains();
        srcDomain.setstorage_domain_type(StorageDomainType.ImportExport);
        srcDomain.setstatus(StorageDomainStatus.Active);
        when(dao.getForStoragePool(parameters.getSourceDomainId(), parameters.getStoragePoolId()))
                .thenReturn(srcDomain);

        final storage_domains destDomain = new storage_domains();
        destDomain.setstorage_domain_type(StorageDomainType.Data);
        destDomain.setused_disk_size(0);
        destDomain.setavailable_disk_size(1000);
        destDomain.setstatus(StorageDomainStatus.Active);
        when(dao.getForStoragePool(parameters.getDestDomainId(), parameters.getStoragePoolId()))
                .thenReturn(destDomain);

        doReturn(dao).when(command).getStorageDomainDAO();
    }

    private static void mockStoragePool(ImportVmTemplateCommand command) {
        final StoragePoolDAO dao = mock(StoragePoolDAO.class);

        final storage_pool pool = new storage_pool();
        pool.setId(command.getParameters().getStoragePoolId());
        when(dao.get(any(Guid.class))).thenReturn(pool);

        doReturn(dao).when(command).getStoragePoolDAO();
    }

    private static void mockGetTemplatesFromExportDomainQuery(VolumeFormat volumeFormat,
            VolumeType volumeType,
            ImportVmTemplateCommand command) {
        final VdcQueryReturnValue result = new VdcQueryReturnValue();
        Map<VmTemplate, DiskImageList> resultMap = new HashMap<VmTemplate, DiskImageList>();

        DiskImage image = new DiskImage();
        image.setactual_size(2);
        image.setvolume_format(volumeFormat);
        image.setvolume_type(volumeType);

        resultMap.put(new VmTemplate(), new DiskImageList(Arrays.asList(image)));
        result.setReturnValue(resultMap);
        result.setSucceeded(true);

        when(command.getBackend().runInternalQuery(eq(VdcQueryType.GetTemplatesFromExportDomain),
                any(VdcQueryParametersBase.class))).thenReturn(result);
    }

    private static void mockStorageDomainStatic(
            ImportVmTemplateCommand command,
            StorageType storageType) {
        final StorageDomainStaticDAO dao = mock(StorageDomainStaticDAO.class);

        final StorageDomainStatic domain = new StorageDomainStatic();
        domain.setstorage_type(storageType);
        when(dao.get(any(Guid.class))).thenReturn(domain);

        doReturn(dao).when(command).getStorageDomainStaticDAO();
    }

    private ImportVmTemplateCommand setupDiskSpaceTest(final int extraDiskSpaceRequired,
            final int pctOfSpaceRequired) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, extraDiskSpaceRequired);
        mcr.mockConfigValue(ConfigValues.FreeSpaceLow, pctOfSpaceRequired);
        return new TestHelperImportVmTemplateCommand(createParameters());
    }

    protected ImportVmTemplateParameters createParameters() {
        VmTemplate t = new VmTemplate();
        t.setname("testTemplate");
        final ImportVmTemplateParameters p =
                new ImportVmTemplateParameters(Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid(), t);
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
        parameters.getVmTemplate().setname(name);
        parameters.setImportAsNewEntity(isImportAsNewEntity);
        ImportVmTemplateCommand command =
                spy(new ImportVmTemplateCommand(parameters));
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
        parameters.getVmTemplate().setdomain(string100);
        parameters.setImportAsNewEntity(true);
        ImportVmTemplateCommand command =
                spy(new ImportVmTemplateCommand(parameters));
        Set<ConstraintViolation<ImportVmTemplateParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        Assert.isTrue(validate.isEmpty());
        parameters.getVmTemplate().setdomain(string100);
        parameters.setImportAsNewEntity(false);
        command = spy(new ImportVmTemplateCommand(parameters));
        validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        Assert.isTrue(validate.isEmpty());
    }
}
