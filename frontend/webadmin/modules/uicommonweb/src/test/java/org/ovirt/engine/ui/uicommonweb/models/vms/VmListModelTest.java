package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;

@RunWith(MockitoJUnitRunner.class)
public class VmListModelTest extends BaseVmListModelTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    VM origVm;

    @Before
    public void setUp() {
        setUpUnitVmModelExpectations(model);

        origVm = new VM();
        setUpOrigVm(origVm);
        origVm.setDedicatedVmForVdsList(HOST_ID_2);
        origVm.setMigrationSupport(MIGRATION_SUPPORT_2);
        origVm.setMigrationDowntime(MIGRATION_DOWNTIME_2);
    }

    @Test
    public void testBuildVmOnNewTemplate() {
        VM vm = VmListModel.buildVmOnNewTemplate(model, origVm);

        verifyBuiltCommonVm(vm.getStaticData());
        verifyBuiltOrigVm(origVm, vm);

        assertEquals(origVm.getDedicatedVmForVdsList(), vm.getDedicatedVmForVdsList());
        assertEquals(origVm.getMigrationSupport(), vm.getMigrationSupport());
        assertEquals(origVm.getMigrationDowntime(), vm.getMigrationDowntime());
    }

    @Test
    public void testBuildVmOnSave() {
        VM vm = new VM();
        VmListModel.buildVmOnSave(model, vm);
        verifyBuiltVm(vm);
    }
}
