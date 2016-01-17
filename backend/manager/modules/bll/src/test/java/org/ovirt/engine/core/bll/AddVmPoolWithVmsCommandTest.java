package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;

@RunWith(MockitoJUnitRunner.class)
public class AddVmPoolWithVmsCommandTest extends CommonVmPoolWithVmsCommandTestAbstract {

    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @Override
    protected AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> createCommand() {
        AddVmPoolWithVmsParameters param =
                new AddVmPoolWithVmsParameters(vmPools, testVm, VM_COUNT, DISK_SIZE);
        param.setStorageDomainId(firstStorageDomainId);
        AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters> command =
                spy(new AddVmPoolWithVmsCommand<AddVmPoolWithVmsParameters>(
                        param, CommandContext.createContext(param.getSessionId())) {
                    @Override
                    protected void initUser() {
                    }

                    @Override
                    protected void initTemplate() {
                        // do nothing - is done here and not with mockito since it's called in the ctor
                    }
                });
        return command;
    }

    @Test
    public void validate() {
        setupForStorageTests();
        assertTrue(command.validate());
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
}
