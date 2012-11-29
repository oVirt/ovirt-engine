package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class ImportVmCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Test
    @Ignore
    public void insufficientDiskSpace() {
        final int lotsOfSpace = 1073741824;
        final int diskSpacePct = 0;
        final ImportVmCommand c = setupDiskSpaceTest(lotsOfSpace, diskSpacePct);
        assertFalse(c.canDoAction());
        assertTrue(c.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void sufficientDiskSpace() {
        final int extraDiskSpaceRequired = 0;
        final int diskSpacePct = 0;
        final ImportVmCommand c = setupDiskSpaceTest(extraDiskSpaceRequired, diskSpacePct);
        assertTrue(c.canDoAction());
    }

    private ImportVmCommand setupDiskSpaceTest(final int diskSpaceRequired, final int diskSpacePct) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, diskSpaceRequired);
        mcr.mockConfigValue(ConfigValues.FreeSpaceLow, diskSpacePct);

        ImportVmCommand cmd = spy(new ImportVmCommand(createParameters()));
        doReturn(true).when(cmd).validateNoDuplicateVm();
        doReturn(true).when(cmd).validateVdsCluster();
        doReturn(true).when(cmd).validateUsbPolicy();
        doReturn(true).when(cmd).canAddVm();
        doReturn(createSourceDomain()).when(cmd).getSourceDomain();
        doReturn(Collections.<VM> singletonList(createVM())).when(cmd).getVmsFromExportDomain();
        doReturn(new VmTemplate()).when(cmd).getVmTemplate();
        doReturn(new storage_pool()).when(cmd).getStoragePool();

        return cmd;
    }

    protected ImportVmParameters createParameters() {
        final VM v = createVM();
        v.setVmName("testVm");
        return new ImportVmParameters(v, Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid());
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setId(Guid.NewGuid());
        v.setDiskSize(2);
        return v;
    }

    protected storage_domains createSourceDomain() {
        storage_domains sd = new storage_domains();
        sd.setstorage_domain_type(StorageDomainType.ImportExport);
        sd.setstatus(StorageDomainStatus.Active);
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
        parameters.getVm().setVmName(name);
        parameters.setImportAsNewEntity(isImportAsNewEntity);
        ImportVmCommand command = new ImportVmCommand(parameters);
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
        ImportVmCommand command = new ImportVmCommand(parameters);
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
        parameters.getVm().setUserDefinedProperties(tooLongString);
        parameters.setImportAsNewEntity(false);
        command = new ImportVmCommand(parameters);
        validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }
}
