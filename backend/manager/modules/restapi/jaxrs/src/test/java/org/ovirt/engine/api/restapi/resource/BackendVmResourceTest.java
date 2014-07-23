package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpStatisticalEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Floppies;
import org.ovirt.engine.api.model.Floppy;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmResourceTest
        extends AbstractBackendSubResourceTest<VM, org.ovirt.engine.core.common.businessentities.VM, BackendVmResource> {

    private static final String ISO_ID = "foo.iso";
    private static final String FLOPPY_ID = "bar.vfd";
    public static final String CERTIFICATE = "O=Redhat,CN=X.Y.Z.Q";
    private static final String PAYLOAD_COMTENT = "payload";
    private static HashMap<Integer, String> osNames = new HashMap<>();

    protected VmHelper vmHelper = VmHelper.getInstance();

    public BackendVmResourceTest() {
        super(new BackendVmResource(GUIDS[0].toString(), new BackendVmsResource()));
    }

    @Override
    protected void init() {
        super.init();
        resource.getParent().backend = backend;
        resource.getParent().sessionHelper = sessionHelper;
        resource.getParent().mappingLocator = resource.mappingLocator;
        resource.getParent().httpHeaders = httpHeaders;
        resource.getParent().messageBundle = messageBundle;
        OsTypeMockUtils.mockOsTypes();
        initBackendResource(vmHelper);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendVmResource("foo", new BackendVmsResource());
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
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetCertuficateExpectations();
        control.replay();
        VM response = resource.get();
        verifyModel(response, 0);
        verifyCertificate(response);
    }

    @Test
    public void testGetNextConfiguration() throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendVmResource.NEXT_RUN));
        setUpGetEntityNextRunExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetCertuficateExpectations();
        control.replay();
        VM response = resource.get();
        verifyModel(response, 0);
        verifyCertificate(response);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setUpGetEntityExpectations(1);
            setUpGetPayloadExpectations(0, 1);
            setUpGetBallooningExpectations();
            control.replay();

            VM vm = resource.get();
            assertTrue(vm.isSetStatistics());
            verifyModel(vm, 0);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testGetWithConsoleSet() throws Exception {
        testGetConsoleAware(true);
    }

    @Test
    public void testGetWithConsoleNotSet() throws Exception {
        testGetConsoleAware(false);
    }

    public void testGetConsoleAware(boolean allContent) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        if (allContent) {
            List<String> populates = new ArrayList<String>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(new int[]{0});
            setUpGetVirtioScsiExpectations(new int[]{0});
            setUpGetSoundcardExpectations(new int[]{0});
            setUpGetRngDeviceExpectations(new int[]{0});
            setUpGetVmOvfExpectations(new int[]{0});
        }
        setUpGetEntityExpectations(1);
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetCertuficateExpectations();
        control.replay();
        VM response = resource.get();
        verifyModel(response, 0);
        verifyCertificate(response);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.POPULATE);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;
        assertTrue(populated ? response.isSetConsole() : !response.isSetConsole());
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
        setUpGetEntityExpectations(3);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetConsoleExpectations(new int[]{0});
        setUpGetVmOvfExpectations(new int[]{0});
        setUpGetVirtioScsiExpectations(new int[] {0});
        setUpGetSoundcardExpectations(new int[] {0});
        setUpGetRngDeviceExpectations(new int[]{0});
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateRemovingPayloads() throws Exception {
        setUpGetEntityExpectations(3);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetPayloadExpectations(0, 1);
        setUpGetNoPayloadExpectations(0, 1);

        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetConsoleExpectations(new int[]{0});
        setUpGetVmOvfExpectations(new int[]{0});
        setUpGetVirtioScsiExpectations(new int[] {0});
        setUpGetSoundcardExpectations(new int[] {0});
        setUpGetRngDeviceExpectations(new int[]{0});

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModelClearingPayloads(resource.update(getModelClearingPayloads(0)), 0);
    }

    protected void verifyModelClearingPayloads(VM model, int index) {
        verifyModel(model, index);
        assertNull(model.getPayloads());
    }

    static VM getModelClearingPayloads(int index) {
        VM model = getModel(0);
        model.setPayloads(new Payloads());

        return model;
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        return new VDSGroup();
    }

    @Test
    public void testUpdateVmPolicy() throws Exception {
        setUpGetEntityExpectations(3);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetConsoleExpectations(new int[]{0});
        setUpGetVmOvfExpectations(new int[]{0});
        setUpGetVirtioScsiExpectations(new int[] {0});
        setUpGetSoundcardExpectations(new int[] {0});
        setUpGetRngDeviceExpectations(new int[]{0});
        setUpEntityQueryExpectations(VdcQueryType.GetVdsStaticByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                getStaticHost());
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        VM model = getModel(0);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHost(new Host());
        model.getPlacementPolicy().getHost().setName(NAMES[1]);
        verifyModel(resource.update(model), 0);
    }

    private VdsStatic getStaticHost() {
        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(GUIDS[2]);
        return vdsStatic;
    }

    @Test
    public void testUpdateMovingCluster() throws Exception {
        setUpGetEntityExpectations(3);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getVdsGroupEntity());

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVMCluster,
                                           ChangeVMClusterParameters.class,
                                           new String[] {"ClusterId", "VmId"},
                                           new Object[] {GUIDS[1], GUIDS[0]},
                                           true,
                                           true,
                                           false));

        setUpActionExpectations(VdcActionType.UpdateVm,
                                VmManagementParametersBase.class,
                                new String[] {},
                                new Object[] {},
                                true,
                                true);

        VM model = getModel(0);
        model.setId(GUIDS[0].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        verifyModelOnNewCluster(resource.update(model), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(2);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
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
        setUpGetEntityExpectations(2);

        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();

        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        VM model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testStart() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RunVm,
                                           RunVmParams.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        Response response = resource.start(new Action());
        verifyActionResponse(response);
        verifyActionModel(((Action) response.getEntity()).getVm(), 0);
    }

    @Test
    public void testStartWithVm() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);

        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        Action action = new Action();
        action.setVm(new VM());
        Response response = resource.start(action);
        verifyActionResponse(response);
        verifyActionModel(((Action) response.getEntity()).getVm(), 0);
    }

    @Test
    public void testStartWithPauseAndStateless() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "RunAndPause", "RunAsStateless" },
                                           new Object[] { GUIDS[0], true, Boolean.TRUE }));

        Action action = new Action();
        action.setPause(true);
        action.setVm(new VM());
        action.getVm().setStateless(true);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithVnc() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "UseVnc" },
                                           new Object[] { GUIDS[0], Boolean.TRUE }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setDisplay(new Display());
        action.getVm().getDisplay().setType(DisplayType.VNC.value());

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithBootDev() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "BootSequence" },
                                           new Object[] { GUIDS[0], BootSequence.N }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setOs(new OperatingSystem());
        action.getVm().getOs().getBoot().add(new Boot());
        action.getVm().getOs().getBoot().get(0).setDev(BootDevice.NETWORK.value());

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithBootMenu() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "BootMenuEnabled" },
                                           new Object[] { GUIDS[0], true }));
        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setBios(new Bios());
        action.getVm().getBios().setBootMenu(new BootMenu());
        action.getVm().getBios().getBootMenu().setEnabled(true);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithCdRomAndFloppy() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "DiskPath", "FloppyPath" },
                                           new Object[] { GUIDS[0], ISO_ID, FLOPPY_ID }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setCdroms(new CdRoms());
        action.getVm().getCdroms().getCdRoms().add(new CdRom());
        action.getVm().getCdroms().getCdRoms().get(0).setFile(new File());
        action.getVm().getCdroms().getCdRoms().get(0).getFile().setId(ISO_ID);
        action.getVm().setFloppies(new Floppies());
        action.getVm().getFloppies().getFloppies().add(new Floppy());
        action.getVm().getFloppies().getFloppies().get(0).setFile(new File());
        action.getVm().getFloppies().getFloppies().get(0).getFile().setId(FLOPPY_ID);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithHostId() throws Exception {
        Host host = new Host();
        host.setId(GUIDS[1].toString());

        testStartWithHost(host, GUIDS[1]);
    }

    @Test
    public void testStartWithHostName() throws Exception {
        setUpGetHostIdExpectations(1);

        Host host = new Host();
        host.setName(NAMES[1]);

        testStartWithHost(host, GUIDS[1]);
    }

    protected void testStartWithHost(Host host, Guid hostId) throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "DestinationVdsId" },
                                           new Object[] { GUIDS[0], hostId }));

        Action action = new Action();
        action.setVm(new VM());
        VmPlacementPolicy placementPolicy = new VmPlacementPolicy();
        placementPolicy.setHost(host);
        action.getVm().setPlacementPolicy(placementPolicy);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testSuspend() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HibernateVm,
                                           VmOperationParameterBase.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.suspend(new Action()));
    }

    @Test
    public void testSuspendAsyncPending() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testSuspendAsyncInProgress() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testSuspendAsyncFinished() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestSuspendAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HibernateVm,
                                                VmOperationParameterBase.class,
                                                new String[] { "VmId" },
                                                new Object[] { GUIDS[0] },
                                                asList(GUIDS[1]),
                                                asList(new AsyncTaskStatus(asyncStatus))));

        Response response = resource.suspend(new Action());
        verifyActionResponse(response, "vms/" + GUIDS[0], true, null, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());

    }

    @Test
    public void testShutdown() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ShutdownVm,
                                           ShutdownVmParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.shutdown(new Action()));
    }

    @Test
    public void testReboot() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RebootVm,
                                           VmOperationParameterBase.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.reboot(new Action()));
    }

    @Test
    public void testStop() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.StopVm,
                                           StopVmParameters.class,
                                           new String[] { "VmId", "StopVmType" },
                                           new Object[] { GUIDS[0], StopVmTypeEnum.NORMAL }));

        verifyActionResponse(resource.stop(new Action()));
    }

    @Test
    public void testCancelMigration() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.CancelMigrateVm,
                                           VmOperationParameterBase.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.cancelMigration(new Action()));
    }

    @Test
    public void testDetach() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmFromPool,
                                           RemoveVmFromPoolParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.detach(new Action()));
    }

    @Test
    public void testTicket() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.SetVmTicket,
                                           SetVmTicketParameters.class,
                                           new String[] { "VmId", "Ticket" },
                                           new Object[] { GUIDS[0], NAMES[1] }));

        Action action = new Action();
        action.setTicket(new Ticket());
        action.getTicket().setValue(NAMES[1]);
        verifyActionResponse(resource.ticket(action));
    }

    @Test
    public void testLogon() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.VmLogon,
                                           VmOperationParameterBase.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        Action action = new Action();
        verifyActionResponse(resource.logon(action));
    }

    @Test
    public void testMigrateWithHostId() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVmToServer,
                                           MigrateVmToServerParameters.class,
                                           new String[] { "VmId", "VdsId", "ForceMigrationForNonMigratableVm" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE }));

        Action action = new Action();
        action.setHost(new Host());
        action.getHost().setId(GUIDS[1].toString());
        verifyActionResponse(resource.migrate(action));
    }

    @Test
    public void testPreviewSnapshot() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.TryBackToAllSnapshotsOfVm,
                                           TryBackToAllSnapshotsOfVmParameters.class,
                                           new String[] { "VmId", "DstSnapshotId" },
                                           new Object[] { GUIDS[0], GUIDS[1] }));
        Action action = new Action();
        Snapshot snapshot = new Snapshot();
        snapshot.setId(GUIDS[1].toString());
        action.setSnapshot(snapshot);
        Response response = resource.previewSnapshot(action);
        verifyActionResponse(response);
        Action actionResponse = (Action)response.getEntity();
        assertTrue(actionResponse.isSetStatus());
        assertEquals(CreationStatus.COMPLETE.value(), actionResponse.getStatus().getState());
    }

    @Test
    public void testUndoSnapshot() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RestoreAllSnapshots,
                                           RestoreAllSnapshotsParameters.class,
                                           new String[] { "VmId", "SnapshotAction" },
                                           new Object[] { GUIDS[0], SnapshotActionEnum.UNDO }));
        Response response = resource.undoSnapshot(new Action());
        verifyActionResponse(response);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(CreationStatus.COMPLETE.value(), action.getStatus().getState());
    }

    @Test
    public void testCommitSnapshot() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RestoreAllSnapshots,
                                           RestoreAllSnapshotsParameters.class,
                                           new String[] { "VmId", "SnapshotAction" },
                                           new Object[] { GUIDS[0], SnapshotActionEnum.COMMIT }));
        Response response = resource.commitSnapshot(new Action());
        verifyActionResponse(response);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(CreationStatus.COMPLETE.value(), action.getStatus().getState());
    }

    @Test
    public void testCloneVm() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM mockedVm = control.createMock(org.ovirt.engine.core.common.businessentities.VM.class);
        VmStatic vmStatic = control.createMock(VmStatic.class);
        expect(mockedVm.getStaticData()).andReturn(vmStatic).anyTimes();

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId, IdQueryParameters.class, new String[]{"Id"}, new Object[]{GUIDS[0]}, mockedVm);

        setUriInfo(setUpActionExpectations(VdcActionType.CloneVm,
                CloneVmParameters.class,
                new String[] { "VmStaticData", "NewName" },
                new Object[] { vmStatic, "someNewName" }));

        Action action = new Action();
        VM vm = new VM();
        vm.setName("someNewName");
        action.setVm(vm);

        Response response = resource.cloneVm(action);
        verifyActionResponse(response);
        Action actionResponse = (Action)response.getEntity();
        assertTrue(actionResponse.isSetStatus());
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        setUpGetHostIdExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVmToServer,
                                           MigrateVmToServerParameters.class,
                                           new String[] { "VmId", "VdsId", "ForceMigrationForNonMigratableVm" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE }));

        Action action = new Action();
        action.setHost(new Host());
        action.getHost().setName(NAMES[1]);
        verifyActionResponse(resource.migrate(action));
    }

    @Test
    public void testMigrateNoHost() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVm,
                MigrateVmParameters.class,
                new String[] { "VmId", "ForceMigrationForNonMigratableVm" },
                new Object[] { GUIDS[0], Boolean.FALSE }));

        verifyActionResponse(resource.migrate(new Action()));
    }

    @Test
    public void testExport() throws Exception {
        testExportWithStorageDomainId(false, false);
    }

    @Test
    public void testExportWithParams() throws Exception {
        testExportWithStorageDomainId(true, true);
    }

    @Test
    public void testExportWithStorageDomainName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestExport(storageDomain, false, false);
    }

    protected void testExportWithStorageDomainId(boolean exclusive, boolean discardSnapshots) throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestExport(storageDomain, exclusive, discardSnapshots);
    }

    protected void doTestExport(StorageDomain storageDomain,
                                boolean exclusive,
                                boolean discardSnapshots) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportVm,
                                           MoveVmParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ForceOverride", "CopyCollapse" },
                                           new Object[] { GUIDS[0], GUIDS[2], exclusive, discardSnapshots }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        if (exclusive) {
            action.setExclusive(exclusive);
        }
        if (discardSnapshots) {
            action.setDiscardSnapshots(discardSnapshots);
        }
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.export(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
    }

    @Test
    public void testMoveWithStorageDomainId() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestMove(storageDomain);
    }

    @Test
    public void testMoveWithStorageDomainName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestMove(storageDomain);
    }

     @Test
    public void testIncompleteMove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.move(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "move", "storageDomain.id|name");
        }
    }

    protected void doTestMove(StorageDomain storageDomain) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MoveVm,
                       MoveVmParameters.class,
                       new String[] { "ContainerId", "StorageDomainId" },
                       new Object[] { GUIDS[0], GUIDS[2] }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        verifyActionResponse(resource.move(action));
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM> statisticsResource =
            (BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM>)resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    @Test
    public void testMaintenance() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.SetHaMaintenance,
                                           SetHaMaintenanceParameters.class,
                                           new String[] { "IsEnabled" },
                                           new Object[] { true }));

        Action action = new Action();
        action.setMaintenanceEnabled(true);
        verifyActionResponse(resource.maintenance(action));
    }

    protected org.ovirt.engine.core.common.businessentities.VM setUpStatisticalExpectations() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        setUpStatisticalEntityExpectations(entity, entity.getStatisticsData());
        setUpGetEntityExpectations(1, false, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<VM, org.ovirt.engine.core.common.businessentities.VM> query,
                               org.ovirt.engine.core.common.businessentities.VM entity)
        throws Exception {
        assertEquals(VM.class, query.getParentType());
        assertSame(entity, query.resolve(GUIDS[0]));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"memory.installed", "memory.used", "cpu.current.guest",
                                       "cpu.current.hypervisor", "cpu.current.total", "migration.progress"},
                         new BigDecimal[] {asDec(10*Mb), asDec(2*Mb), asDec(30), asDec(40), asDec(70), asDec(50)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetVm());
        assertEquals(GUIDS[0].toString(), adopted.getVm().getId());
    }

    protected void setUpGetHostIdExpectations(int idx) throws Exception {
        VDS host = BackendHostsResourceTest.setUpEntityExpectations(control.createMock(VDS.class), idx);
        setUpGetEntityExpectations(VdcQueryType.GetVdsByName,
                                   NameQueryParameters.class,
                                   new String[] { "Name" },
                                   new Object[] { NAMES[idx] },
                                   host);
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpWindowsGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times,
                                   notFound,
                new org.ovirt.engine.core.common.businessentities.VM() {{
                    setId(GUIDS[0]);
                    setVmOs(OsRepository.DEFAULT_X86_OS);
                }});
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.VM entity) throws Exception {

        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected void setUpGetEntityNextRunExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmNextRunConfiguration,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
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
        String uri = "vms/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + GUIDS[0], false);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        return setUpEntityExpectations(vm, vm.getStatisticsData(), index);
    }

    protected void verifyModelOnNewCluster(VM model, int index) {
        assertNotNull(model.getCluster().getId());
        assertEquals(GUIDS[1].toString(), model.getCluster().getId());
        verifyModel(model, index);
    }

    @Override
    protected void verifyModel(VM model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    protected void verifyActionModel(VM model, int index) {
        assertNotNull(model);
        assertEquals(GUIDS[index].toString(), model.getId());
        verifyLinks(model);
    }

    private void verifyCertificate(VM model) {
        assertNotNull(model.getDisplay());
        assertNotNull(model.getDisplay().getCertificate());
        assertEquals(model.getDisplay().getCertificate().getSubject(), CERTIFICATE);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomain dom = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        dom.setId(GUIDS[idx]);
        dom.setStorageName(NAMES[idx]);
        return dom;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStorageDomainStatic(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic dom =
                new org.ovirt.engine.core.common.businessentities.StorageDomainStatic();
        dom.setId(GUIDS[idx]);
        dom.setStorageName(NAMES[idx]);
        return dom;
    }

    protected void setUpGetPayloadExpectations(int index, int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVmPayload,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[index] },
                                       getPayloadModel());
        }
    }

    private VmPayload getPayloadModel() {
        VmPayload payload = new VmPayload();
        payload.setType(VmDeviceType.CDROM);
        payload.getFiles().put("payloadFile", new String(Base64.decodeBase64(PAYLOAD_COMTENT)));
        return payload;
    }

    protected void setUpGetNoPayloadExpectations(int index, int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVmPayload,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[index] },
                                       null);
        }
    }

    private void setUpGetBallooningExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.IsBalloonEnabled,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true);
    }

    private void setUpGetCertuficateExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVdsCertificateSubjectByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0]},
                CERTIFICATE);
    }

    private void setUpGetVirtioScsiExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVirtioScsiControllers,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    private void setUpGetSoundcardExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetSoundDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    private void setUpGetVmOvfExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVmOvfByVmId,
                    GetVmOvfByVmIdParameters.class,
                    new String[] { "Id", "RequiredGeneration" },
                    new Object[] { GUIDS[idxs[i]], 0L },
                    "configuration");
        }
    }
}
