package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.ConfigurationType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class BackendVmsResourceTest
        extends AbstractBackendCollectionResourceTest<Vm, org.ovirt.engine.core.common.businessentities.VM, BackendVmsResource> {

    private static final String DEFAULT_TEMPLATE_ID = Guid.Empty.toString();
    private static final String PAYLOAD_COMTENT = "payload";
    public static final String CERTIFICATE = "O=Redhat,CN=X.Y.Z.Q";

    private OsRepository osRepository;

    public BackendVmsResourceTest() {
        super(new BackendVmsResource(), SearchType.VM, "VMs : ");
    }

    @Override
    public void init() {
        super.init();
        OsTypeMockUtils.mockOsTypes();
        osRepository = control.createMock(OsRepository.class);
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);

        Config.setConfigUtils(new VmMapperMockConfigUtils());
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            UriInfo uriInfo = setUpUriExpectations(null);

            org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
            VmStatistics vmStatistics = new VmStatistics();
            vmStatistics.setcpu_sys(0D);
            vmStatistics.setcpu_user(0D);
            vmStatistics.setelapsed_time(0D);
            vmStatistics.setRoundedElapsedTime(0D);
            vmStatistics.setusage_cpu_percent(0);
            vmStatistics.setusage_mem_percent(0);
            vmStatistics.setusage_network_percent(0);
            vm.setStatisticsData(vmStatistics);
            vm.setMigrationProgressPercent(50);
            for (int i=0; i<GUIDS.length-1; i++) {
                setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                        IdQueryParameters.class,
                        new String[] { "Id" },
                        new Object[] { GUIDS[i] },
                        vm);
            }
            setUpGetGraphicsExpectations(3);
            setUpQueryExpectations("");
            collection.setUriInfo(uriInfo);
            List<Vm> vms = getCollection();
            assertTrue(vms.get(0).isSetStatistics());
            verifyCollection(vms);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testAddAsyncPending() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 0);
        setUpGetBallooningExpectations(1, 0);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(new int[]{0});
        setUpGetVirtioScsiExpectations(new int[]{0});
        setUpGetSoundcardExpectations(new int[] { 0 });
        setUpGetRngDeviceExpectations(new int[] { 0 });
        setUpGetVmOvfExpectations(new int[]{0});
        setUpGetCertuficateExpectations(1, 0);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getVdsGroupEntity());

        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getTemplateEntity(0));
        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{Guid.Empty},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[1]),
                asList(new AsyncTaskStatus(asyncStatus)),
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                getEntity(0));
        Vm model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 0);
        Vm created = (Vm)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus().getState());
    }

    @Test
    public void testAddFromScratch() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetPayloadExpectations(2, 0);
        setUpGetConsoleExpectations(new int[]{0, 0});
        setUpGetVmOvfExpectations(new int[]{0, 0});
        setUpGetVirtioScsiExpectations(new int[]{0, 0});
        setUpGetSoundcardExpectations(new int[]{0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});
        setUpGetBallooningExpectations(2, 0);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(2, 0);
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getVdsGroupEntity());
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{Guid.Empty},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[1]),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                getEntity(0));
        Vm model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 0);
        assertNull(((Vm) response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddFromScratchNamedCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(2, 0);
        setUpGetBallooningExpectations(2, 0);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(new int[]{0, 0});
        setUpGetVmOvfExpectations(new int[]{0, 0});
        setUpGetVirtioScsiExpectations(new int[]{0, 0});
        setUpGetSoundcardExpectations(new int[]{0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});
        setUpGetCertuficateExpectations(2, 0);
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpVDSGroup(GUIDS[1]));

        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { Guid.Empty },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Vm model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[1]);
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 0);
    }

    @Test
    public void testAddFromScratchCantDo() throws Exception {
        doTestBadAddFromScratch(false, true, CANT_DO);
    }

    @Test
    public void testAddFromScratchFailure() throws Exception {
        doTestBadAddFromScratch(true, false, FAILURE);
    }

    private void doTestBadAddFromScratch(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getTemplateEntity(0));


        expect(osRepository.isBalloonEnabled(anyInt(), anyObject(Version.class))).andReturn(false).anyTimes();

        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[1]},
                getVdsGroupEntity());
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmFromScratch,
                                           AddVmParameters.class,
                                           new String[] { "StorageDomainId" },
                                           new Object[] { Guid.Empty },
                                           canDo,
                                           success));
        Vm model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testCloneWithDisk() throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendVmsResource.CLONE, "true"));
        setUpTemplateDisksExpectations(GUIDS[1]);
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpGetCertuficateExpectations(1, 2);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());
        setUpCreationExpectations(VdcActionType.AddVmFromTemplate,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Response response = collection.add(createModel(createDisksCollection()));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testCloneVmFromSnapshot() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        org.ovirt.engine.core.common.businessentities.VM vmConfiguration = getEntity(0);
        Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> diskImageMap = new HashMap<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk>();
        diskImageMap.put(Guid.newGuid(), new DiskImage());
        vmConfiguration.setDiskMap(diskImageMap);
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(2, 2);
        setUpGetVmOvfExpectations(2);
        setUpGetVirtioScsiExpectations(2);
        setUpGetSoundcardExpectations(2);
        setUpGetRngDeviceExpectations(2);
        setUpEntityQueryExpectations(VdcQueryType.GetVmConfigurationBySnapshot,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[1]},
                vmConfiguration);
        setUpCreationExpectations(VdcActionType.AddVmFromSnapshot,
                AddVmFromSnapshotParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Vm model = createModel(createDisksCollection(), createSnapshotsCollection(1));
        model.setTemplate(null);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testClone() throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendVmsResource.CLONE, "true"));
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(2);
        setUpGetVmOvfExpectations(2);
        setUpGetVirtioScsiExpectations(2);
        setUpGetSoundcardExpectations(1, 2);
        setUpGetRngDeviceExpectations(1, 2);
        setUpGetCertuficateExpectations(1, 2);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[1]},
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getVdsGroupEntity());
        setUpCreationExpectations(VdcActionType.AddVmFromTemplate,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Response response = collection.add(createModel(new Disks()));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddStatelessWithLatestTemplateVersion() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 2, 1 });
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[1]},
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getVdsGroupEntity());

        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(2);
        vm.setVmtGuid(GUIDS[1]);
        vm.setStateless(true);
        vm.setUseLatestVersion(true);

        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                vm);

        Response response = collection.add(createModel(null));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        Vm returnValueVM = (Vm) response.getEntity();
        verifyModel(returnValueVM, 2);
        assertTrue(returnValueVM.isStateless());
        assertTrue(returnValueVM.isUseLatestTemplateVersion());
    }

    @Test
    public void testAdd() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setupAddExpectations();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getEntity(2));
        Response response = collection.add(createModel(null));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddPassTemplateByName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Name", "DataCenterId" },
                new Object[] { NAMES[1], GUIDS[3] },
                getTemplateEntity(1));
        setupAddExpectations();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getEntity(2));
        Vm model = getModel(2);
        model.setTemplate(new Template());
        model.getTemplate().setName(NAMES[1].toString());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    private void setupAddExpectations() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(new int[] { 2 });
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpGetVmOvfExpectations(new int[]{2});
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getVdsGroupEntity());
    }


    @Test
    public void testAddFromConfigurationWithRegenerateTrue() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 3);
        setUpGetBallooningExpectations(1, 3);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 3);
        setUpGetConsoleExpectations(new int[] { 3 });
        setUpGetVmOvfExpectations(new int[] { 3 });
        setUpGetVirtioScsiExpectations(new int[] { 3 });
        setUpGetSoundcardExpectations(new int[]{3});
        setUpGetRngDeviceExpectations(new int[]{3});
        Vm model = createModel(null);
        org.ovirt.engine.core.common.businessentities.VM returnedVM = getEntity(2);
        model.setInitialization(new Initialization());
        model.getInitialization().setRegenerateIds(Boolean.TRUE);
        model.getInitialization().setConfiguration(new Configuration());
        model.getInitialization().getConfiguration().setData("asdasdasd");
        model.getInitialization().getConfiguration().setType("ovf");
        setUpGetEntityExpectations(VdcQueryType.GetVmFromConfiguration,
                GetVmFromConfigurationQueryParameters.class,
                new String[]{"VmConfiguration", "ConfigurationType"},
                new Object[]{model.getInitialization().getConfiguration().getData(), ConfigurationType.OVF},
                returnedVM);
        Guid newId = GUIDS[3];
        setUpCreationExpectations(VdcActionType.ImportVmFromConfiguration,
                ImportVmParameters.class,
                new String[] { "Vm", "VdsGroupId", "ImportAsNewEntity" },
                new Object[] { returnedVM, Guid.createGuidFromString(model.getCluster().getId()), true},
                true,
                true,
                newId,
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { newId },
                getEntityWithProvidedId(2, newId));
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        Vm queriedVm = (Vm) response.getEntity();
        assertEquals(newId.toString(), queriedVm.getId());
        queriedVm.setId(GUIDS[2].toString());
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddFromConfiguration() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[]{2});
        setUpGetRngDeviceExpectations(new int[]{2});
        Vm model = createModel(null);
        org.ovirt.engine.core.common.businessentities.VM returnedVM = getEntity(2);
        model.setInitialization(new Initialization());
        model.getInitialization().setConfiguration(new Configuration());
        model.getInitialization().getConfiguration().setData("asdasdasd");
        model.getInitialization().getConfiguration().setType("ovf");
        setUpGetEntityExpectations(VdcQueryType.GetVmFromConfiguration,
                GetVmFromConfigurationQueryParameters.class,
                new String[] { "VmConfiguration", "ConfigurationType" },
                new Object[] { model.getInitialization().getConfiguration().getData(), ConfigurationType.OVF},
                returnedVM);
        setUpCreationExpectations(VdcActionType.ImportVmFromConfiguration,
                ImportVmParameters.class,
                new String[] { "Vm", "VdsGroupId", "ImportAsNewEntity"},
                new Object[] { returnedVM, Guid.createGuidFromString(model.getCluster().getId()), false},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                returnedVM);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddFromConfigurationNamedCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[]{2});
        setUpGetRngDeviceExpectations(new int[]{2});
        Vm model = createModel(null);
        org.ovirt.engine.core.common.businessentities.VM returnedVM = getEntity(2);
        model.setInitialization(new Initialization());
        model.getInitialization().setConfiguration(new Configuration());
        model.getInitialization().getConfiguration().setData("asdasdasd");
        model.getInitialization().getConfiguration().setType("ovf");
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[1]);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpVDSGroup(GUIDS[1]));
        setUpGetEntityExpectations(VdcQueryType.GetVmFromConfiguration,
                GetVmFromConfigurationQueryParameters.class,
                new String[] { "VmConfiguration", "ConfigurationType" },
                new Object[] { model.getInitialization().getConfiguration().getData(), ConfigurationType.OVF},
                returnedVM);
        setUpCreationExpectations(VdcActionType.ImportVmFromConfiguration,
                ImportVmParameters.class,
                new String[] { "Vm", "VdsGroupId" },
                new Object[] { returnedVM, GUIDS[1] },
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                returnedVM);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddFromConfigurationCantDo() throws Exception {
        testBadAddFromConfiguration(false, true, CANT_DO);
    }

    @Test
    public void testAddFromConfigurationFailure() throws Exception {
        testBadAddFromConfiguration(true, false, FAILURE);
    }

    private void testBadAddFromConfiguration(boolean canDo, boolean success, String detail)
            throws Exception {
        Vm model = createModel(null);
        org.ovirt.engine.core.common.businessentities.VM returnedVM = getEntity(2);
        model.setInitialization(new Initialization());
        model.getInitialization().setConfiguration(new Configuration());
        model.getInitialization().getConfiguration().setData("asdasdasd");
        model.getInitialization().getConfiguration().setType("ovf");
        setUpGetEntityExpectations(VdcQueryType.GetVmFromConfiguration,
                GetVmFromConfigurationQueryParameters.class,
                new String[] { "VmConfiguration", "ConfigurationType" },
                new Object[] { model.getInitialization().getConfiguration().getData(), ConfigurationType.OVF},
                returnedVM);
        setUriInfo(setUpActionExpectations(VdcActionType.ImportVmFromConfiguration,
                ImportVmParameters.class,
                new String[] { "Vm", "VdsGroupId" },
                new Object[] { returnedVM, Guid.createGuidFromString(model.getCluster().getId())},
                canDo,
                success));
        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void doTestBadAddFromConfigurationMissingParameters() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Vm model = createModel(null);
        model.setInitialization(new Initialization());
        model.getInitialization().setConfiguration(new Configuration());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "add", "initialization.configuration.type", "initialization.configuration.data");
        }
    }

    @Test
    public void testAddWithPlacementPolicySingleHostName() throws Exception {
        setUpAddVm();
        setUpGetHostByNameExpectations(1);
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Vm model = createModel(null);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHosts(new Hosts());
        model.getPlacementPolicy().getHosts().getHosts().add(new Host());
        model.getPlacementPolicy().getHosts().getHosts().get(0).setName(NAMES[1]);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddWithPlacementPolicySingleHostId() throws Exception {
        setUpAddVm();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Vm model = createModel(null);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHosts(new Hosts());
        model.getPlacementPolicy().getHosts().getHosts().add(new Host());
        model.getPlacementPolicy().getHosts().getHosts().get(0).setId(GUIDS[1].toString());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddWithPlacementPolicyHostsIds() throws Exception {
        setUpAddVm();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Vm model = createModel(null);
        model.setPlacementPolicy(new VmPlacementPolicy());
        Hosts hosts = new Hosts();
        for (int i =0; i < GUIDS.length; i++){
            Host newHost = new Host();
            newHost.setId(GUIDS[i].toString());
            hosts.getHosts().add(newHost);
        }
        model.getPlacementPolicy().setHosts(hosts);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddWithPlacementPolicyHostsNames() throws Exception {
        setUpAddVm();
        for (int i =0; i < NAMES.length; i++){
            setUpGetHostByNameExpectations(i);
        }
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[]{"StorageDomainId"},
                new Object[]{GUIDS[0]},
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[2]},
                getEntity(2));

        Vm model = createModel(null);
        model.setPlacementPolicy(new VmPlacementPolicy());
        Hosts hosts = new Hosts();
        for (int i =0; i < NAMES.length; i++){
            Host newHost = new Host();
            newHost.setName(NAMES[i].toString());
            hosts.getHosts().add(newHost);
        }
        model.getPlacementPolicy().setHosts(hosts);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    private void setUpAddVm() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpGetCertuficateExpectations(1, 2);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[1]},
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());
    }

    private VdsStatic getStaticHost() {
        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(GUIDS[2]);
        return vdsStatic;
    }

    @Test
    public void testAddWithStorageDomain() throws Exception {
        setUpAddVm();
        setUpCreationExpectations(VdcActionType.AddVm,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Vm model = createModel(null);
        addStorageDomainToModel(model);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddNamedCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpGetCertuficateExpectations(1, 2);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                setUpVDSGroup(GUIDS[2]));

        setUpCreationExpectations(VdcActionType.AddVm,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Vm model = getModel(2);
        model.setTemplate(new Template());
        model.getTemplate().setId(GUIDS[1].toString());
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[2]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddWithClonePermissionsDontClone() throws Exception {
        doTestAddWithClonePermissions(createModel(null), false);
    }

    @Test
    public void testAddWithClonePermissionsClone() throws Exception {
        Vm model = createModel(null);
        model.setPermissions(new Permissions());
        model.getPermissions().setClone(true);

        doTestAddWithClonePermissions(model, true);
    }

    private void doTestAddWithClonePermissions(Vm model, boolean copy) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);
        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[2] },
                                     getVdsGroupEntity());
        setUpCreationExpectations(VdcActionType.AddVm,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId", "CopyTemplatePermissions" },
                                  new Object[] { GUIDS[0], copy },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testCloneFromTemplateWithClonePermissionsDontClone() throws Exception {
        doTestCloneFromTemplateWithClonePermissions(createModel(createDisksCollection()), false);
    }

    @Test
    public void testCloneFromTemplateWithClonePermissionsClone() throws Exception {
        Vm model = createModel(createDisksCollection());
        model.setPermissions(new Permissions());
        model.getPermissions().setClone(true);
        doTestCloneFromTemplateWithClonePermissions(model, true);
    }

    private void doTestCloneFromTemplateWithClonePermissions(Vm model, boolean copy) throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendVmsResource.CLONE, "true"));
        setUpTemplateDisksExpectations(GUIDS[1]);
        setUpGetPayloadExpectations(1, 2);
        setUpGetBallooningExpectations(1, 2);
        setUpGetGraphicsExpectations(1);
        setUpGetCertuficateExpectations(1, 2);

        setUpGetConsoleExpectations(new int[]{2});
        setUpGetVmOvfExpectations(new int[]{2});
        setUpGetVirtioScsiExpectations(new int[]{2});
        setUpGetSoundcardExpectations(new int[] { 1, 2 });
        setUpGetRngDeviceExpectations(new int[] { 1, 2 });
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[]{"Id"},
                                     new Object[]{GUIDS[1]},
                getTemplateEntity(1));
                                     setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                                     IdQueryParameters.class,
                                     new String[]{"Id"},
                                     new Object[]{GUIDS[2]},
                                     getVdsGroupEntity());

        setUpCreationExpectations(VdcActionType.AddVmFromTemplate,
                                  AddVmParameters.class,
                                  new String[] { "StorageDomainId", "CopyTemplatePermissions" },
                                  new Object[] { GUIDS[0], copy },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailed() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetGraphicsExpectations(3);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListAllContentIsConsolePopulated() throws Exception {
        testListAllConsoleAware(true);
    }

    @Test
    public void testListAllContentIsNotConsolePopulated() throws Exception {
        testListAllConsoleAware(false);
    }

    private void testListAllConsoleAware(boolean allContent) throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpGetGraphicsExpectations(3);
        if (allContent) {
            List<String> populates = new ArrayList<String>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetPayloadExpectations(3);
            setUpGetBallooningExpectations(3);
            setUpGetConsoleExpectations(new int[]{0, 1, 2});
            setUpGetVmOvfExpectations(new int[]{0, 1, 2});
            setUpGetVirtioScsiExpectations(new int[]{0, 1, 2});
            setUpGetSoundcardExpectations(new int[]{0, 1, 2});
            setUpGetRngDeviceExpectations(new int[]{0, 1, 2});
            setUpGetCertuficateExpectations(3);
        }

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListAllContent() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        List<String> populates = new ArrayList<String>();
        populates.add("true");
        expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
        setUpGetPayloadExpectations(3);
        setUpGetBallooningExpectations(3);
        setUpGetGraphicsExpectations(3);
        setUpGetConsoleExpectations(new int[]{0, 1, 2});
        setUpGetVmOvfExpectations(new int[]{0, 1, 2});
        setUpGetVirtioScsiExpectations(new int[]{0, 1, 2});
        setUpGetSoundcardExpectations(new int[]{0, 1, 2});
        setUpGetRngDeviceExpectations(new int[]{0, 1, 2});
        setUpGetCertuficateExpectations(3);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    private VmPayload getPayloadModel() {
        VmPayload payload = new VmPayload();
        payload.setDeviceType(VmDeviceType.CDROM);
        payload.getFiles().put("payloadFile", new String(Base64.decodeBase64(PAYLOAD_COMTENT)));
        return payload;
    }

    private void setUpGetCertuficateExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsCertificateSubjectByVmId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[i] },
                    CERTIFICATE);
        }
    }

    private void setUpGetCertuficateExpectations(int times, int index) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsCertificateSubjectByVmId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    CERTIFICATE);
        }
    }

    @Test
    @Override
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpGetGraphicsExpectations(3);
        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setUpGetSoundcardExpectations(new int[] { 1 });
        setUpGetRngDeviceExpectations(new int[] { 1 });

        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());
        setUriInfo(setUpActionExpectations(VdcActionType.AddVm,
                                           AddVmParameters.class,
                                           new String[] { "StorageDomainId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));

        try {
            collection.add(createModel(null));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

//    @Test
//    public void testIsConsoleAddedInList() {
//
//    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Vm model = new Vm();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "add", "cluster.id|name");
        }
    }

    @Test
    public void testAddIncompleteParameters2() throws Exception {
        Vm model = createModel(null);
        model.setTemplate(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "add", "template.id|name");
        }
    }

    @Test
    public void testAddUploadIcon() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setupAddExpectations();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[] { "StorageDomainId", "VmLargeIcon" },
                new Object[] { GUIDS[0],
                        VmIcon.typeAndDataToDataUrl(IconTestHelpler.MEDIA_TYPE, IconTestHelpler.DATA_URL) },
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getEntity(2));

        final Vm model = createModel(null);
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddUseExistingIcons() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getTemplateEntity(1));
        setupAddExpectations();
        setUpCreationExpectations(VdcActionType.AddVm,
                AddVmParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[2],
                VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getEntity(2));
        final Vm model = createModel(null);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIcon(GUIDS[3]));
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Vm);
        verifyModel((Vm) response.getEntity(), 2);
    }

    @Test
    public void testAddSetAndUploadIconFailure() throws Exception {
        control.replay();
        final Vm model = createModel(null);
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BAD_REQUEST);
        }
    }

    private void setUpTemplateDisksExpectations(Guid templateId) {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesDisks,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { templateId },
                                     createDiskList());
    }

    @SuppressWarnings("serial")
    private List<DiskImage> createDiskList() {
        return new ArrayList<DiskImage>(){{
                                            add(new DiskImage(){{setId(GUIDS[0]);}});
                                         }};
    }

    static org.ovirt.engine.core.common.businessentities.VM setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VM entity, VmStatistics statistics, int index, Guid vmId) {
        entity.setId(vmId);
        entity.setVdsGroupId(GUIDS[2]);
        entity.setName(NAMES[index]);
        entity.setVmDescription(DESCRIPTIONS[index]);
        entity.setCpuPerSocket(4);
        entity.setNumOfSockets(2);
        entity.setUsageMemPercent(20);
        entity.getGraphicsInfos().put(GraphicsType.VNC, new GraphicsInfo());
        entity.setNumOfMonitors(2);
        entity.setVmType(VmType.Server);
        entity.setRunOnVdsName(NAMES[NAMES.length - 1]);
        entity.setOrigin(index == 0 ? OriginType.HOSTED_ENGINE : OriginType.OVIRT);
        entity.setBootSequence(null);
        entity.getStaticData().setSmallIconId(GUIDS[2]);
        entity.getStaticData().setLargeIconId(GUIDS[3]);
        setUpStatisticalEntityExpectations(entity, statistics);
        return entity;
    }

    static org.ovirt.engine.core.common.businessentities.VM setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VM entity, VmStatistics statistics, int index) {
        return setUpEntityExpectations(entity, statistics, index, GUIDS[index]);
    }

    static org.ovirt.engine.core.common.businessentities.VmTemplate setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VmTemplate entity, int index) {
        entity.setId(GUIDS[index]);
        entity.setVdsGroupId(GUIDS[2]);
        entity.setName(NAMES[index]);
        entity.setDescription(DESCRIPTIONS[index]);
        entity.setCpuPerSocket(4);
        entity.setNumOfSockets(2);
        entity.setDefaultDisplayType(DisplayType.cirrus);
        entity.setNumOfMonitors(2);
        entity.setVmType(VmType.Server);
        return entity;
    }

    static org.ovirt.engine.core.common.businessentities.VM setUpStatisticalEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VM entity, VmStatistics statistics) {
        entity.setVmMemSizeMb(10);
        entity.setStatisticsData(statistics);
        statistics.setusage_mem_percent(20);
        statistics.setcpu_user(30.0);
        statistics.setcpu_sys(40.0);
        statistics.setusage_cpu_percent(50);
        entity.setMigrationProgressPercent(50);
        entity.setGuestMemoryFree(5120L);
        entity.setGuestMemoryBuffered(2048L);
        entity.setGuestMemoryCached(1024L);
        return entity;
    }

    static Vm getModel(int index) {
        Vm model = new Vm();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setId(GUIDS[index].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());
        return model;
    }

    @Override
    protected List<Vm> getCollection() {
        return collection.list().getVms();
    }

    @Override
    protected void verifyCollection(List<Vm> collection) throws Exception {
        super.verifyCollection(collection);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.POPULATE);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;

        for (Vm vm : collection) {
            assertTrue(populated ? vm.isSetConsole() : !vm.isSetConsole());
        }
    }

    @Override
    protected void verifyModel(Vm model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    static void verifyModelSpecific(Vm model, int index) {
        assertNotNull(model.getCluster());
        assertNotNull(model.getCluster().getId());
        assertNotNull(model.getCpu());
        assertNotNull(model.getCpu().getTopology());
        assertEquals(4, model.getCpu().getTopology().getCores().intValue());
        assertEquals(2, model.getCpu().getTopology().getSockets().intValue());
        assertEquals(GUIDS[2].toString(), model.getSmallIcon().getId());
        assertEquals(GUIDS[3].toString(), model.getLargeIcon().getId());

    }

    private Vm createModel(Disks disks) {
        Vm model = getModel(2);

        model.setTemplate(new Template());
        model.getTemplate().setId(GUIDS[1].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());
        if(disks != null){
            model.setDisks(disks);
        }

        return model;
    }

    private Vm createModel(Disks disks, Snapshots snapshots) {
        Vm model = createModel(disks);
        if (snapshots != null) {
            model.setSnapshots(snapshots);
        }
        return model;
    }

    private void addStorageDomainToModel(Vm model) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[1].toString());
        model.setStorageDomain(storageDomain);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        return setUpEntityExpectations(vm, vm.getStatisticsData(), index);
    }

    protected org.ovirt.engine.core.common.businessentities.VM getEntityWithProvidedId(int index, Guid vmId) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        return setUpEntityExpectations(vm, vm.getStatisticsData(), index, vmId);
    }

    protected org.ovirt.engine.core.common.businessentities.VmTemplate getTemplateEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VmTemplate template = new org.ovirt.engine.core.common.businessentities.VmTemplate();
        return setUpEntityExpectations(template, index);
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        VDSGroup cluster = new VDSGroup();
        cluster.setStoragePoolId(GUIDS[3]);
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        return cluster;
    }

    private Disks createDisksCollection() {
        Disks disks = new Disks();
        disks.getDisks().add(map(createDiskList().get(0), null));
        return disks;
    }

    private Snapshots createSnapshotsCollection(int index) {
        Snapshots snapshots = new Snapshots();
        snapshots.getSnapshots().add(map(createSnapshot(index), null));
        return snapshots;
    }

    private org.ovirt.engine.core.common.businessentities.Snapshot createSnapshot(int index) {
        org.ovirt.engine.core.common.businessentities.Snapshot result =
                new org.ovirt.engine.core.common.businessentities.Snapshot();
        result.setId(GUIDS[index]);
        result.setDescription("snap1");
        return result;
    }

    private Disk map(DiskImage entity, Disk template) {
        return getMapper(org.ovirt.engine.core.common.businessentities.storage.Disk.class, Disk.class).map(entity, template);
    }

    private Snapshot map(org.ovirt.engine.core.common.businessentities.Snapshot entity, Snapshot template) {
        return getMapper(org.ovirt.engine.core.common.businessentities.Snapshot.class, Snapshot.class).map(entity,
                template);
    }

    private ArrayList<DiskImageBase> mapDisks(Disks disks) {
        ArrayList<DiskImageBase> diskImages = null;
        if (disks!=null && disks.isSetDisks()) {
            diskImages = new ArrayList<DiskImageBase>();
            for (Disk disk : disks.getDisks()) {
                DiskImage diskImage = (DiskImage)DiskMapper.map(disk, null);
                diskImages.add(diskImage);
            }
        }
        return diskImages;
    }

    protected void setUpGetPayloadExpectations(int times) throws Exception {
        VmPayload payload = new VmPayload();
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVmPayload,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[i] },
                    payload);
        }
    }

    protected void setUpGetPayloadExpectations(int times, int index) throws Exception {
        VmPayload payload = new VmPayload();
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVmPayload,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    payload);
        }
    }

    private void setUpGetBallooningExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.IsBalloonEnabled,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[i] },
                    true);
        }
    }

    private void setUpGetBallooningExpectations(int times, int index) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.IsBalloonEnabled,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    true);
        }
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

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        // If the query to retrieve the virtual machines succeeds, then we will run another query to add the
        // initialization information:
        if (failure == null) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmsInit,
                IdsQueryParameters.class,
                new String[]{},
                new Object[]{},
                setUpVmInit()
            );
        }

        // Add the default expectations:
        super.setUpQueryExpectations(query, failure);
    }

    protected void setUpGetGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[] {},
                    new Object[] {},
                    Arrays.asList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    private List<VmInit> setUpVmInit() {
        List<VmInit> vminits = new ArrayList<>(NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            VmInit vmInit = control.createMock(VmInit.class);
            vminits.add(vmInit);
        }
        return vminits;
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
