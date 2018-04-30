package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VM;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmSnapshotListModelTest extends BaseVmListModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    @BeforeEach
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
