package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServicesListVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterClusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;

@RunWith(MockitoJUnitRunner.class)
public class GlusterServiceSyncJobTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid SERVER1_ID = Guid.newGuid();
    private static final Guid SERVER2_ID = Guid.newGuid();
    private static final Guid SERVER3_ID = Guid.newGuid();
    private static final Guid SERVICE1_ID = Guid.newGuid();
    private static final Guid SERVICE2_ID = Guid.newGuid();
    private static final Guid SERVICE3_ID = Guid.newGuid();
    private static final String SERVICE1_NAME = "service1";
    private static final String SERVICE2_NAME = "service2";
    private static final String SERVICE3_NAME = "service3";
    private List<GlusterClusterService> existingClusterServices;
    private Map<String, GlusterService> serviceNameMap;
    private GlusterServiceSyncJob syncJob;
    private List<VDS> upServers;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 5),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 500),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10),
            mockConfig(ConfigValues.GlusterServicesEnabled, Version.getLast(), true));

    @Mock
    private GlusterServiceDao serviceDao;

    @Mock
    private GlusterServerServiceDao serverServiceDao;

    @Mock
    private GlusterClusterServiceDao clusterServiceDao;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @Before
    public void setUp() {
        createObjects();
        setupCommonMock();
    }

    private void createObjects() {
        upServers = createUpServers();
        serviceNameMap = createServiceNameMap();
        existingClusterServices = createClusterServices();
    }

    private void setupCommonMock() {
        syncJob = Mockito.spy(GlusterServiceSyncJob.getInstance());
        syncJob.setLogUtil(logUtil);

        doReturn(serviceDao).when(syncJob).getGlusterServiceDao();
        doReturn(serverServiceDao).when(syncJob).getGlusterServerServiceDao();
        doReturn(clusterServiceDao).when(syncJob).getGlusterClusterServiceDao();
        doReturn(clusterDao).when(syncJob).getClusterDao();
        doReturn(glusterUtil).when(syncJob).getGlusterUtil();
        doReturn(serviceNameMap).when(syncJob).getServiceNameMap();

        doReturn(upServers).when(glusterUtil).getAllUpServers(CLUSTER_ID);
        doReturn(Collections.singletonList(createCluster())).when(clusterDao).getAll();
        doReturn(createServerServices(SERVER1_ID, GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerId(SERVER1_ID);
        doReturn(createServerServices(SERVER2_ID, GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerId(SERVER2_ID);
        doReturn(createServerServices(SERVER3_ID, GlusterServiceStatus.RUNNING)).when(serverServiceDao)
                .getByServerId(SERVER3_ID);
        doReturn(existingClusterServices).when(clusterServiceDao).getByClusterId(CLUSTER_ID);
        doReturn(createServers()).when(glusterUtil).getAllUpServers(CLUSTER_ID);

        doNothing().when(syncJob).acquireLock(SERVER1_ID);
        doNothing().when(syncJob).releaseLock(SERVER1_ID);
        doNothing().when(syncJob).acquireLock(SERVER2_ID);
        doNothing().when(syncJob).releaseLock(SERVER2_ID);
        doNothing().when(syncJob).acquireLock(SERVER3_ID);
        doNothing().when(syncJob).releaseLock(SERVER3_ID);
    }

    private List<VDS> createServers() {
        List<VDS> serverList = new ArrayList<>();
        VDS server1 = createUpServer(SERVER1_ID);
        server1.setStatus(VDSStatus.Up);
        serverList.add(server1);
        VDS server2 = createUpServer(SERVER2_ID);
        server2.setStatus(VDSStatus.Up);
        serverList.add(server2);
        VDS server3 = createUpServer(SERVER3_ID);
        server3.setStatus(VDSStatus.Up);
        serverList.add(server3);

        return serverList;
    }

    private Map<String, GlusterService> createServiceNameMap() {
        Map<String, GlusterService> map = new HashMap<>();
        map.put(SERVICE1_NAME, createGlusterService(SERVICE1_ID, SERVICE1_NAME, ServiceType.GLUSTER));
        map.put(SERVICE2_NAME, createGlusterService(SERVICE2_ID, SERVICE2_NAME, ServiceType.GLUSTER_SWIFT));
        map.put(SERVICE3_NAME, createGlusterService(SERVICE3_ID, SERVICE3_NAME, ServiceType.GLUSTER_SWIFT));
        return map;
    }

    private GlusterService createGlusterService(Guid serviceId, String serviceName, ServiceType type) {
        GlusterService service = new GlusterService();
        service.setId(serviceId);
        service.setServiceName(serviceName);
        service.setServiceType(type);
        return service;
    }

    private Cluster createCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setGlusterService(true);
        cluster.setCompatibilityVersion(Version.getLast());
        return cluster;
    }

    private List<GlusterClusterService> createClusterServices() {
        List<GlusterClusterService> services = new ArrayList<>();

        services.add(createClusterService(CLUSTER_ID, ServiceType.GLUSTER_SWIFT, GlusterServiceStatus.RUNNING));
        services.add(createClusterService(CLUSTER_ID, ServiceType.GLUSTER, GlusterServiceStatus.RUNNING));

        return services;
    }

    private GlusterClusterService createClusterService(Guid clusterId,
            ServiceType serviceType,
            GlusterServiceStatus status) {
        GlusterClusterService service = new GlusterClusterService();
        service.setClusterId(clusterId);
        service.setServiceType(serviceType);
        service.setStatus(status);
        return service;
    }

    private List<GlusterServerService> createServerServices(Guid serverId, GlusterServiceStatus status) {
        List<GlusterServerService> services = new ArrayList<>();

        services.add(createServerService(serverId, SERVICE1_ID, SERVICE1_NAME, status));
        services.add(createServerService(serverId, SERVICE2_ID, SERVICE2_NAME, status));
        services.add(createServerService(serverId, SERVICE3_ID, SERVICE3_NAME, status));

        return services;
    }

    private List<GlusterServerService> createServerServicesWithMixedStatus(Guid serverId) {
        List<GlusterServerService> services = new ArrayList<>();

        services.add(createServerService(serverId, SERVICE1_ID, SERVICE1_NAME, GlusterServiceStatus.RUNNING));
        services.add(createServerService(serverId, SERVICE2_ID, SERVICE2_NAME, GlusterServiceStatus.STOPPED));
        services.add(createServerService(serverId, SERVICE3_ID, SERVICE3_NAME, GlusterServiceStatus.ERROR));

        return services;
    }

    private GlusterServerService createServerService(Guid serverId,
            Guid serviceId,
            String serviceName,
            GlusterServiceStatus status) {
        GlusterServerService service = new GlusterServerService();
        service.setId(Guid.newGuid());
        service.setServerId(serverId);
        service.setServiceId(serviceId);
        service.setStatus(status);
        service.setServiceName(serviceName);
        return service;
    }

    private List<VDS> createUpServers() {
        List<VDS> servers = new ArrayList<>();
        servers.add(createUpServer(SERVER1_ID));
        servers.add(createUpServer(SERVER2_ID));
        servers.add(createUpServer(SERVER3_ID));
        return servers;
    }

    private VDS createUpServer(Guid serverId) {
        VDS server = new VDS();
        server.setId(serverId);
        return server;
    }

    @Test
    public void testRefreshGlusterServicesNoChanges() throws Exception {
        mockNoChanges();
        syncJob.refreshGlusterServices();
        verifyNoChanges();
    }

    @Test
    public void testRefreshGlusterServicesWithChanges() throws Exception {
        mockWithChanges();
        syncJob.refreshGlusterServices();
        verifyWithChanges();
    }

    @Test
    public void testRefreshGlusterServicesWhenVdsmVerbFails() {
        mockVdsmFailureOnServer1AndNoChangesOnOthers();
        syncJob.refreshGlusterServices();
        verifyNoChangesWithFailureOnServer1();
    }

    private void verifyCommonCalls() {
        // all clusters fetched from db
        verify(clusterDao, times(1)).getAll();

        // get all servers of the cluster
        verify(glusterUtil, times(1)).getAllUpServers(CLUSTER_ID);

        // Fetch existing services from server1
        verify(serverServiceDao, times(1)).getByServerId(SERVER1_ID);

        // Fetch existing services from server2
        verify(serverServiceDao, times(1)).getByServerId(SERVER2_ID);

        // Fetch existing services from server3
        verify(serverServiceDao, times(1)).getByServerId(SERVER3_ID);

        // Fetch services statuses from all three servers
        verify(syncJob, times(3)).runVdsCommand(eq(VDSCommandType.GlusterServicesList),
                any(GlusterServicesListVDSParameters.class));

    }

    @SuppressWarnings("unchecked")
    private void verifyNoChanges() {
        verifyCommonCalls();

        // Since there are no changes in any service status, there should be no database update
        verify(serverServiceDao, never()).updateAll(any(List.class));
        verify(clusterServiceDao, never()).update(any(GlusterClusterService.class));
    }

    private void verifyNoChangesWithFailureOnServer1() {
        verifyCommonCalls();

        // One update on serverServiceDao to update statuses of all services of server1
        verify(serverServiceDao, times(1)).updateAll(argThat(isCollectionOfServicesOfServer1WithStatusUnknown()));

        // two updates on clusterServiceDao to update status of each service type to MIXED
        verify(clusterServiceDao, times(2)).update(argThat(isClusterServiceWithMixedStatus()));
    }

    private void verifyWithChanges() {
        verifyCommonCalls();

        // service statuses get updated on server2 and server3
        verify(serverServiceDao, times(2)).updateAll(argThat(isListOfServersWithChangedStatus()));

        // two updates on clusterServiceDao to update status of each service type to MIXED
        verify(clusterServiceDao, times(2)).update(argThat(isClusterServiceWithMixedStatus()));
    }

    private Matcher<GlusterClusterService> isClusterServiceWithMixedStatus() {
        return new ArgumentMatcher<GlusterClusterService>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterClusterService)) {
                    return false;
                }

                return ((GlusterClusterService) argument).getStatus() == GlusterServiceStatus.MIXED;
            }
        };
    }

    private Matcher<Collection<GlusterServerService>> isCollectionOfServicesOfServer1WithStatusUnknown() {
        return new ArgumentMatcher<Collection<GlusterServerService>>() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof Collection)) {
                    return false;
                }

                Collection<GlusterServerService> serverServices = (Collection<GlusterServerService>) argument;

                for (GlusterServerService service : serverServices) {
                    // Status of all services from server1 should change to UNKNOWN.
                    // Nothing else should change
                    if (!(service.getServerId().equals(SERVER1_ID) && service.getStatus() == GlusterServiceStatus.UNKNOWN)) {
                        return false;
                    }
                }

                return true;
            }
        };

    }

    private Matcher<List<GlusterServerService>> isListOfServersWithChangedStatus() {
        return new ArgumentMatcher<List<GlusterServerService>>() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof List)) {
                    return false;
                }

                List<GlusterServerService> serverServices = (List<GlusterServerService>) argument;

                for (GlusterServerService service : serverServices) {
                    // server1 has no services with changed status
                    if (service.getServerId().equals(SERVER1_ID)) {
                        return false;
                    }

                    // on server2, only service2 and service3 have changed status.
                    if (service.getServerId().equals(SERVER2_ID)) {
                        if (!(service.getServiceId().equals(SERVICE2_ID) || service.getServiceId().equals(SERVICE3_ID))) {
                            return false;
                        }
                    }

                    // on server3, all services have different status. so no checks required.
                }

                return true;
            }
        };
    }

    private void mockNoChanges() {
        mockStatusForAllServicesOfServer(SERVER1_ID, GlusterServiceStatus.RUNNING);
        mockStatusForAllServicesOfServer(SERVER2_ID, GlusterServiceStatus.RUNNING);
        mockStatusForAllServicesOfServer(SERVER3_ID, GlusterServiceStatus.RUNNING);
    }

    private void mockWithChanges() {
        // no changes (all RUNNING) on server1 (all services in RUNNING status)
        mockStatusForAllServicesOfServer(SERVER1_ID, GlusterServiceStatus.RUNNING);

        // mixed status on server2
        List<GlusterServerService> server2Services = createServerServicesWithMixedStatus(SERVER2_ID);
        doReturn(createVDSReturnValue(SERVER2_ID, server2Services)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GlusterServicesList),
                        argThat(isServer(SERVER2_ID)));

        // all stopped on server3
        mockStatusForAllServicesOfServer(SERVER3_ID, GlusterServiceStatus.STOPPED);
    }

    private void mockVdsmFailureOnServer1AndNoChangesOnOthers() {
        doReturn(createVDSReturnValueForFailure()).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GlusterServicesList),
                        argThat(isServer(SERVER1_ID)));
        mockStatusForAllServicesOfServer(SERVER2_ID, GlusterServiceStatus.RUNNING);
        mockStatusForAllServicesOfServer(SERVER3_ID, GlusterServiceStatus.RUNNING);
    }

    private void mockStatusForAllServicesOfServer(Guid serverId, GlusterServiceStatus status) {
        doReturn(createVDSReturnValue(serverId, status)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GlusterServicesList),
                        argThat(isServer(serverId)));
    }

    private Matcher<GlusterServicesListVDSParameters> isServer(final Guid serverId) {
        return new ArgumentMatcher<GlusterServicesListVDSParameters>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterServicesListVDSParameters)) {
                    return false;
                }

                return ((GlusterServicesListVDSParameters) argument).getVdsId().equals(serverId);
            }
        };
    }

    private VDSReturnValue createVDSReturnValue(Guid serverId, List<GlusterServerService> serverServices) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(true);
        ret.setReturnValue(serverServices);
        return ret;
    }

    private VDSReturnValue createVDSReturnValue(Guid serverId, GlusterServiceStatus status) {
        return createVDSReturnValue(serverId, createServerServices(serverId, status));
    }

    private VDSReturnValue createVDSReturnValueForFailure() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(false);
        ret.setVdsError(new VDSError(EngineError.GlusterServicesActionFailed, "VDSM Error"));
        return ret;
    }
}
