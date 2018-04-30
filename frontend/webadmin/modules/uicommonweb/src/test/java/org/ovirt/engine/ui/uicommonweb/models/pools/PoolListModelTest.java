package org.ovirt.engine.ui.uicommonweb.models.pools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmListModelTest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PoolListModelTest extends BaseVmListModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    PoolModel model;

    @BeforeEach
    public void setUp()  {
        setUpUnitVmModelExpectations(model);
    }

    @Test
    public void testBuildVmOnSave() {
        VM vm = PoolListModel.buildVmOnSave(model);
        verifyBuiltPoolVm(vm);
    }

    private void verifyBuiltPoolVm(VM vm) {
        verifyBuiltCoreVm(vm.getStaticData());
        verifyBuiltKernelOptions(vm.getStaticData());
        verifyBuiltMigrationOptions(vm.getStaticData());
        verifyBuiltVmSpecific(vm);

        assertEquals(VM_NAME, vm.getName());
        assertEquals(USB_POLICY, vm.getUsbPolicy());
        assertEquals(HOST_ID, vm.getDedicatedVmForVdsList().get(0));
    }

}
