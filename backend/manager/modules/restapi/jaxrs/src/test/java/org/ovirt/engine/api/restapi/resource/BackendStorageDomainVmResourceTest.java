package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainVmsResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmResourceTest
    extends AbstractBackendSubResourceTest<Vm,
                                           org.ovirt.engine.core.common.businessentities.VM,
                                           BackendStorageDomainVmResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    private static final String URL_BASE = "storagedomains/" + STORAGE_DOMAIN_ID + "/vms/" + VM_ID;

    public BackendStorageDomainVmResourceTest() {
        super(new BackendStorageDomainVmResource(new BackendStorageDomainVmsResource(STORAGE_DOMAIN_ID),
                                                 VM_ID.toString()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.getParent().setUriInfo(uriInfo);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendStorageDomainVmResource(null, "foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetExportNotFound() throws Exception {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, true);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetExport() throws Exception {
        testGet(StorageDomainType.ImportExport);
    }

    protected void testGet(StorageDomainType domainType) throws Exception {
        setUpGetStorageDomainExpectations(domainType);
        setUpGetEntityExpectations(domainType, STORAGE_DOMAIN_ID);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testImportNotFound() throws Exception {
        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[2].toString());
        action.setCluster(new org.ovirt.engine.api.model.Cluster());
        action.getCluster().setId(GUIDS[1].toString());
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, true);
        control.replay();
        try {
            resource.doImport(action);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testImport() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, false, false);
    }

    @Test
    public void testImportCollapseSnapshots() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, true, false);
    }

    @Test
    public void testImportWithStorageDomainName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2));

        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestImport(storageDomain, cluster, false, false);
    }

    @Test
    public void testImportWithClusterName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                getCluster(1));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setName(NAMES[1]);
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, false, false);
    }

    @Test
    public void testImportAsNewEntity() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, false, true);
    }

    @Test
    public void testImportWithDiskWithoutId() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo,
                BackendStorageDomainVmResource.COLLAPSE_SNAPSHOTS,
                Boolean.toString(true));
        setUriInfo(uriInfo);

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);
        action.setClone(false);
        Vm vm = new Vm();
        DiskAttachments diskAttachments = new DiskAttachments();
        DiskAttachment diskAttachment = new DiskAttachment();
        diskAttachment.setDisk(new Disk());
        diskAttachments.getDiskAttachments().add(diskAttachment);
        vm.setDiskAttachments(diskAttachments);
        action.setVm(vm);

        control.replay();
        try {
            resource.doImport(action);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Disk", "setVolumesTypeFormat", "id");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        String[] names = new String[] { "VmId", "StorageDomainId", "StoragePoolId" };
        Object[] values = new Object[] { GUIDS[1], GUIDS[3], DATA_CENTER_ID };
        setUpActionExpectations(VdcActionType.RemoveVmFromImportExport,
                RemoveVmFromImportExportParameters.class,
                names,
                values,
                true,
                true);
        verifyRemove(resource.remove());
    }

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType, boolean replay) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetVmsFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId"},
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID},
                                         setUpVms(),
                                         failure);
            break;
        default:
            break;
        }

        if (replay) {
            control.replay();
        }
    }


    protected List<org.ovirt.engine.core.common.businessentities.VM> setUpVms() {
        List<org.ovirt.engine.core.common.businessentities.VM> ret = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id) {
        setUpGetDataCenterByStorageDomainExpectations(id, 1);
    }

    public void doTestImport(StorageDomain storageDomain, org.ovirt.engine.api.model.Cluster cluster, boolean collapseSnapshots, boolean importAsNewEntity) throws Exception {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.ImportVm,
            ImportVmParameters.class,
            new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "ClusterId", "CopyCollapse", "ImportAsNewEntity" },
            new Object[] { VM_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1], collapseSnapshots, importAsNewEntity },
            true, // valid,
            true, // success
            null, // taskReturn
            null, // baseUri
            false //replay
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendStorageDomainVmResource.COLLAPSE_SNAPSHOTS, Boolean.toString(collapseSnapshots));
        setUriInfo(uriInfo);
        control.replay();

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);
        action.setClone(importAsNewEntity);

        verifyActionResponse(resource.doImport(action));
    }

    public void doTestRegister(org.ovirt.engine.api.model.Cluster cluster, boolean importAsNewEntity) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ImportVmFromConfiguration,
                                           ImportVmParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ClusterId", "ImportAsNewEntity", "ImagesExistOnTargetStorageDomain"},
                                           new Object[] { VM_ID, GUIDS[3], GUIDS[1], importAsNewEntity, true}));

        Action action = new Action();
        action.setCluster(cluster);

        action.setClone(importAsNewEntity);

        verifyActionResponse(resource.register(action));
    }

    @Test
    public void testImportAsyncPending() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testImportAsyncInProgress() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testImportAsyncFinished() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestImportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3]);

        setUriInfo(setUpActionExpectations(
                VdcActionType.ImportVm,
                ImportVmParameters.class,
                new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "ClusterId" },
                new Object[] { VM_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1] },
                asList(GUIDS[1]),
                asList(new AsyncTaskStatus(asyncStatus))));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);

        Response response = resource.doImport(action);
        verifyActionResponse(response, URL_BASE, true, null);
        action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus());
    }

    @Test
    public void testIncompleteImport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.doImport(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "doImport", "cluster.id|name", "storageDomain.id|name");
        }
    }

    @Test
    public void testRegisterVM() throws Exception {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, false);
    }

    @Test
    public void testRegisterAsNewEntity() throws Exception {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, true);
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));
    }

    protected void setUpGetEntityExpectations(int times, StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId);
        }
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) throws Exception {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound) throws Exception {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { VM_ID },
                                         notFound ? null : getEntity(1));
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool());
            setUpEntityQueryExpectations(VdcQueryType.GetVmsFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpVms(notFound));
            break;
        default:
            break;
        }
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
        String uri = URL_BASE + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, URL_BASE, false);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        return setUpEntityExpectations(vm, vm.getStatisticsData(), index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.VM> setUpVms(boolean notFound) {
        List<org.ovirt.engine.core.common.businessentities.VM> ret =
                new ArrayList<>();
        if (notFound) {
            return ret;
        }
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
    }

    @Override
    protected void verifyModel(Vm model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStorageDomainStatic(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic dom =
                new org.ovirt.engine.core.common.businessentities.StorageDomainStatic();
        dom.setId(GUIDS[idx]);
        return dom;
    }

    protected Cluster getCluster(int idx) {
        Cluster cluster = new Cluster();
        cluster.setId(GUIDS[idx]);
        return cluster;
    }
}
