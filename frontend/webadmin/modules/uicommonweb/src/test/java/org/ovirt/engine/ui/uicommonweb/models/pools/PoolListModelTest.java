package org.ovirt.engine.ui.uicommonweb.models.pools;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmListModelTest;

@RunWith(MockitoJUnitRunner.class)
public class PoolListModelTest extends BaseVmListModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    PoolModel model;

    @Before
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
