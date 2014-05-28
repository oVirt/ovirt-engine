package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmListModelTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

@RunWith(MockitoJUnitRunner.class)
public class UserPortalListModelTest extends BaseVmListModelTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    VM origVm;

    @Before
    public void setUp() {
        setUpUnitVmModelExpectations(model);

        origVm = new VM();
        setUpOrigVm(origVm);
    }

    @Test
    public void testBuildVmOnNewTemplate() {
        VM vm = UserPortalListModel.buildVmOnNewTemplate(model, origVm);

        verifyBuiltCommonVm(vm.getStaticData());
        verifyBuiltOrigVm(origVm, vm);
    }

    @Test
    public void testBuildVmOnSave() {
        VM vm = new VM();
        UserPortalListModel.buildVmOnSave(model, vm);
        verifyBuiltVm(vm);
    }
}
