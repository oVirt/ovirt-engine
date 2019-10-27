package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AddClusterCommandTest extends BaseCommandTest {

    private static final String CLUSTER_NAME = "clusterName";
    private static final String CLUSTER_DESCRIPTION = "cluster description";
    private static final Guid DATA_CENTER_ID = Guid.newGuid();
    private static final String CPU_NAME = "Cpu Name";
    private static final int MAX_VDS_MEMORY_OVER_COMMIT = 10;
    private static final boolean SMT_DISABLED = false;
    private static final boolean COUNT_THREADS_AS_CORES = true;
    private static final boolean SET_TRANSPARENT_HUGE_PAGES = true;
    private static final Version SET_COMPATIBILITY_VERSION = new Version("3.5");
    private static final MigrateOnErrorOptions MIGRATE_ON_ERROR = MigrateOnErrorOptions.NO;
    private static final String CORRELATION_ID = "C0RR3LAT10N1D";
    private static final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.x86;

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

    @Mock
    private ClusterCpuFlagsManager clusterCpuFlagsManager;

    private Cluster cluster = createCluster();
    private ClusterOperationParameters parameters = createParameters(cluster);
    private CommandContext commandContext = CommandContext.createContext(parameters.getSessionId());

    @InjectMocks
    private AddClusterCommand<ClusterOperationParameters> addClusterCommand =
            new AddClusterCommand<>(parameters, commandContext);

    @Mock
    private BackendInternal backend;

    @BeforeEach
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
        cluster.setSmtDisabled(SMT_DISABLED);
        cluster.setCountThreadsAsCores(COUNT_THREADS_AS_CORES);
        cluster.setTransparentHugepages(SET_TRANSPARENT_HUGE_PAGES);
        cluster.setCompatibilityVersion(SET_COMPATIBILITY_VERSION);
        cluster.setMigrateOnError(MIGRATE_ON_ERROR);
        cluster.setArchitecture(ARCHITECTURE_TYPE);
        cluster.setLogMaxMemoryUsedThreshold(95);
        cluster.setLogMaxMemoryUsedThresholdType(LogMaxMemoryUsedThresholdType.PERCENTAGE);
        return cluster;
    }

    private static ClusterOperationParameters createParameters(Cluster cluster) {
        ClusterOperationParameters parameters = new ClusterOperationParameters(cluster);
        parameters.setCorrelationId(CORRELATION_ID);
        return parameters;
    }

    private void mockDao() {
        when(macPoolDao.getDefaultPool()).thenReturn(new MacPool());
    }

    private void mockBackend() {
        when(backend.runInternalAction(any(), any(), any())).thenReturn(new ActionReturnValue());
    }

    @Test
    public void executeCommandTest() {
        addClusterCommand.executeCommand();

        verify(clusterDao).save(cluster);
        verify(backend).runInternalAction(eq(ActionType.AddCpuProfile), any(), any());

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
