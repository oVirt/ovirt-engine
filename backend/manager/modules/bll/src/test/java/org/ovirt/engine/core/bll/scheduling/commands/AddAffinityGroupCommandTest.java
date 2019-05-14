package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddAffinityGroupCommandTest extends BaseCommandTest {

    Guid clusterId = Guid.newGuid();
    Guid vmId = Guid.newGuid();
    private Guid vmLabelId = Guid.newGuid();
    private Guid hostLabelId = Guid.newGuid();

    @Mock
    AffinityGroupDao affinityGroupDao;
    @Mock
    private LabelDao labelDao;
    @Mock
    VmStaticDao vmStaticDao;

    @Mock
    private VdsStaticDao vdsStaticDao;

    AffinityGroupCRUDParameters parameters = new AffinityGroupCRUDParameters(null, createAffinityGroup());

    @Spy
    @InjectMocks
    AddAffinityGroupCommand command = new AddAffinityGroupCommand(parameters, null);

    private AffinityGroup affinityGroup;

    @BeforeEach
    public void setup() {
        command.setCluster(new Cluster());

        VmStatic vmStatic = new VmStatic();
        vmStatic.setClusterId(clusterId);
        vmStatic.setId(vmId);
        doReturn(Collections.singletonList(vmStatic)).when(vmStaticDao).getByIds(any());

        Label vmLabel = new LabelBuilder().id(vmLabelId).build();
        Label hostLabel = new LabelBuilder().id(hostLabelId).build();
        doReturn(Arrays.asList(vmLabel, hostLabel)).when(labelDao).getAllByIds(any());
    }

    @Test
    public void excuteCommandTest() {
        command.executeCommand();
        assertEquals(AuditLogType.USER_ADDED_AFFINITY_GROUP, command.getAuditLogTypeValue());
    }

    @Test
    public void validate_vmNameExists_Test() {
        doReturn(new AffinityGroup()).when(affinityGroupDao).getByName(any());
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
        doReturn(Collections.emptyList()).when(vmStaticDao).getByIds(any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_ENTITY_FOR_AFFINITY_GROUP);
    }

    @Test
    public void validate_vmNotInCluster_Test() {
        doReturn(Guid.newGuid()).when(command).getClusterId();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_ENTITY_NOT_IN_AFFINITY_GROUP_CLUSTER);
    }

    @Test
    public void validate_duplicateVm_Test() {
        affinityGroup.getVmIds().add(vmId);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_ENTITY_IN_AFFINITY_GROUP);
    }

    @Test
    public void testInvalidLabels() {
        doReturn(Collections.emptyList()).when(labelDao).getAllByIds(any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_LABEL_FOR_AFFINITY_GROUP);
    }

    @Test
    public void testDuplicateLabels() {
        affinityGroup.getVmLabels().add(vmLabelId);
        affinityGroup.getHostLabels().add(hostLabelId);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_LABEL_IN_AFFINITY_GROUP);
    }

    @Test
    public void validate_nonEnforcing_Test() {
        affinityGroup.setVmEnforcing(false);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validate_emptyAffinityGroup() {
        affinityGroup.setVmIds(null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validate_succeed_Test() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private AffinityGroup createAffinityGroup() {
        affinityGroup = new AffinityGroup();
        affinityGroup.setVmIds(new ArrayList<>());
        affinityGroup.getVmIds().add(vmId);
        affinityGroup.setClusterId(clusterId);
        affinityGroup.setVmLabels(new ArrayList<>(Collections.singletonList(vmLabelId)));
        affinityGroup.setHostLabels(new ArrayList<>(Collections.singletonList(hostLabelId)));
        return affinityGroup;
    }
}
