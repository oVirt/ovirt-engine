package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;


@RunWith(MockitoJUnitRunner.class)
public class AddAffinityGroupCommandTest {

    Guid clusterId = Guid.newGuid();
    Guid vmId = Guid.newGuid();
    AddAffinityGroupCommand command;

    @Mock
    AffinityGroupDao affinityGroupDao;

    @Mock
    VmStaticDao vmStaticDao;

    @Mock
    AffinityGroupCRUDParameters parameters;

    private AffinityGroup affinityGroup;

    @Before
    public void setup() {
        command = spy(new AddAffinityGroupCommand(parameters));
        doReturn(createAffinityGroup()).when(parameters).getAffinityGroup();
        doReturn(affinityGroupDao).when(command).getAffinityGroupDao();
        doReturn(vmStaticDao).when(command).getVmStaticDao();
        doReturn(new VDSGroup()).when(command).getVdsGroup();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setVdsGroupId(clusterId);
        doReturn(vmStatic).when(vmStaticDao).get(any(Guid.class));
        doReturn(clusterId).when(command).getVdsGroupId();
    }

    @Test
    public void excuteCommandTest() {
        command.executeCommand();
        assertEquals(command.getAuditLogTypeValue(), AuditLogType.USER_ADDED_AFFINITY_GROUP);
    }

    @Test
    public void canDoAction_vmNameExists_Test() {
        doReturn(new AffinityGroup()).when(affinityGroupDao).getByName(anyString());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
    }

    @Test
    public void canDoAction_vdsGroupNull_Test() {
        doReturn(null).when(command).getVdsGroup();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
    }

    @Test
    public void canDoAction_vmNotExists_Test() {
        doReturn(null).when(vmStaticDao).get(any(Guid.class));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP);
    }

    @Test
    public void canDoAction_vmNotInCluster_Test() {
        doReturn(Guid.newGuid()).when(command).getVdsGroupId();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER);
    }

    @Test
    public void canDoAction_duplicateVm_Test() {
        affinityGroup.getEntityIds().add(vmId);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP);
    }

    @Test
    public void canDoAction_succeed_Test() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    private AffinityGroup createAffinityGroup() {
        affinityGroup = new AffinityGroup();
        affinityGroup.setEntityIds(new ArrayList<Guid>());
        affinityGroup.getEntityIds().add(vmId);
        return affinityGroup;
    }
}
