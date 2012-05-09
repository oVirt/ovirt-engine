package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;

/** A test case for the {@link UpdateVmCommand}. */
public class UpdateVmCommandTest {

    private VM vm;
    private VmStatic vmStatic;
    private UpdateVmCommand<VmManagementParametersBase> command;

    @Before
    public void setUp() {
        VmHandler.Init();
        vm = new VM();
        vmStatic = new VmStatic();

        VmManagementParametersBase params = new VmManagementParametersBase();
        params.setCommandType(VdcActionType.UpdateVm);
        params.setVmStaticData(vmStatic);

        command = spy(new UpdateVmCommand<VmManagementParametersBase>(params));
        doReturn(vm).when(command).getVm();
    }

    @Test
    public void testUpdateFieldsQuotaEnforcementType() {
        vm.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);
        vmStatic.setQuotaEnforcementType(QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);

        assertTrue("Quota enforcement type should be updatable", command.areUpdatedFieldsLegal());
    }

    @Test
    public void testUpdateFieldsQutoaDefault() {
        vm.setIsQuotaDefault(true);
        vmStatic.setIsQuotaDefault(false);

        assertTrue("Quota default should be updatable", command.areUpdatedFieldsLegal());
    }

}
