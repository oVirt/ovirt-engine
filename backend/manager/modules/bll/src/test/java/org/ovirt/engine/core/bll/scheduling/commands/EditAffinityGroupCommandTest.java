package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
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
public class EditAffinityGroupCommandTest extends BaseCommandTest {
    private static final String AFFINITY_GROUP_NAME = "test123";
    Guid clusterId = Guid.newGuid();

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
    EditAffinityGroupCommand command = new EditAffinityGroupCommand(parameters, null);

    private AffinityGroup affinityGroup;

    @BeforeEach
    public void setup() {
        AffinityGroup affinityGroup2 = new AffinityGroup();
        affinityGroup2.setClusterId(clusterId);
        affinityGroup2.setName(AFFINITY_GROUP_NAME + "##");
        doReturn(affinityGroup2).when(command).getAffinityGroup();
        doReturn(new Cluster()).when(command).getCluster();
        VmStatic vmStatic = new VmStatic();
        vmStatic.setClusterId(clusterId);
        doReturn(Collections.singletonList(vmStatic)).when(vmStaticDao).getByIds(any());
        doReturn(clusterId).when(command).getClusterId();
    }

    @Test
    public void validate_noAffinityGroup_Test() {
        doReturn(null).when(command).getAffinityGroup();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
    }

    @Test
    public void validate_changeClusterIds_Test() {
        doReturn(Guid.newGuid()).when(command).getClusterId();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_CLUSTER_ID);
    }

    @Test
    public void validate_testNameChange_Test() {
        doReturn(new AffinityGroup()).when(affinityGroupDao).getByName(affinityGroup.getName());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
    }

    @Test
    public void validate_succeed_Test() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        // validateParameters is tests at {@link AddAffinityGroupCommandTest}
        verify(command, times(1)).validateParameters();
    }

    @Test
    public void excuteCommandTest() {
        command.executeCommand();
        assertEquals(AuditLogType.USER_UPDATED_AFFINITY_GROUP, command.getAuditLogTypeValue());
    }

    private AffinityGroup createAffinityGroup() {
        affinityGroup = new AffinityGroup();
        affinityGroup.setName(AFFINITY_GROUP_NAME);
        affinityGroup.setClusterId(clusterId);
        affinityGroup.setVmIds(new ArrayList<>());
        return affinityGroup;
    }
}
