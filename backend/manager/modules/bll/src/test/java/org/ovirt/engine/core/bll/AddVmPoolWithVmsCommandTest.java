package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class AddVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {

    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @SuppressWarnings("serial")
    @Override
    protected AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param =
                new AddVmPoolWithVmsParameters(vmPools, testVm, VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        return spy(new AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(param) {
            @Override
            protected void initTemplate() {
                // do nothing - is done here and not with mockito since it's called in the ctor
            }
        });
    }

    @Test
    public void validateCanDoAction() {
        setupForStorageTests();
        assertTrue(command.canDoAction());
    }

    @Test
    public void validateSufficientSpaceOnDestinationDomains() {
        setupForStorageTests();
        assertTrue(command.checkDestDomains());
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void validateInsufficientSpaceOnDomains() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void validateDomainNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN.toString()));
        verify(multipleSdValidator).allDomainsWithinThresholds();
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void validatePatternBasedPoolName() {
        String patternBaseName = "aa-??bb";
        command.getParameters().getVmStaticData().setName(patternBaseName);
        command.getParameters().getVmPool().setName(patternBaseName);
        assertTrue(command.validateInputs());
    }

    @Test
    public void validateBeanValidations() {
        assertTrue(command.validateInputs());
    }

    private void setupForStorageTests() {
        doReturn(multipleSdValidator).when((AddVmPoolWithVmsCommand) command).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForNewDisks(anyList());
    }
}
