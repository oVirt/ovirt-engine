package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.types.DiskMapper;

import static org.easymock.EasyMock.expect;

public class BackendVmsResourceTest
        extends AbstractBackendCollectionResourceTest<VM, org.ovirt.engine.core.common.businessentities.VM, BackendVmsResource> {

    private static final String DEFAULT_TEMPLATE_ID = Guid.Empty.toString();

    public BackendVmsResourceTest() {
        super(new BackendVmsResource(), SearchType.VM, "VMs : ");
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
            for (int i=0; i<GUIDS.length-1; i++) {
                setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                        GetVmByVmIdParameters.class,
                        new String[] { "Id" },
                        new Object[] { GUIDS[i] },
                        vm);
            }
            setUpQueryExpectations("");
            collection.setUriInfo(uriInfo);
            List<VM> vms = getCollection();
            assertTrue(vms.get(0).isSetStatistics());
            verifyCollection(vms);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.RemoveVm, RemoveVmParameters.class, new String[] {
                "VmId", "Force" }, new Object[] { GUIDS[0], Boolean.FALSE }, true, true);
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveForced() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.RemoveVm, RemoveVmParameters.class, new String[] {
            "VmId", "Force" }, new Object[] { GUIDS[0], Boolean.TRUE }, true, true);
        verifyRemove(collection.remove(GUIDS[0].toString(), new Action(){{setForce(true);}}));
    }

    @Test
    public void testRemoveForcedIncomplete() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.RemoveVm, RemoveVmParameters.class, new String[] {
                                "VmId", "Force" }, new Object[] { GUIDS[0], Boolean.FALSE }, true, true);
        verifyRemove(collection.remove(GUIDS[0].toString(), new Action(){{}}));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                GetVmByVmIdParameters.class,
                new String[] { "Id" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                GetVmByVmIdParameters.class,
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

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVm,
                                           RemoveVmParameters.class,
                                           new String[] { "VmId", "Force" },
                                           new Object[] { GUIDS[0], Boolean.FALSE },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
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
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getTemplateEntity(0));
        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                                  AddVmFromScratchParameters.class,
                                  new String[] { "StorageDomainId"},
                                  new Object[] { Guid.Empty},
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(asyncStatus)),
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        VM model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM)response.getEntity(), 0);
        VM created = (VM)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus().getState());
    }

    @Test
    public void testAddFromScratch() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getTemplateEntity(0));

        Disks disks = new Disks();
        disks.getDisks().add(new Disk());
        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                                  AddVmFromScratchParameters.class,
                                  new String[] { "StorageDomainId", "DiskInfoList" },
                                  new Object[] { Guid.Empty, mapDisks(disks) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        VM model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);
        model.setDisks(disks);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 0);
        assertNull(((VM)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddFromScratchWithStorageDomain() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getTemplateEntity(0));
        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                                  AddVmFromScratchParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        VM model = getModel(0);
        addStorageDomainToModel(model);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 0);
        assertNull(((VM)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddFromScratchNamedCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetEntityExpectations("Cluster: name=" + NAMES[1],
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[1]));
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getEntity(0));
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVmFromScratch,
                                  AddVmFromScratchParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { Guid.Empty },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[1]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        VM model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[1]);
        model.setTemplate(new Template());
        model.getTemplate().setId(DEFAULT_TEMPLATE_ID);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 0);
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
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmFromScratch,
                                           AddVmFromScratchParameters.class,
                                           new String[] { "StorageDomainId" },
                                           new Object[] { Guid.Empty },
                                           canDo,
                                           success));
        VM model = getModel(0);
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
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainsExpectations(0, 1);
        setUpTemplateDisksExpectations(GUIDS[1]);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVmFromTemplate,
                                  AddVmFromTemplateParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Response response = collection.add(createModel(createDisksCollection()));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    @Test
    public void testClone() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainsExpectations(0, 1);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVmFromTemplate,
                                  AddVmFromTemplateParameters.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Response response = collection.add(createModel(new Disks(){{setClone(true);}}));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainsExpectations(0, 1);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));
        setUpCreationExpectations(VdcActionType.AddVm,
                                  VmManagementParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        Response response = collection.add(createModel(null));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    @Test
    public void testAddWithPlacementPolicy() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainsExpectations(0, 1);
        setUpGetEntityExpectations("Hosts: name=" + NAMES[1],
                SearchType.VDS,
                getHost());
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));
        setUpCreationExpectations(VdcActionType.AddVm,
                                  VmManagementParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        VM model = createModel(null);
        model.setPlacementPolicy(new VmPlacementPolicy());
        model.getPlacementPolicy().setHost(new Host());
        model.getPlacementPolicy().getHost().setName(NAMES[1]);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    private VDS getHost() {
        VDS vds = new VDS();
        vds.setvds_id(GUIDS[2]);
        return vds;
    }

    @Test
    public void testAddWithStorageDomain() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));

        setUpCreationExpectations(VdcActionType.AddVm,
                                  VmManagementParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        VM model = createModel(null);
        addStorageDomainToModel(model);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    @Test
    public void testAddNamedCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpStorageDomainsExpectations(0, 1);

        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));

        setUpGetEntityExpectations("Cluster: name=" + NAMES[2],
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[2]));

        setUpCreationExpectations(VdcActionType.AddVm,
                                  VmManagementParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  VdcQueryType.GetVmByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[2] },
                                  getEntity(2));

        VM model = getModel(2);
        model.setTemplate(new Template());
        model.getTemplate().setId(GUIDS[1].toString());
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[2]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VM);
        verifyModel((VM) response.getEntity(), 2);
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailed() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpStorageDomainsExpectations(0, 1);
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                     GetVmTemplateParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     getTemplateEntity(0));
        setUriInfo(setUpActionExpectations(VdcActionType.AddVm,
                                           VmManagementParametersBase.class,
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

    @Test
    public void testAddIncompleteParameters() throws Exception {
        VM model = new VM();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "VM", "add", "template.id|name", "cluster.id|name");
        }
    }

    private void setUpStorageDomainsExpectations(int storageIndex, int templateIndex) {
        storage_domains domain = control.createMock(storage_domains.class);
        expect(domain.getid()).andReturn(GUIDS[storageIndex]).anyTimes();

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainsByVmTemplateId,
                                     GetStorageDomainsByVmTemplateIdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[templateIndex] },
                                     domain);
    }

    private void setUpTemplateDisksExpectations(Guid templateId) {
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesDisks,
                                     GetVmTemplatesDisksParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { templateId },
                                     createDiskList());
    }

    @SuppressWarnings("serial")
    private List<DiskImage> createDiskList() {
        return new ArrayList<DiskImage>(){{
                                            add(new DiskImage(){{setId(GUIDS[0]);
                                                                 setinternal_drive_mapping("1");}});
                                         }};
    }

    static org.ovirt.engine.core.common.businessentities.VM setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VM entity, VmStatistics statistics, int index) {
        expect(entity.getvm_guid()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getvds_group_id()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getvm_name()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getvm_description()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getnum_of_cpus()).andReturn(8).anyTimes();
        expect(entity.getnum_of_sockets()).andReturn(2).anyTimes();
        expect(entity.getusage_mem_percent()).andReturn(Integer.valueOf(20)).anyTimes();
        expect(entity.getdisplay_type()).andReturn(DisplayType.vnc).anyTimes();
        expect(entity.getdisplay_secure_port()).andReturn(5900).anyTimes();
        expect(entity.getnum_of_monitors()).andReturn(2).anyTimes();
        expect(entity.getvm_type()).andReturn(VmType.Server).anyTimes();
        expect(entity.getrun_on_vds_name()).andReturn(NAMES[NAMES.length -1]).anyTimes();
        setUpStatisticalEntityExpectations(entity, statistics);
        return entity;
    }

    static org.ovirt.engine.core.common.businessentities.VmTemplate setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VmTemplate entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getvds_group_id()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getname()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getdescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getnum_of_cpus()).andReturn(8).anyTimes();
        expect(entity.getnum_of_sockets()).andReturn(2).anyTimes();
        expect(entity.getdefault_display_type()).andReturn(DisplayType.vnc).anyTimes();
        expect(entity.getnum_of_monitors()).andReturn(2).anyTimes();
        expect(entity.getvm_type()).andReturn(VmType.Server).anyTimes();
        return entity;
    }

    static org.ovirt.engine.core.common.businessentities.VM setUpStatisticalEntityExpectations(
            org.ovirt.engine.core.common.businessentities.VM entity, VmStatistics statistics) {
        expect(entity.getmem_size_mb()).andReturn(10).anyTimes();
        expect(entity.getStatisticsData()).andReturn(statistics).anyTimes();
        expect(statistics.getusage_mem_percent()).andReturn(20).anyTimes();
        expect(statistics.getcpu_user()).andReturn(Double.valueOf(30L)).anyTimes();
        expect(statistics.getcpu_sys()).andReturn(Double.valueOf(40L)).anyTimes();
        expect(statistics.getusage_cpu_percent()).andReturn(50).anyTimes();
        return entity;
    }

    static VM getModel(int index) {
        VM model = new VM();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    protected List<VM> getCollection() {
        return collection.list().getVMs();
    }

    protected void verifyModel(VM model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    static void verifyModelSpecific(VM model, int index) {
        assertNotNull(model.getCluster());
        assertNotNull(model.getCluster().getId());
        assertNotNull(model.getCpu());
        assertNotNull(model.getCpu().getTopology());
        assertEquals(4, model.getCpu().getTopology().getCores());
        assertEquals(2, model.getCpu().getTopology().getSockets());
    }

    private VM createModel(Disks disks) {
        VM model = getModel(2);

        model.setTemplate(new Template());
        model.getTemplate().setId(GUIDS[1].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());
        if(disks != null){
            model.setDisks(disks);
        }

        return model;
    }

    private void addStorageDomainToModel(VM model) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[1].toString());
        model.setStorageDomain(storageDomain);
    }

    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(org.ovirt.engine.core.common.businessentities.VM.class),
                control.createMock(VmStatistics.class),
                index);
    }

    protected org.ovirt.engine.core.common.businessentities.VmTemplate getTemplateEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(org.ovirt.engine.core.common.businessentities.VmTemplate.class),
                index);
    }

    private Disks createDisksCollection() {
        Disks disks = new Disks();
        disks.setClone(true);
        disks.getDisks().add(map(createDiskList().get(0), null));
        return disks;
    }

    private Disk map(DiskImage entity, Disk template) {
        return getMapper(DiskImage.class, Disk.class).map(entity, template);
    }

    private ArrayList<DiskImageBase> mapDisks(Disks disks) {
        ArrayList<DiskImageBase> diskImages = null;
        if (disks!=null && disks.isSetDisks()) {
            diskImages = new ArrayList<DiskImageBase>();
            for (Disk disk : disks.getDisks()) {
                DiskImage diskImage = DiskMapper.map(disk, null);
                diskImages.add(diskImage);
            }
        }
        return diskImages;
    }
}
