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
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
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

    @Mock
    private OsRepository osRepository;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.VmPriorityMaxValue, 100));

    @Before
    public void setUp() {
        // The VM to use
        Guid vmId = Guid.newGuid();
        Guid vdsGroupId = Guid.newGuid();
        Guid spId = Guid.newGuid();

        vm = new VM();
        vm.setId(vmId);
        vm.setVdsGroupId(vdsGroupId);
        vm.setStoragePoolId(spId);
        vm.setVmOs(14);
        when(vmDao.get(vmId)).thenReturn(vm);

        // The cluster to use
        vdsGroup = new VDSGroup();
        vdsGroup.setId(vdsGroupId);
        vdsGroup.setStoragePoolId(spId);
        vdsGroup.setcompatibility_version(Version.v3_2);
        when(vdsGroupDao.get(vdsGroupId)).thenReturn(vdsGroup);
        when(osRepository.getMinimumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(0);
        when(osRepository.getMaximumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(100);
        AddVmTemplateParameters params = new AddVmTemplateParameters(vm, "templateName", "Template for testing");

        // init the injector with the osRepository instance
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        // Using the compensation constructor since the normal one contains DB access
        cmd = spy(new AddVmTemplateCommand<AddVmTemplateParameters>(params) {

            @Override
            protected void updateVmDisks() {
            }

            @Override
            public VM getVm() {
                return vm;
            }
        });
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
