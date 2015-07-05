package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class EditAffinityGroupCommandTest {
    private static final String AFFINITY_GROUP_NAME = "test123";
    Guid clusterId = Guid.newGuid();
    EditAffinityGroupCommand command;

    @Mock
    AffinityGroupDao affinityGroupDao;

    @Mock
    VmStaticDao vmStaticDao;

    @Mock
    AffinityGroupCRUDParameters parameters;

    private AffinityGroup affinityGroup;

    @Before
    public void setup() {
        doReturn(createAffinityGroup()).when(parameters).getAffinityGroup();
        command = spy(new EditAffinityGroupCommand(parameters) {
            @Override
            protected AffinityGroup getAffinityGroup() {
                AffinityGroup affinityGroup2 = new AffinityGroup();
                affinityGroup2.setClusterId(clusterId);
                affinityGroup2.setName(AFFINITY_GROUP_NAME + "##");
                return affinityGroup2;
            }
        });
        doReturn(affinityGroupDao).when(command).getAffinityGroupDao();
        doReturn(vmStaticDao).when(command).getVmStaticDao();
        doReturn(new VDSGroup()).when(command).getVdsGroup();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setVdsGroupId(clusterId);
        doReturn(vmStatic).when(vmStaticDao).get(any(Guid.class));
        doReturn(clusterId).when(command).getVdsGroupId();
    }

    @Test
    public void canDoAction_noAffinityGroup_Test() {
        doReturn(null).when(command).getAffinityGroup();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
    }

    @Test
    public void canDoAction_changeClusterIds_Test() {
        doReturn(Guid.newGuid()).when(command).getClusterId();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_CLUSTER_ID);
    }

    @Test
    public void canDoAction_testNameChange_Test() {
        doReturn(new AffinityGroup()).when(affinityGroupDao).getByName(affinityGroup.getName());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
    }

    @Test
    public void canDoAction_succeed_Test() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
        // validateParameters is tests at {@link AddAffinityGroupCommandTest}
        verify(command, times(1)).validateParameters();
    }

    @Test
    public void excuteCommandTest() {
        command.executeCommand();
        assertEquals(command.getAuditLogTypeValue(), AuditLogType.USER_UPDATED_AFFINITY_GROUP);
    }

    private AffinityGroup createAffinityGroup() {
        affinityGroup = new AffinityGroup();
        affinityGroup.setName(AFFINITY_GROUP_NAME);
        affinityGroup.setClusterId(clusterId);
        affinityGroup.setEntityIds(new ArrayList<Guid>());
        return affinityGroup;
    }
}
