package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpStatisticalEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostResourceTest
        extends AbstractBackendSubResourceTest<Host, VDS, BackendHostResource> {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.OrganizationName, "oVirt"));
    }

    private static final StorageType ISCSI_STORAGE_TYPE = StorageType.ISCSI;
    private static final int ISCSI_PORT_INT = 3260;
    private static final String ISCSI_USER_PASS = "123456789012";
    private static final String ISCSI_USER_NAME = "ori";
    private static final String ISCSI_PORT_STRING = "3260";
    private static final String ISCSI_IQN = "iqn.1986-03.com.sun:02:ori01";
    private static final String ISCSI_SERVER_ADDRESS = "shual1.eng.lab";
    protected static final String ROOT_PASSWORD = "s3CR3t";

    public BackendHostResourceTest() {
        super(new BackendHostResource(GUIDS[0].toString(), new BackendHostsResource()));
    }

    @Override
    protected void init() {
        super.init();
        setUpParentMock();
    }

    private void setUpParentMock() {
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendHostResource("foo", new BackendHostsResource())));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateWithClusterId() {
        setUpGetEntityExpectations(3);
        setUriInfo(setUpActionExpectations(ActionType.ChangeVDSCluster,
                ChangeVDSClusterParameters.class,
                new String[] { "ClusterId", "VdsId" },
                new Object[] { GUIDS[1],  GUIDS[0]},
                true,
                true,
                new ActionReturnValue(),
                false));

        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                UpdateVdsActionParameters.class,
                new String[] { "RootPassword" },
                new Object[] { ROOT_PASSWORD },
                true,
                true));

        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        Host host = getModel(0);
        host.setCluster(cluster);
        verifyModel(resource.update(host), 0);
    }

    @Test
    public void testUpdateWithClusterName() {
        String clusterName = "Default";
        setUpGetEntityExpectations(3);

        setUpEntityQueryExpectations(QueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { clusterName },
                getCluster(clusterName, GUIDS[1]));

        setUriInfo(setUpActionExpectations(ActionType.ChangeVDSCluster,
                ChangeVDSClusterParameters.class,
                new String[] { "ClusterId", "VdsId" },
                new Object[] { GUIDS[1], GUIDS[0] },
                true,
                true,
                new ActionReturnValue(),
                false));

        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                UpdateVdsActionParameters.class,
                new String[] { "RootPassword" },
                new Object[] { ROOT_PASSWORD },
                true,
                true));

        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setName(clusterName);
        Host host = getModel(0);
        host.setCluster(cluster);
        verifyModel(resource.update(host), 0);
    }

    private Cluster getCluster(String name, Guid id) {
        Cluster cluster = mock(Cluster.class);
        when(cluster.getId()).thenReturn(id);
        when(cluster.getName()).thenReturn(name);
        return cluster;
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityWithNoCertificateInfoExpectations();
        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityWithNoCertificateInfoExpectations();

        Host model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testActivate() {
        setUriInfo(setUpActionExpectations(ActionType.ActivateVds,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.activate(new Action()));
    }

    @Test
    public void testActivateAsyncPending() {
        doTestActivateAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testActivateAsyncInProgress() {
        doTestActivateAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testActivateAsyncFinished() {
        doTestActivateAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestActivateAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) {
        setUriInfo(setUpActionExpectations(ActionType.ActivateVds,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] },
                                           asList(GUIDS[1]),
                                           asList(new AsyncTaskStatus(asyncStatus))));

        Response response = resource.activate(new Action());
        verifyActionResponse(response, "hosts/" + GUIDS[0], true, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus());

    }

    @Test
    public void testApprove() {
        setUriInfo(setUpActionExpectations(ActionType.ApproveVds,
                                           ApproveVdsParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.approve(new Action()));
    }

    @Test
    public void testApproveChangingCluster() {
        setUpGetEntityExpectations(4);

        setUriInfo(setUpActionExpectations(ActionType.ChangeVDSCluster,
                                           ChangeVDSClusterParameters.class,
                                           new String[] { "ClusterId", "VdsId" },
                                           new Object[] { GUIDS[0],  GUIDS[0]},
                                           true,
                                           true,
                                           new ActionReturnValue(),
                                           false));

        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { },
                                           new Object[] { },
                                           true,
                                           true));

        setUriInfo(setUpActionExpectations(ActionType.ApproveVds,
                                           ApproveVdsParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[0].toString());
        Action action = new Action();
        action.setCluster(cluster);
        verifyActionResponse(resource.approve(action));
    }

    @Test
    public void testIscsiLogin() {
        setUriInfo(setUpActionExpectations(ActionType.ConnectStorageToVds,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "VdsId",
                                                          "StorageServerConnection.Connection",
                                                          "StorageServerConnection.Portal",
                                                          "StorageServerConnection.Iqn",
                                                          "StorageServerConnection.Port",
                                                          "StorageServerConnection.StorageType",
                                                          "StorageServerConnection.UserName",
                                                          "StorageServerConnection.Password" },
                                           new Object[] { GUIDS[0],
                                                          ISCSI_SERVER_ADDRESS,
                                                          StorageServerConnections.DEFAULT_TPGT, //TODO: right now hard-coded, but this should change when VDSM and Backend support portal
                                                          ISCSI_IQN,
                                                          ISCSI_PORT_STRING,
                                                          ISCSI_STORAGE_TYPE,
                                                          ISCSI_USER_NAME,
                                                          ISCSI_USER_PASS }));

        Action action = new Action();
        IscsiDetails iscsiDetails = new IscsiDetails();
        iscsiDetails.setAddress(ISCSI_SERVER_ADDRESS);
        iscsiDetails.setPort(ISCSI_PORT_INT);
        iscsiDetails.setTarget(ISCSI_IQN);
        iscsiDetails.setUsername(ISCSI_USER_NAME);
        iscsiDetails.setPassword(ISCSI_USER_PASS);
        action.setIscsi(iscsiDetails);
        verifyActionResponse(resource.iscsiLogin(action));
    }

    @Test
    public void testIscsiDiscover() {
        IscsiDetails iscsiDetails = new IscsiDetails();
        iscsiDetails.setAddress(ISCSI_SERVER_ADDRESS);
        iscsiDetails.setPort(ISCSI_PORT_INT);
        iscsiDetails.setUsername(ISCSI_USER_NAME);
        iscsiDetails.setPassword(ISCSI_USER_PASS);

        Action action = new Action();
        action.setIscsi(iscsiDetails);

        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(true);

        when(backend.runQuery(eq(QueryType.DiscoverSendTargets),
                                eqParams(DiscoverSendTargetsQueryParameters.class,
                                              addSession("VdsId", "Connection.Connection", "Connection.Port", "Connection.UserName", "Connection.Password"),
                                              addSession(GUIDS[0], ISCSI_SERVER_ADDRESS, ISCSI_PORT_STRING, ISCSI_USER_NAME, ISCSI_USER_PASS)
                                              ))).thenReturn(queryResult);
        enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(QueryType.DiscoverSendTargets),
                eqParams(DiscoverSendTargetsQueryParameters.class,
                        addSession("VdsId", "Connection.Connection", "Connection.Port", "Connection.UserName", "Connection.Password"),
                        addSession(GUIDS[0], ISCSI_SERVER_ADDRESS, ISCSI_PORT_STRING, ISCSI_USER_NAME, ISCSI_USER_PASS)
                )));
        resource.iscsiDiscover(action);
    }

    @Test
    public void testDeactivate() {
        setUriInfo(setUpActionExpectations(ActionType.MaintenanceNumberOfVdss,
                                           MaintenanceNumberOfVdssParameters.class,
                                           new String[] { "VdsIdList" },
                                           new Object[] { asList(GUIDS[0]) }));

        verifyActionResponse(resource.deactivate(new Action()));
    }

    @Test
    public void testForceSelect() {
        setUriInfo(setUpActionExpectations(ActionType.ForceSelectSPM,
                                           ForceSelectSPMParameters.class,
                                           new String[] { "PreferredSPMId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.forceSelectSpm(new Action()));
    }

    @Test
    public void testInstall() {
        setUpGetEntityWithNoCertificateInfoExpectations();

        setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { NAMES[2] }));

        Action action = new Action();
        action.setRootPassword(NAMES[2]);
        verifyActionResponse(resource.install(action));
    }

    @Test
    public void testUpgrade() {
        setUriInfo(setUpActionExpectations(ActionType.UpgradeHost,
                                           UpgradeHostParameters.class,
                                           new String[] { "VdsId", "oVirtIsoFile" },
                                           new Object[] { GUIDS[0], NAMES[0] }));

        Action action = new Action();
        action.setImage(NAMES[0]);
        verifyActionResponse(resource.upgrade(action));
    }

    @Test
    public void testCommitNetConfig() {
        setUriInfo(setUpActionExpectations(ActionType.CommitNetworkChanges,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.commitNetConfig(new Action()));
    }

    @Test
    public void testManualFence() {
        setUpGetEntityWithNoCertificateInfoExpectations();

        setUriInfo(setUpActionExpectations(ActionType.FenceVdsManualy,
                                           FenceVdsManualyParameters.class,
                                           new String[] { "VdsId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[1] }));

        Action action = new Action();
        action.setFenceType(FenceType.MANUAL.value());

        verifyActionResponse(resource.fence(action));
    }

    @Test
    public void testRestartFence() {
        Action action = new Action();
        action.setFenceType(FenceType.RESTART.value());
        action.setMaintenanceAfterRestart(true);

        doTestFence(ActionType.RestartVds,
                    action);
    }

    @Test
    public void testStartFence() {
        doTestFence(FenceType.START,
                    ActionType.StartVds);
    }

    @Test
    public void testStopFence() {
        doTestFence(FenceType.STOP,
                    ActionType.StopVds);
    }

    public void doTestFence(FenceType fenceType,
                            ActionType actionType) {
        Action action = new Action();
        action.setFenceType(fenceType.value());
        action.setMaintenanceAfterRestart(true);

        doTestFence(actionType, action);
    }

    public void doTestFence(ActionType actionType,
                            Action action) {
        setUriInfo(setUpActionExpectations(actionType,
                                           FenceVdsActionParameters.class,
                                           new String[] { "VdsId"},
                                           new Object[] { GUIDS[0]}));

        verifyActionResponse(resource.fence(action));
    }

    @Test
    public void testIncompleteFence() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.fence(new Action())),
                "Action", "fence", "fenceType");
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        VDS entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Host, VDS> statisticsResource =
            (BackendStatisticsResource<Host, VDS>)resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    @Test
    public void testFenceStatus() {
        FenceOperationResult retVal = new FenceOperationResult(FenceOperationResult.Status.SUCCESS, PowerStatus.ON);
        setUpEntityQueryExpectations(QueryType.GetVdsFenceStatus,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                retVal);
        Action action = new Action();
        action.setFenceType(FenceType.STATUS.value());
        verifyActionResponse(resource.fence(action));
        PowerManagementStatus status = action.getPowerManagement().getStatus();
        assertEquals(PowerManagementStatus.ON, status);
    }

    @Test
    public void testFenceStatusFailure() {
        FenceOperationResult retVal =
                new FenceOperationResult(FenceOperationResult.Status.ERROR, PowerStatus.UNKNOWN, "some_error");
        setUpEntityQueryExpectations(QueryType.GetVdsFenceStatus,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                retVal);
        Action action = new Action();
        action.setFenceType(FenceType.STATUS.value());
        Response response = resource.fence(action);
        Action actionReturned = (Action)response.getEntity();
        assertEquals(actionReturned.getStatus(), CreationStatus.FAILED.value());
        assertNotNull(actionReturned.getFault());
        assertEquals("some_error", actionReturned.getFault().getReason());
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVds,
                RemoveVdsParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveForced() {
        setUpGetEntityExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveVds,
            RemoveVdsParameters.class,
            new String[] { "VdsId", "ForceAction" },
            new Object[] { GUIDS[0], Boolean.TRUE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendHostResource.FORCE, Boolean.TRUE.toString());
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForcedIncomplete() {
        setUpGetEntityExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveVds,
            RemoveVdsParameters.class,
            new String[] { "VdsId", "ForceAction" },
            new Object[] { GUIDS[0], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendHostResource.FORCE, Boolean.FALSE.toString());
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        setUpGetEntityExpectations(1);
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        setUpGetEntityExpectations(1);
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVds,
                RemoveVdsParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    @Test
    public void testEnrollCertificate() {
        setUriInfo(setUpActionExpectations(ActionType.HostEnrollCertificate,
                VdsActionParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.enrollCertificate(new Action()));
    }

    protected VDS setUpStatisticalExpectations() {
        VdsStatistics stats = mock(VdsStatistics.class);
        VDS entity = mock(VDS.class);
        setUpStatisticalEntityExpectations(entity, stats);
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, entity);
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Host, VDS> query, VDS entity) throws Exception {
        assertEquals(Host.class, query.getParentType());
        assertSame(entity, query.resolve(GUIDS[0]));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"memory.total", "memory.used", "memory.free", "memory.shared",
                        "swap.total", "swap.free", "swap.used", "ksm.cpu.current",
                        "cpu.current.user", "cpu.current.system",
                                       "cpu.current.idle", "cpu.load.avg.5m", "boot.time"},
                new BigDecimal[] { asDec(5120 * Mb), asDec(1024 * Mb), asDec(4096 * Mb), asDec(38 * Mb), asDec(30 * Mb),
                        asDec(25 * Mb), asDec(5 * Mb), asDec(40), asDec(45), asDec(50), asDec(55),
                        new BigDecimal(0.0060, new MathContext(2)), asDec(0) });
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetHost());
        assertEquals(GUIDS[0].toString(), adopted.getHost().getId());
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "hosts/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, VDS entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVdsByVdsId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations() {
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, getEntity(0));
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations(int times, boolean notFound, VDS entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVdsByVdsId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "hosts/" + GUIDS[0], false);
    }

    @Override
    protected VDS getEntity(int index) {
        VDS entity = setUpEntityExpectations(spy(new VDS()), null, index);
        return entity;
    }

    @Override
    protected void verifyModel(Host model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }
}
