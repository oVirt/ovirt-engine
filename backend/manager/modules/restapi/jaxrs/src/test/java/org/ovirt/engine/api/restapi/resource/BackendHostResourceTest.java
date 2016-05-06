package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpStatisticalEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostResourceTest
        extends AbstractBackendSubResourceTest<Host, VDS, BackendHostResource> {

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
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendHostResource("foo", new BackendHostsResource());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }


    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            VdsStatistics stats = control.createMock(VdsStatistics.class);
            VDS entity = getEntity(0);
            setUpStatisticalEntityExpectations(entity, stats);
            setUpGetEntityExpectations(1, false, entity);
            control.replay();

            Host host = resource.get();
            assertTrue(host.isSetStatistics());
            verifyModel(host, 0);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateWithClusterId() throws Exception {
        setUpGetEntityExpectations(3);
        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVDSCluster,
                ChangeVDSClusterParameters.class,
                new String[] { "ClusterId", "VdsId" },
                new Object[] { GUIDS[1],  GUIDS[0]},
                true,
                true,
                new VdcReturnValueBase(),
                false));

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
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
    public void testUpdateWithClusterName() throws Exception {
        String clusterName = "Default";
        setUpGetEntityExpectations(3);

        setUpEntityQueryExpectations(VdcQueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { clusterName },
                getCluster(clusterName, GUIDS[1]));

        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVDSCluster,
                ChangeVDSClusterParameters.class,
                new String[] { "ClusterId", "VdsId" },
                new Object[] { GUIDS[1], GUIDS[0] },
                true,
                true,
                new VdcReturnValueBase(),
                false));

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
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
        Cluster cluster = control.createMock(Cluster.class);
        expect(cluster.getId()).andReturn(id).anyTimes();
        expect(cluster.getName()).andReturn(name).anyTimes();
        return cluster;
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           valid,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityWithNoCertificateInfoExpectations();
        control.replay();

        Host model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ActivateVds,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.activate(new Action()));
    }

    @Test
    public void testActivateAsyncPending() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testActivateAsyncInProgress() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testActivateAsyncFinished() throws Exception {
        doTestActivateAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestActivateAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ActivateVds,
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
    public void testApprove() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ApproveVds,
                                           ApproveVdsParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.approve(new Action()));
    }

    @Test
    public void testApproveChangingCluster() throws Exception {
        setUpGetEntityExpectations(4);

        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVDSCluster,
                                           ChangeVDSClusterParameters.class,
                                           new String[] { "ClusterId", "VdsId" },
                                           new Object[] { GUIDS[0],  GUIDS[0]},
                                           true,
                                           true,
                                           new VdcReturnValueBase(),
                                           false));

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { },
                                           new Object[] { },
                                           true,
                                           true,
                                           false));

        setUriInfo(setUpActionExpectations(VdcActionType.ApproveVds,
                                           ApproveVdsParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.approve(new Action() {{ setCluster(new org.ovirt.engine.api.model.Cluster()); getCluster().setId(GUIDS[0].toString()); }}));
    }

    @Test
    public void testIscsiLogin() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ConnectStorageToVds,
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
    public void testIscsiDiscover() throws Exception {
        IscsiDetails iscsiDetails = new IscsiDetails();
        iscsiDetails.setAddress(ISCSI_SERVER_ADDRESS);
        iscsiDetails.setPort(ISCSI_PORT_INT);
        iscsiDetails.setUsername(ISCSI_USER_NAME);
        iscsiDetails.setPassword(ISCSI_USER_PASS);

        Action action = new Action();
        action.setIscsi(iscsiDetails);

        VdcQueryReturnValue queryResult = new VdcQueryReturnValue();
        queryResult.setSucceeded(true);

        expect(backend.runQuery(eq(VdcQueryType.DiscoverSendTargets),
                                eqQueryParams(DiscoverSendTargetsQueryParameters.class,
                                              addSession("VdsId", "Connection.Connection", "Connection.Port", "Connection.UserName", "Connection.Password"),
                                              addSession(GUIDS[0], ISCSI_SERVER_ADDRESS, ISCSI_PORT_STRING, ISCSI_USER_NAME, ISCSI_USER_PASS)
                                              ))).andReturn(queryResult);
        control.replay();
        resource.iscsiDiscover(action);
        verify(backend);
    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MaintenanceNumberOfVdss,
                                           MaintenanceNumberOfVdssParameters.class,
                                           new String[] { "VdsIdList" },
                                           new Object[] { asList(GUIDS[0]) }));

        verifyActionResponse(resource.deactivate(new Action()));
    }

    @Test
    public void testForceSelect() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ForceSelectSPM,
                                           ForceSelectSPMParameters.class,
                                           new String[] { "PreferredSPMId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.forceSelectSpm(new Action()));
    }

    @Test
    public void testInstall() throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations();

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { NAMES[2] }));

        Action action = new Action();
        action.setRootPassword(NAMES[2]);
        verifyActionResponse(resource.install(action));
    }

    @Test
    public void testUpgrade() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.UpgradeHost,
                                           UpgradeHostParameters.class,
                                           new String[] { "VdsId", "oVirtIsoFile" },
                                           new Object[] { GUIDS[0], NAMES[0] }));

        Action action = new Action();
        action.setImage(NAMES[0]);
        verifyActionResponse(resource.upgrade(action));
    }

    @Test
    public void testCommitNetConfig() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.CommitNetworkChanges,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.commitNetConfig(new Action()));
    }

    @Test
    public void testManualFence() throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations();

        setUriInfo(setUpActionExpectations(VdcActionType.FenceVdsManualy,
                                           FenceVdsManualyParameters.class,
                                           new String[] { "VdsId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[1] }));

        Action action = new Action();
        action.setFenceType(FenceType.MANUAL.value());

        verifyActionResponse(resource.fence(action));
    }

    @Test
    public void testRestartFence() throws Exception {
        doTestFence(FenceType.RESTART,
                    VdcActionType.RestartVds);
    }

    @Test
    public void testStartFence() throws Exception {
        doTestFence(FenceType.START,
                    VdcActionType.StartVds);
    }

    @Test
    public void testStopFence() throws Exception {
        doTestFence(FenceType.STOP,
                    VdcActionType.StopVds);
    }

    public void doTestFence(FenceType fenceType,
                            VdcActionType actionType) throws Exception {
        setUriInfo(setUpActionExpectations(actionType,
                                           FenceVdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] }));

        Action action = new Action();
        action.setFenceType(fenceType.value());

        verifyActionResponse(resource.fence(action));
    }

    @Test
    public void testIncompleteFence() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.fence(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "fence", "fenceType");
        }
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
    public void testFenceStatus() throws Exception {
        VDSReturnValue retVal = new VDSReturnValue();
        retVal.setSucceeded(true);
        retVal.setReturnValue(new FenceOperationResult(FenceOperationResult.Status.SUCCESS, PowerStatus.ON));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsFenceStatus,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                retVal);
        control.replay();
        Action action = new Action();
        action.setFenceType(FenceType.STATUS.value());
        verifyActionResponse(resource.fence(action));
        PowerManagementStatus status = action.getPowerManagement().getStatus();
        assertEquals(PowerManagementStatus.ON, status);
    }

    @Test
    public void testFenceStatusFailure() throws Exception {
        VDSReturnValue retVal = new VDSReturnValue();
        retVal.setSucceeded(true);
        retVal.setReturnValue(
                new FenceOperationResult(
                        FenceOperationResult.Status.ERROR,
                        PowerStatus.UNKNOWN,
                        "some_error"));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsFenceStatus,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                retVal);
        control.replay();
        Action action = new Action();
        action.setFenceType(FenceType.STATUS.value());
        Response response = resource.fence(action);
        Action actionReturned = (Action)response.getEntity();
        assertEquals(actionReturned.getStatus(), CreationStatus.FAILED.value());
        assertNotNull(actionReturned.getFault());
        assertEquals("some_error", actionReturned.getFault().getReason());
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVds,
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
    public void testRemoveNonExistant() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveForced() throws Exception {
        setUpGetEntityExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveVds,
            RemoveVdsParameters.class,
            new String[] { "VdsId", "ForceAction" },
            new Object[] { GUIDS[0], Boolean.TRUE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendHostResource.FORCE, Boolean.TRUE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForcedIncomplete() throws Exception {
        setUpGetEntityExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveVds,
            RemoveVdsParameters.class,
            new String[] { "VdsId", "ForceAction" },
            new Object[] { GUIDS[0], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendHostResource.FORCE, Boolean.FALSE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpGetEntityExpectations(1);
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpGetEntityExpectations(1);
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVds,
                RemoveVdsParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testEnrollCertificate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HostEnrollCertificate,
                VdsActionParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.enrollCertificate(new Action()));
    }

    protected VDS setUpStatisticalExpectations() throws Exception {
        VdsStatistics stats = control.createMock(VdsStatistics.class);
        VDS entity = control.createMock(VDS.class);
        setUpStatisticalEntityExpectations(entity, stats);
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Host, VDS> query, VDS entity) throws Exception {
        assertEquals(Host.class, query.getParentType());
        assertSame(entity, query.resolve(GUIDS[0]));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"memory.total", "memory.used", "memory.free", "memory.shared",
                                       "memory.buffers", "memory.cached", "swap.total", "swap.free", "swap.used",
                                       "swap.cached", "ksm.cpu.current", "cpu.current.user", "cpu.current.system",
                                       "cpu.current.idle", "cpu.load.avg.5m", "boot.time"},
                         new BigDecimal[] {asDec(5120*Mb), asDec(1024*Mb), asDec(4096*Mb), asDec(38*Mb), asDec(0), asDec(0), asDec(30*Mb),
                                           asDec(25*Mb), asDec(5*Mb), asDec(0), asDec(40), asDec(45), asDec(50), asDec(55), new BigDecimal(0.0060, new MathContext(2)), asDec(0)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetHost());
        assertEquals(GUIDS[0].toString(), adopted.getHost().getId());
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "hosts/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, VDS entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations() throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, getEntity(0));
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations(int times, boolean notFound, VDS entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "hosts/" + GUIDS[0], false);
    }

    @Override
    protected VDS getEntity(int index) {
        VDS entity = setUpEntityExpectations(control.createMock(VDS.class), null, index);
        return entity;
    }

    @Override
    protected void verifyModel(Host model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }
}
