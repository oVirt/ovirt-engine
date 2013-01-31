package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/** A test case for {@link AddVmTemplateCommand} */
@RunWith(MockitoJUnitRunner.class)
public class AddVmTemplateCommandTest {

    private AddVmTemplateCommand<AddVmTemplateParameters> cmd;
    private VM vm;
    private VDSGroup vdsGroup;

    @Mock
    private VmDAO vmDao;

    @Mock
    private VdsGroupDAO vdsGroupDao;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule
            (mockConfig(ConfigValues.VMMinMemorySizeInMB, 0),
                    mockConfig(ConfigValues.VM64BitMaxMemorySizeInMB, Version.v3_2.toString(), 100),
                    mockConfig(ConfigValues.VmPriorityMaxValue, 100));


    @Before
    public void setUp() {
        // The VM to use
        Guid vmId = Guid.NewGuid();
        Guid vdsGroupId = Guid.NewGuid();
        Guid spId = Guid.NewGuid();

        vm = new VM();
        vm.setId(vmId);
        vm.setVdsGroupId(vdsGroupId);
        vm.setStoragePoolId(spId);
        vm.setVmOs(VmOsType.RHEL6x64);
        when(vmDao.get(vmId)).thenReturn(vm);

        // The cluster to use
        vdsGroup = new VDSGroup();
        vdsGroup.setId(vdsGroupId);
        vdsGroup.setStoragePoolId(spId);
        vdsGroup.setcompatibility_version(Version.v3_2);
        when(vdsGroupDao.get(vdsGroupId)).thenReturn(vdsGroup);

        AddVmTemplateParameters params = new AddVmTemplateParameters(vm, "templateName", "Template for testing");

        // Using the compensation constructor since the normal one contains DB access
        cmd = spy(new AddVmTemplateCommand<AddVmTemplateParameters>(params));
        doReturn(vmDao).when(cmd).getVmDAO();
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDAO();
        cmd.setVmId(vmId);
        cmd.setVdsGroupId(vdsGroupId);
    }

    @Test
    public void testCanDoAction() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Up);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
    }
}
