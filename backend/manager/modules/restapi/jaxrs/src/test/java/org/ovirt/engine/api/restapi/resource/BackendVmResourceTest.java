package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpStatisticalEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Floppies;
import org.ovirt.engine.api.model.Floppy;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
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
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
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
        extends AbstractBackendSubResourceTest<Vm, org.ovirt.engine.core.common.businessentities.VM, BackendVmResource> {

    private static final String ISO_ID = "foo.iso";
    private static final String FLOPPY_ID = "bar.vfd";
    public static final String CERTIFICATE = "O=Redhat,CN=X.Y.Z.Q";
    private static final String PAYLOAD_COMTENT = "payload";

    public BackendVmResourceTest() {
        super(new BackendVmResource(GUIDS[0].toString(), new BackendVmsResource()));
    }

    @Override
    protected void init() {
        super.init();
        resource.getParent().mappingLocator = resource.mappingLocator;
        resource.getParent().httpHeaders = httpHeaders;
        resource.getParent().messageBundle = messageBundle;
        OsTypeMockUtils.mockOsTypes();
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
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations();
        control.replay();
        Vm response = resource.get();
        verifyModel(response, 0);
        verifyCertificate(response);
    }

    @Test
    public void testGetNextConfiguration() throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendVmResource.NEXT_RUN));
        setUpGetEntityNextRunExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetNextRunGraphicsExpectations(1);
        setUpGetCertuficateExpectations();
        control.replay();
        Vm response = resource.get();
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
            setUpGetGraphicsExpectations(1);
            control.replay();

            Vm vm = resource.get();
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
            List<String> populates = new ArrayList<>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(0);
            setUpGetVirtioScsiExpectations(0);
            setUpGetSoundcardExpectations(0);
            setUpGetRngDeviceExpectations(0);
            setUpGetVmOvfExpectations(0);
        }
        setUpGetEntityExpectations(1);
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations();
        control.replay();
        Vm response = resource.get();
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

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
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

        setUpGetPayloadExpectations(0, 1);
        setUpGetNoPayloadExpectations(0, 1);

        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);


        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModelClearingPayloads(resource.update(getModelClearingPayloads()), 0);
    }

    @Test
    public void testUpdateUploadIcon() throws Exception {
        setUpGetEntityExpectations(3);

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        final Vm model = getModel(0);
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateUseExistingIcons() throws Exception {
        setUpGetEntityExpectations(3);

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[]{},
                new Object[]{},
                true,
                true));
        final Vm model = getModel(0);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIcon(GUIDS[3]));
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateSetAndUploadIconFailure() throws Exception {
        control.replay();
        final Vm model = getModel(0);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        try {
            verifyModel(resource.update(model), 0);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BAD_REQUEST);
        }
    }

    protected void verifyModelClearingPayloads(Vm model, int index) {
        verifyModel(model, index);
        assertNull(model.getPayloads());
    }

    static Vm getModelClearingPayloads() {
        Vm model = getModel(0);
        model.setPayloads(new Payloads());

        return model;
    }

    @Test
    public void testUpdateVmPolicySingleHostName() throws Exception {
        setUpUdpateVm();
        setUpGetHostByNameExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));


        Vm model = getModel(0);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHosts(new Hosts());
        model.getPlacementPolicy().getHosts().getHosts().add(new Host());
        model.getPlacementPolicy().getHosts().getHosts().get(0).setName(NAMES[1]);
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateVmPolicySingleHostId() throws Exception {
        setUpUdpateVm();
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Vm model = getModel(0);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHosts(new Hosts());
        model.getPlacementPolicy().getHosts().getHosts().add(new Host());
        model.getPlacementPolicy().getHosts().getHosts().get(0).setId(GUIDS[1].toString());
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateVmPolicyHostsIds() throws Exception {
        setUpUdpateVm();
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Vm model = getModel(0);
        model.setPlacementPolicy(new VmPlacementPolicy());
        Hosts hosts = new Hosts();
        for (int i =0; i < GUIDS.length; i++){
            Host newHost = new Host();
            newHost.setId(GUIDS[i].toString());
            hosts.getHosts().add(newHost);
        }
        model.getPlacementPolicy().setHosts(hosts);
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateVmPolicyHostsNames() throws Exception {
        setUpUdpateVm();
        for (int i =0; i < NAMES.length; i++){
            setUpGetHostByNameExpectations(i);
        }
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Vm model = getModel(0);
        model.setPlacementPolicy(new VmPlacementPolicy());
        Hosts hosts = new Hosts();
        for (int i =0; i < NAMES.length; i++){
            Host newHost = new Host();
            newHost.setName(NAMES[i]);
            hosts.getHosts().add(newHost);
        }
        model.getPlacementPolicy().setHosts(hosts);
        verifyModel(resource.update(model), 0);
    }

    private void setUpUdpateVm() throws Exception {
        setUpGetEntityExpectations(3);

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUpGetGraphicsExpectations(1);
    }

    @Test
    public void testUpdateMovingCluster() throws Exception {
        setUpGetEntityExpectations(3);

        setUpGetPayloadExpectations(0, 2);
        setUpGetBallooningExpectations();
        setUpGetBallooningExpectations();
        setUpGetConsoleExpectations(0);
        setUpGetVmOvfExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUpGetGraphicsExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVMCluster,
                ChangeVMClusterParameters.class,
                new String[]{"ClusterId", "VmId"},
                new Object[]{GUIDS[1], GUIDS[0]},
                true,
                true,
                false));

        setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[]{},
                new Object[]{},
                true,
                true);

        Vm model = getModel(0);
        model.setId(GUIDS[0].toString());
        model.setCluster(new org.ovirt.engine.api.model.Cluster());
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

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(2);

        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                VmManagementParametersBase.class,
                new String[] {},
                new Object[] {},
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
        setUpGetEntityExpectations(2);

        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);

        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        Vm model = getModel(1);
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
                new String[]{"VmId"},
                new Object[]{GUIDS[0]}));

        Action action = new Action();
        action.setVm(new Vm());
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
        action.setVm(new Vm());
        action.getVm().setStateless(true);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithSpice() throws Exception {
        testStartWithModifiedGraphics(GraphicsType.SPICE);
    }

    @Test
    public void testStartWithVnc() throws Exception {
        testStartWithModifiedGraphics(GraphicsType.VNC);
    }

    private void testStartWithModifiedGraphics(GraphicsType graphicsType) throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "RunOnceGraphics" },
                                           new Object[] { GUIDS[0], Collections.singleton(graphicsType) }));

        Action action = new Action();
        action.setVm(new Vm());
        action.getVm().setDisplay(new Display());
        DisplayType display = (graphicsType == GraphicsType.VNC)
                ? DisplayType.VNC
                : DisplayType.SPICE;
        action.getVm().getDisplay().setType(display);

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
        action.setVm(new Vm());
        action.getVm().setOs(new OperatingSystem());
        action.getVm().getOs().setBoot(new Boot());
        action.getVm().getOs().getBoot().setDevices(new Boot.DevicesList());
        action.getVm().getOs().getBoot().getDevices().getDevices().add(BootDevice.NETWORK);

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
        action.setVm(new Vm());
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
        action.setVm(new Vm());
        action.getVm().setCdroms(new Cdroms());
        action.getVm().getCdroms().getCdroms().add(new Cdrom());
        action.getVm().getCdroms().getCdroms().get(0).setFile(new File());
        action.getVm().getCdroms().getCdroms().get(0).getFile().setId(ISO_ID);
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
        setUpGetHostByNameExpectations(1);

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
        action.setVm(new Vm());
        VmPlacementPolicy placementPolicy = new VmPlacementPolicy();
        placementPolicy.setHosts(new Hosts());
        placementPolicy.getHosts().getHosts().add(host);
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
        verifyActionResponse(response, "vms/" + GUIDS[0], true, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus());

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
    public void testReorderMacAddresses() throws Exception {

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                control.createMock(org.ovirt.engine.core.common.businessentities.VM.class));

        setUriInfo(setUpActionExpectations(VdcActionType.ReorderVmNics,
                                           VmOperationParameterBase.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.reorderMacAddresses(new Action()));
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
        setUpGetEntityExpectations(1);
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
    public void testFreezeFilesystems() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.FreezeVm,
                VmOperationParameterBase.class,
                new String[] { "VmId" },
                new Object[] { GUIDS[0] }));

        Action action = new Action();
        verifyActionResponse(resource.freezeFilesystems(action));
    }

    @Test
    public void testThawFilesystems() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ThawVm,
                VmOperationParameterBase.class,
                new String[] { "VmId" },
                new Object[] { GUIDS[0] }));

        Action action = new Action();
        verifyActionResponse(resource.thawFilesystems(action));
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
        assertEquals(CreationStatus.COMPLETE.value(), actionResponse.getStatus());
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
        assertEquals(CreationStatus.COMPLETE.value(), action.getStatus());
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
        assertEquals(CreationStatus.COMPLETE.value(), action.getStatus());
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
        Vm vm = new Vm();
        vm.setName("someNewName");
        action.setVm(vm);

        Response response = resource.doClone(action);
        verifyActionResponse(response);
        Action actionResponse = (Action)response.getEntity();
        assertTrue(actionResponse.isSetStatus());
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        setUpGetHostByNameExpectations(1);

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
    public void testMigrateWithClusterId() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVm,
                MigrateVmParameters.class,
                new String[] { "VmId", "ForceMigrationForNonMigratableVm", "TargetClusterId"},
                new Object[] { GUIDS[0], Boolean.FALSE, GUIDS[1]}));

        Action action = new Action();
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        action.setCluster(cluster);

        verifyActionResponse(resource.migrate(action));
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
                                           MoveOrCopyParameters.class,
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
    public void testStatisticalQuery() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Vm, org.ovirt.engine.core.common.businessentities.VM> statisticsResource =
            (BackendStatisticsResource<Vm, org.ovirt.engine.core.common.businessentities.VM>)resource.getStatisticsResource();
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

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        setUpActionExpectations(VdcActionType.RemoveVm, RemoveVmParameters.class, new String[] {
                "VmId", "Force" }, new Object[] { GUIDS[0], Boolean.FALSE }, true, true);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForced() throws Exception {
        setUpGetEntityExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveVm,
            RemoveVmParameters.class,
            new String[] { "VmId", "Force" },
            new Object[] { GUIDS[0], Boolean.TRUE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendVmResource.FORCE, Boolean.TRUE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveDetachOnly() throws Exception {
        setUpGetEntityExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveVm,
            RemoveVmParameters.class,
            new String[] { "VmId", "RemoveDisks" },
            new Object[] { GUIDS[0], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendVmResource.DETACH_ONLY, Boolean.TRUE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForcedIncomplete() throws Exception {
        setUpGetEntityExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveVm,
            RemoveVmParameters.class,
            new String[] { "VmId", "Force" },
            new Object[] { GUIDS[0], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendVmResource.DETACH_ONLY, Boolean.FALSE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new org.ovirt.engine.core.common.businessentities.VM());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUpGetPayloadExpectations(0, 1);
        setUpGetBallooningExpectations();
        setUpGetGraphicsExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVm,
                RemoveVmParameters.class,
                new String[] { "VmId", "Force" },
                new Object[] { GUIDS[0], Boolean.FALSE },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected org.ovirt.engine.core.common.businessentities.VM setUpStatisticalExpectations() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        setUpStatisticalEntityExpectations(entity, entity.getStatisticsData());
        setUpGetEntityExpectations(1, false, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Vm, org.ovirt.engine.core.common.businessentities.VM> query,
                               org.ovirt.engine.core.common.businessentities.VM entity)
        throws Exception {
        assertEquals(Vm.class, query.getParentType());
        assertSame(entity, query.resolve(GUIDS[0]));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                new String[]{"memory.installed", "memory.used", "memory.free", "memory.buffered", "memory.cached",
                        "cpu.current.guest", "cpu.current.hypervisor", "cpu.current.total", "migration.progress"},
                new BigDecimal[]{asDec(10 * Mb), asDec(2 * Mb), asDec(5 * Mb), asDec(2 * Mb), asDec(1 * Mb),
                        asDec(30), asDec(40), asDec(70), asDec(50)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetVm());
        assertEquals(GUIDS[0].toString(), adopted.getVm().getId());
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

    protected void verifyModelOnNewCluster(Vm model, int index) {
        assertNotNull(model.getCluster().getId());
        assertEquals(GUIDS[1].toString(), model.getCluster().getId());
        verifyModel(model, index);
    }

    @Override
    protected void verifyModel(Vm model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    protected void verifyActionModel(Vm model, int index) {
        assertNotNull(model);
        assertEquals(GUIDS[index].toString(), model.getId());
        verifyLinks(model);
    }

    private void verifyCertificate(Vm model) {
        assertNotNull(model.getDisplay());
        assertNotNull(model.getDisplay().getCertificate());
        assertEquals(CERTIFICATE, model.getDisplay().getCertificate().getSubject());
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
        payload.setDeviceType(VmDeviceType.CDROM);
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

    protected void setUpGetGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    protected void setUpGetNextRunGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetNextRunGraphicsDevices,
                    IdQueryParameters.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
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

    protected void setUpGetHostByNameExpectations(int idx) throws Exception {
        VDS host = BackendHostsResourceTest.setUpEntityExpectations(control.createMock(VDS.class), idx);
        setUpGetEntityExpectations(VdcQueryType.GetVdsByName,
                NameQueryParameters.class,
                new String[]{"Name"},
                new Object[]{NAMES[idx]},
                host);
    }
}
