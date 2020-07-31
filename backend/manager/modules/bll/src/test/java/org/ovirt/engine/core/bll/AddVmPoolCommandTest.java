package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmPoolCommandTest extends CommonVmPoolCommandTestAbstract {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxIoThreadsPerVm, 127),
                MockConfigDescriptor.of(ConfigValues.ValidNumOfMonitors, Arrays.asList("1", "2", "4")),
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @Override
    protected AddVmPoolCommand<AddVmPoolParameters> createCommand() {
        AddVmPoolParameters param = new AddVmPoolParameters(vmPools, testVm, VM_COUNT);
        param.setStorageDomainId(firstStorageDomainId);
        return new AddVmPoolCommand<>(param, null);
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
