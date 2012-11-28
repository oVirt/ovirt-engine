package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;

public class ImportVmCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

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
        return new TestHelperImportVmCommand(createParameters());
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
        String string300 = string100 + string100 + string100;
        checkVmName(true, string300);
    }

    @Test
    public void testDoNotValidateNameSizeImport() {
        String string300 = string100 + string100 + string100;
        checkVmName(false, string300);
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
        ImportVmCommand command =
                spy(new ImportVmCommand(parameters));
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertEquals(validate.isEmpty(), !isImportAsNewEntity);
    }

    /**
     * Checking that other fields in vmStatic aren't get validated in Import or
     * import as cloned.
     * Unfortunately the other validations in VmStatic are max size of 4000 chars,
     * so I check UserDefinedProperties with String.length = 5000
     */
    @Test
    public void testOtherFieldsNotValidatedInImport() {
        ImportVmParameters parameters = createParameters();
        StringBuilder builder = new StringBuilder();
        // 50 * string 100 = string5000
        for (int i = 0; i < 50; i++) {
            builder.append(string100);
        }

        String string5000 = builder.toString();
        assertFalse(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE > string5000.length());
        parameters.getVm().setUserDefinedProperties(string5000);
        parameters.setImportAsNewEntity(true);
        ImportVmCommand command =
                spy(new ImportVmCommand(parameters));
        Set<ConstraintViolation<ImportVmParameters>> validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
        parameters.getVm().setUserDefinedProperties(builder.toString());
        parameters.setImportAsNewEntity(false);
        command = spy(new ImportVmCommand(parameters));
        validate =
                ValidationUtils.getValidator().validate(parameters,
                        command.getValidationGroups().toArray(new Class<?>[0]));
        assertTrue(validate.isEmpty());
    }

}
