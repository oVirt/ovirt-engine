package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AddAffinityGroupCommandTest extends BaseCommandTest {

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
        command = spy(new AddAffinityGroupCommand(parameters, null));
        doReturn(createAffinityGroup()).when(parameters).getAffinityGroup();
        doReturn(affinityGroupDao).when(command).getAffinityGroupDao();
        doReturn(vmStaticDao).when(command).getVmStaticDao();
        doReturn(new Cluster()).when(command).getCluster();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setClusterId(clusterId);
        doReturn(vmStatic).when(vmStaticDao).get(any(Guid.class));
        doReturn(clusterId).when(command).getClusterId();
    }

    @Test
    public void excuteCommandTest() {
        command.executeCommand();
        assertEquals(command.getAuditLogTypeValue(), AuditLogType.USER_ADDED_AFFINITY_GROUP);
    }

    @Test
    public void validate_vmNameExists_Test() {
        doReturn(new AffinityGroup()).when(affinityGroupDao).getByName(anyString());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
    }

    @Test
    public void validate_clusterNull_Test() {
        doReturn(null).when(command).getCluster();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
    }

    @Test
    public void validate_vmNotExists_Test() {
        doReturn(null).when(vmStaticDao).get(any(Guid.class));
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP);
    }

    @Test
    public void validate_vmNotInCluster_Test() {
        doReturn(Guid.newGuid()).when(command).getClusterId();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER);
    }

    @Test
    public void validate_duplicateVm_Test() {
        affinityGroup.getEntityIds().add(vmId);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP);
    }

    @Test
    public void validate_nonEnforcing_Test() {
        affinityGroup.setEnforcing(false);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validate_emptyAffinityGroup() {
        affinityGroup.setEntityIds(null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validate_succeed_Test() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private AffinityGroup createAffinityGroup() {
        affinityGroup = new AffinityGroup();
        affinityGroup.setEntityIds(new ArrayList<>());
        affinityGroup.getEntityIds().add(vmId);
        return affinityGroup;
    }
}
