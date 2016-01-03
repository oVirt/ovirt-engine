package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class AddVdsGroupCommandTest {

    private final String CLUSTER_NAME = "clusterName";
    private final String CLUSTER_DESCRIPTION = "cluster description";
    private final Guid DATA_CENTER_ID = Guid.newGuid();
    private final String CPU_NAME = "Cpu Name";
    private final int MAX_VDS_MEMORY_OVER_COMMIT = 10;
    private final boolean COUNT_THREADS_AS_CORES = true;
    private final boolean SET_TRANSPARENT_HUGE_PAGES = true;
    private final Version SET_COMPATIBILITY_VERSION = new Version("3.5");
    private final MigrateOnErrorOptions MIGRATE_ON_ERROR = MigrateOnErrorOptions.NO;
    private final String CORRELATION_ID = "C0RR3LAT10N1D";
    private final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.x86;

    //Mocks
    @Mock
    private VdsGroupDao vdsGroupDao;
    @Mock
    private Network managementNetwork;
    @Mock
    private static NetworkClusterDao networkClusterDao;

    private AddVdsGroupCommand addVdsGroupCommand;
    private CommandContext commandContext;
    private VDSGroup cluster;

    @Mock
    private BackendInternal backend;
    @Mock
    private ManagementNetworkOnClusterOperationParameters parameters;

    @Before
    public void setUp() {
        createCluster();
        createParameters();
        createCommandContext();
        createCommand();
        mockBackend();
    }

    private void createCluster() {
        cluster = new VDSGroup();
        cluster.setName(CLUSTER_NAME);
        cluster.setDescription(CLUSTER_DESCRIPTION);
        cluster.setStoragePoolId(DATA_CENTER_ID);
        cluster.setCpuName(CPU_NAME);
        cluster.setMaxVdsMemoryOverCommit(MAX_VDS_MEMORY_OVER_COMMIT);
        cluster.setCountThreadsAsCores(COUNT_THREADS_AS_CORES);
        cluster.setTransparentHugepages(SET_TRANSPARENT_HUGE_PAGES);
        cluster.setCompatibilityVersion(SET_COMPATIBILITY_VERSION);
        cluster.setMigrateOnError(MIGRATE_ON_ERROR);
        cluster.setArchitecture(ARCHITECTURE_TYPE);
    }

    private void createParameters() {
        parameters = new ManagementNetworkOnClusterOperationParameters(cluster);
        parameters.setCorrelationId(CORRELATION_ID);
    }

    private void createCommandContext() {
        commandContext = new CommandContext(new EngineContext().withSessionId(parameters.getSessionId())).withExecutionContext(new ExecutionContext());
    }

    private void createCommand() {
        AddVdsGroupCommand addClusterCommandInstance = new AddVdsGroupCommand(parameters, commandContext) {
            {
                networkClusterDao = AddVdsGroupCommandTest.networkClusterDao;
            }
        };

        addVdsGroupCommand = spy(addClusterCommandInstance);
        doReturn(ARCHITECTURE_TYPE).when(addVdsGroupCommand).getArchitecture();
        doReturn(backend).when(addVdsGroupCommand).getBackend();
        doReturn(vdsGroupDao).when(addVdsGroupCommand).getVdsGroupDao();
        doReturn(managementNetwork).when(addVdsGroupCommand).getManagementNetwork();

    }

    private void mockBackend() {
        doReturn(backend).when(addVdsGroupCommand).getBackend();

        VdcReturnValueBase addClusterReturnValue = mock(VdcReturnValueBase.class);
        when(addClusterReturnValue.getSucceeded()).thenReturn(true);

        when(backend.runAction(any(VdcActionType.class), any(CpuProfileParameters.class))).thenReturn(addClusterReturnValue);
    }

    @Test
    public void executeCommandTest() {
        addVdsGroupCommand.executeCommand();

        verify(vdsGroupDao).save(cluster);
        verify(backend).runAction(eq(VdcActionType.AddCpuProfile), any(CpuProfileParameters.class));

        assertTrue(addVdsGroupCommand.getReturnValue().getSucceeded());
    }

    @Test
    public void getAuditLogTest() {
        addVdsGroupCommand.setSucceeded(true);
        assertEquals(AuditLogType.USER_ADD_VDS_GROUP, addVdsGroupCommand.getAuditLogTypeValue());

        addVdsGroupCommand.setSucceeded(false);
        assertEquals(AuditLogType.USER_ADD_VDS_GROUP_FAILED, addVdsGroupCommand.getAuditLogTypeValue());
    }

    @Test
    public void getPermissionCheckSubjectsTest() {
        List<PermissionSubject> permissions = addVdsGroupCommand.getPermissionCheckSubjects();

        assertEquals(permissions.size(), 1);
        PermissionSubject permissionSubject = permissions.get(0);

        assertEquals(cluster.getStoragePoolId(), permissionSubject.getObjectId());
        assertEquals(VdcObjectType.StoragePool, permissionSubject.getObjectType());

        /**
         * @TODO : Once there is a way to get the ActionType properly from the permissionSubject add the following test:
         * assertEquals(ActionGroup.CREATE_CLUSTER, permissionSubject.getActionGroup());
         *
         * This test doesn't work because it relies on the class's name which mockito changes.
         */
    }
}
