package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

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
public class VmListModelTest extends BaseVmListModelTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    VM origVm;

    @BeforeEach
    public void setUp() {
        setUpUnitVmModelExpectations(model);

        origVm = new VM();
        setUpOrigVm(origVm);
        origVm.setDedicatedVmForVdsList(Collections.singletonList(HOST_ID_2));
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
