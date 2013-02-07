package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;

/** A test case for the {@link UpdateVmCommand}. */
@RunWith(MockitoJUnitRunner.class)
public class UpdateVmCommandTest {

    private VM vm;
    private VmStatic vmStatic;
    private UpdateVmCommand<VmManagementParametersBase> command;
    private VDSGroup group;

    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VmDAO vmDAO;
    @Mock
    private VdsDAO vdsDAO;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.MaxVmNameLengthWindows, 15),
            mockConfig(ConfigValues.MaxVmNameLengthNonWindows, 64),
            mockConfig(ConfigValues.SupportedClusterLevels,
                    new HashSet<Version>(Arrays.asList(Version.v2_2, Version.v3_0, Version.v3_1))),
            mockConfig(ConfigValues.VMMinMemorySizeInMB, 256),
            mockConfig(ConfigValues.VM32BitMaxMemorySizeInMB, 20480),
            mockConfig(ConfigValues.PredefinedVMProperties, "3.1", ""),
            mockConfig(ConfigValues.UserDefinedVMProperties, "3.1", ""),
            mockConfig(ConfigValues.PredefinedVMProperties, "3.0", ""),
            mockConfig(ConfigValues.UserDefinedVMProperties, "3.0", ""),
            mockConfig(ConfigValues.ValidNumOfMonitors, "1,2,4"),
            mockConfig(ConfigValues.VmPriorityMaxValue, 100),
            mockConfig(ConfigValues.MaxNumOfVmCpus, "3.0", 16),
            mockConfig(ConfigValues.MaxNumOfVmSockets, "3.0", 16),
            mockConfig(ConfigValues.MaxNumOfCpuPerSocket, "3.0", 16)
            );

    @Before
    public void setUp() {
        VmHandler.Init();
        vm = new VM();
        vmStatic = new VmStatic();
        group = new VDSGroup();
        group.setId(Guid.NewGuid());
        group.setcompatibility_version(Version.v3_0);

        vm.setVdsGroupId(group.getId());
        vmStatic.setVdsGroupId(group.getId());

        VmManagementParametersBase params = new VmManagementParametersBase();
        params.setCommandType(VdcActionType.UpdateVm);
        params.setVmStaticData(vmStatic);

        command = spy(new UpdateVmCommand<VmManagementParametersBase>(params) {
            @Override
            protected VDSGroup getVdsGroup() {
                return group;
            }
        });
        doReturn(vm).when(command).getVm();
        doReturn(backendInternal).when(command).getBackend();
    }

    @Test
    public void testLongName() {
        vmStatic.setVmName("this_should_be_very_long_vm_name_so_it will_fail_can_do_action_validation");
        assertFalse("canDoAction should fail for too long vm name.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void testValidName() {
        prepareVmToPassCanDoAction();

        assertTrue("canDoAction should have passed.", command.canDoAction());
    }

    @Test
    public void testChangeToExistingName() {
        prepareVmToPassCanDoAction();
        mockSameNameQuery(true);

        assertFalse("canDoAction should have failed with vm name already in use.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST);
    }

    @Test
    public void testNameNotChanged() {
        prepareVmToPassCanDoAction();
        vm.setVmName("vm1");
        mockSameNameQuery(true);

        assertTrue("canDoAction should have passed.", command.canDoAction());
    }

    @Test
    public void testInvalidMemory() {
        prepareVmToPassCanDoAction();
        vmStatic.setMemSizeMb(99999);

        assertFalse("canDoAction should have failed with invalid memory.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_MEMORY_SIZE);
    }

    @Test
    public void testDedicatedHostNotExist() {
        prepareVmToPassCanDoAction();

        // this will cause null to return when getting vds from vdsDAO
        doReturn(vdsDAO).when(command).getVdsDAO();

        vmStatic.setDedicatedVmForVds(Guid.NewGuid());

        assertFalse("canDoAction should have failed with invalid dedicated host.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER);
    }

    @Test
    public void testDedicatedHostNotInSameCluster() {
        prepareVmToPassCanDoAction();

        VDS vds = new VDS();
        vds.setVdsGroupId(Guid.NewGuid());
        doReturn(vdsDAO).when(command).getVdsDAO();
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        vmStatic.setDedicatedVmForVds(Guid.NewGuid());

        assertFalse("canDoAction should have failed with invalid dedicated host.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER);
    }

    @Test
    public void testValidDedicatedHost() {
        prepareVmToPassCanDoAction();

        VDS vds = new VDS();
        vds.setVdsGroupId(group.getId());
        doReturn(vdsDAO).when(command).getVdsDAO();
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        vmStatic.setDedicatedVmForVds(Guid.NewGuid());

        assertTrue("canDoAction should have passed.", command.canDoAction());
    }

    @Test
    public void testInvalidNumberOfMonitors() {
        prepareVmToPassCanDoAction();
        vmStatic.setNumOfMonitors(99);

        assertFalse("canDoAction should have failed with invalid number of monitors.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_NUM_OF_MONITORS);
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
        vmStatic.setQuotaDefault(false);

        assertTrue("Quota default should be updatable", command.areUpdatedFieldsLegal());
    }

    @Test
    public void testChangeClusterForbidden() {
        prepareVmToPassCanDoAction();
        vmStatic.setVdsGroupId(Guid.NewGuid());

        assertFalse("canDoAction should have failed with cant change cluster.", command.canDoAction());
        assertCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_CLUSTER);
    }

    private void prepareVmToPassCanDoAction() {
        vmStatic.setVmName("vm1");
        vmStatic.setMemSizeMb(256);
        mockVmDaoGetVm();
        mockSameNameQuery(false);
        mockValidateCustomProperties();
    }

    private void assertCanDoActionMessage(VdcBllMessages msg) {
        assertTrue("canDoAction failed for the wrong reason",
                command.getReturnValue()
                        .getCanDoActionMessages()
                        .contains(msg.name()));
    }

    private void mockVmDaoGetVm() {
        doReturn(vmDAO).when(command).getVmDAO();
        when(vmDAO.get(any(Guid.class))).thenReturn(vm);
    }

    private void mockValidateCustomProperties() {
        doReturn(Collections.<ValidationError>emptyList()).when(command).validateCustomProperties(any(VmStatic.class));
    }

    private void mockSameNameQuery(boolean result) {
        VdcQueryReturnValue returnValue = mock(VdcQueryReturnValue.class);
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), any(IsVmWithSameNameExistParameters.class)))
                .thenReturn(returnValue);
        when(returnValue.getReturnValue()).thenReturn(result);
    }
}
