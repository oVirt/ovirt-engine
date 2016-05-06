package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplateResourceTest
    extends AbstractBackendSubResourceTest<Template,
                                           VmTemplate,
                                           BackendStorageDomainTemplateResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    private static final String URL_BASE = "storagedomains/" + STORAGE_DOMAIN_ID + "/templates/" + TEMPLATE_ID;

    public BackendStorageDomainTemplateResourceTest() {
        super(new BackendStorageDomainTemplateResource(new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID),
                                                       TEMPLATE_ID.toString()));
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
            new BackendStorageDomainTemplateResource(null, "foo");
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
        setUpGetEntityExpectations(StorageDomainType.ImportExport, GUIDS[2], true);
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        control.replay();
        try {
            resource.doImport(action);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRegisterTemplate() throws Exception {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, false);
    }

    @Test
    public void testRegisterTemplateAsNewEntity() throws Exception {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, true);
    }

    public void doTestRegister(org.ovirt.engine.api.model.Cluster cluster, boolean importAsNewEntity) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ImportVmTemplateFromConfiguration,
                                           ImportVmTemplateParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ClusterId", "ImportAsNewEntity", "ImagesExistOnTargetStorageDomain"},
                                           new Object[] { TEMPLATE_ID, GUIDS[3], GUIDS[1], importAsNewEntity, true }));

        Action action = new Action();
        action.setCluster(cluster);
        action.setClone(importAsNewEntity);
        verifyActionResponse(resource.register(action));
    }

    @Test
    public void testImport() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, false);
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
        doTestImport(storageDomain, cluster, false);
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
        doTestImport(storageDomain, cluster, false);
    }

    @Test
    public void testImportAsNewEntity() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, true);
    }

    @Test
    public void testRemove() throws Exception {
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplateFromImportExport,
                VmTemplateImportExportParameters.class,
                new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                new Object[] { GUIDS[1], GUIDS[3], GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
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
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplateFromImportExport,
                VmTemplateImportExportParameters.class,
                new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                new Object[] { GUIDS[1], GUIDS[3], GUIDS[0] },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-->0) {
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

    public void doTestImport(StorageDomain storageDomain, org.ovirt.engine.api.model.Cluster cluster, boolean importAsNewEntity) throws Exception {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);
        setUriInfo(setUpActionExpectations(VdcActionType.ImportVmTemplate,
                                           ImportVmTemplateParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "ClusterId", "ImportAsNewEntity" },
                                           new Object[] { TEMPLATE_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1], importAsNewEntity }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);
        action.setClone(importAsNewEntity);
        verifyActionResponse(resource.doImport(action));
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
                VdcActionType.ImportVmTemplate,
                ImportVmTemplateParameters.class,
                new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "ClusterId" },
                new Object[] { TEMPLATE_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1] },
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
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                         GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { TEMPLATE_ID },
                                         notFound ? null : getEntity(1));
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool());
            setUpEntityQueryExpectations(VdcQueryType.GetTemplatesFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpTemplates(notFound));
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
    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    protected HashMap<VmTemplate, List<DiskImage>> setUpTemplates(boolean notFound) {
        HashMap<VmTemplate, List<DiskImage>> ret = new HashMap<>();
        if (notFound) {
            return ret;
        }
        for (int i = 0; i < NAMES.length; i++) {
            ret.put(getEntity(i), new ArrayList<>());
        }
        return ret;
    }

    @Override
    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
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

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType, boolean replay)
            throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { STORAGE_DOMAIN_ID },
                setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesFromStorageDomain,
                    GetVmTemplatesFromStorageDomainParameters.class,
                    new String[] { "Id" },
                    new Object[] { STORAGE_DOMAIN_ID },
                    setUpTemplates(),
                    failure);
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetTemplatesFromExportDomain,
                    GetAllFromExportDomainQueryParameters.class,
                    new String[] { "StoragePoolId", "StorageDomainId" },
                    new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                    setUpExportTemplates(),
                    failure);
            break;
        default:
            break;
        }

        if (replay) {
            control.replay();
        }
    }

    protected List<VmTemplate> setUpTemplates() {
        List<VmTemplate> ret = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
    }

    protected HashMap<VmTemplate, List<DiskImage>> setUpExportTemplates() {
        HashMap<VmTemplate, List<DiskImage>> ret = new LinkedHashMap<>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.put(getEntity(i), new ArrayList<>());
        }
        return ret;
    }

}
