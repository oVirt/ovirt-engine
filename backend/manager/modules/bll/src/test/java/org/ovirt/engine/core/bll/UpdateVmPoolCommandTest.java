package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateVmPoolCommandTest extends CommonVmPoolCommandTestAbstract {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

    @Override
    protected UpdateVmPoolCommand<AddVmPoolParameters> createCommand() {
        AddVmPoolParameters param = new AddVmPoolParameters(vmPools, testVm, VM_COUNT);
        param.setStorageDomainId(firstStorageDomainId);
        return new UpdateVmPoolCommand<>(param, null);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void validate() {
        mockVMPoolDao();
        mockVMDao();
        setupForStorageTests();
        assertTrue(command.validate());
    }

    private void mockVMPoolDao() {
        when(vmPoolDao.get(vmPoolId)).thenReturn(vmPools);
    }
    private void mockVMDao() {
        when(vmDao.getAllForVmPool(vmPoolId)).thenReturn(vms);
    }
}
