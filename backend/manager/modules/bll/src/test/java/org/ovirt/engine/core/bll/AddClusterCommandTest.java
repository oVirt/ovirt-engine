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
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class AddClusterCommandTest extends BaseCommandTest {

    private final String CLUSTER_NAME = "clusterName";
    private final String CLUSTER_DESCRIPTION = "cluster description";
    private final Guid DATA_CENTER_ID = Guid.newGuid();
    private final String CPU_NAME = "Cpu Name";
    private final int MAX_VDS_MEMORY_OVER_COMMIT = 10;
    private final boolean COUNT_THREADS_AS_CORES = true;
    private final boolean SET_TRANSPARENT_HUGE_PAGES = true;
    private static final Version SET_COMPATIBILITY_VERSION = new Version("3.5");
    private final MigrateOnErrorOptions MIGRATE_ON_ERROR = MigrateOnErrorOptions.NO;
    private final String CORRELATION_ID = "C0RR3LAT10N1D";
    private final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.x86;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.OvsSupported, SET_COMPATIBILITY_VERSION, false));

    //Mocks
    @Mock
    private static CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private Network managementNetwork;
    @Mock
    private static NetworkClusterDao networkClusterDao;

    private AddClusterCommand addClusterCommand;
    private CommandContext commandContext;
    private Cluster cluster;

    @Mock
    private BackendInternal backend;
    @Mock
    private ManagementNetworkOnClusterOperationParameters parameters;

    @Before
    public void setUp() {
        createCluster();
        createParameters();
        injectMocks();
        createCommandContext();
        createCommand();
        mockBackend();
    }

    private void createCluster() {
        cluster = new Cluster();
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

    private void injectMocks() {
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
        injectorRule.bind(ClusterDao.class, clusterDao);
        injectorRule.bind(Network.class, managementNetwork);
        injectorRule.bind(NetworkClusterDao.class, networkClusterDao);
    }

    private void createCommandContext() {
        commandContext = CommandContext.createContext(parameters.getSessionId());
    }

    private void createCommand() {
        AddClusterCommand addClusterCommandInstance = new AddClusterCommand(parameters, commandContext) {
            {
                cpuFlagsManagerHandler = AddClusterCommandTest.cpuFlagsManagerHandler;
                networkClusterDao = AddClusterCommandTest.networkClusterDao;
            }
        };

        addClusterCommand = spy(addClusterCommandInstance);
        doReturn(ARCHITECTURE_TYPE).when(addClusterCommand).getArchitecture();
        doReturn(backend).when(addClusterCommand).getBackend();
        doReturn(clusterDao).when(addClusterCommand).getClusterDao();
        doReturn(managementNetwork).when(addClusterCommand).getManagementNetwork();

    }

    private void mockBackend() {
        doReturn(backend).when(addClusterCommand).getBackend();

        VdcReturnValueBase addClusterReturnValue = mock(VdcReturnValueBase.class);
        when(addClusterReturnValue.getSucceeded()).thenReturn(true);

        when(backend.runAction(any(VdcActionType.class), any(CpuProfileParameters.class))).thenReturn(addClusterReturnValue);
    }

    @Test
    public void executeCommandTest() {
        addClusterCommand.executeCommand();

        verify(clusterDao).save(cluster);
        verify(backend).runAction(eq(VdcActionType.AddCpuProfile), any(CpuProfileParameters.class));

        assertTrue(addClusterCommand.getReturnValue().getSucceeded());
    }

    @Test
    public void getAuditLogTest() {
        addClusterCommand.setSucceeded(true);
        assertEquals(AuditLogType.USER_ADD_CLUSTER, addClusterCommand.getAuditLogTypeValue());

        addClusterCommand.setSucceeded(false);
        assertEquals(AuditLogType.USER_ADD_CLUSTER_FAILED, addClusterCommand.getAuditLogTypeValue());
    }

    @Test
    public void getPermissionCheckSubjectsTest() {
        List<PermissionSubject> permissions = addClusterCommand.getPermissionCheckSubjects();

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
