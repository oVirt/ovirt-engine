package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;

@RunWith(MockitoJUnitRunner.class)
public class VmSnapshotListModelTest extends BaseVmListModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    @Before
    public void setUp()  {
        setUpUnitVmModelExpectations(model);
    }

    @Test
    public void testBuildVmOnClone() {
        VM vm = new VM();

        VmSnapshotListModel.buildVmOnClone(model, vm);

        verifyBuiltVm(vm);
    }
}
