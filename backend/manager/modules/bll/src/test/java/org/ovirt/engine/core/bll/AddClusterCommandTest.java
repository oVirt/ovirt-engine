package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class AddClusterCommandTest extends BaseCommandTest {

    private static final String CLUSTER_NAME = "clusterName";
    private static final String CLUSTER_DESCRIPTION = "cluster description";
    private static final Guid DATA_CENTER_ID = Guid.newGuid();
    private static final String CPU_NAME = "Cpu Name";
    private static final int MAX_VDS_MEMORY_OVER_COMMIT = 10;
    private static final boolean COUNT_THREADS_AS_CORES = true;
    private static final boolean SET_TRANSPARENT_HUGE_PAGES = true;
    private static final Version SET_COMPATIBILITY_VERSION = new Version("3.5");
    private static final MigrateOnErrorOptions MIGRATE_ON_ERROR = MigrateOnErrorOptions.NO;
    private static final String CORRELATION_ID = "C0RR3LAT10N1D";
    private static final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.x86;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    //Mocks
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;
    @Mock
    private ClusterDao clusterDao;

    @Mock
    private MacPoolDao macPoolDao;

    @Mock
    private Network managementNetwork;

    @Mock
    private NetworkClusterDao networkClusterDao;

    private Cluster cluster = createCluster();
    private ManagementNetworkOnClusterOperationParameters parameters = createParameters(cluster);
    private CommandContext commandContext = CommandContext.createContext(parameters.getSessionId());

    @InjectMocks
    private AddClusterCommand<ManagementNetworkOnClusterOperationParameters> addClusterCommand =
            new AddClusterCommand<>(parameters, commandContext);

    @Mock
    private BackendInternal backend;

    @Before
    public void setUp() {
        mockDao();
        mockBackend();
    }

    private static Cluster createCluster() {
        Cluster cluster = new Cluster();
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
        return cluster;
    }

    private static ManagementNetworkOnClusterOperationParameters createParameters(Cluster cluster) {
        ManagementNetworkOnClusterOperationParameters parameters =
                new ManagementNetworkOnClusterOperationParameters(cluster);
        parameters.setCorrelationId(CORRELATION_ID);
        return parameters;
    }

    private void mockDao() {
        when(macPoolDao.getDefaultPool()).thenReturn(new MacPool());
    }

    private void mockBackend() {
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

        assertEquals(2, permissions.size());

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
