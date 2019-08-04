package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendStorageDomainTemplateResource(null, "foo")));
    }

    @Test
    public void testGetExportNotFound() {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, true);
        setUriInfo(setUpBasicUriExpectations());
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGetExport() {
        testGet(StorageDomainType.ImportExport);
    }

    protected void testGet(StorageDomainType domainType) {
        setUpGetStorageDomainExpectations(domainType);
        setUpGetEntityExpectations(domainType, STORAGE_DOMAIN_ID);
        setUriInfo(setUpBasicUriExpectations());

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testImportNotFound() {
        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[2].toString());
        action.setCluster(new org.ovirt.engine.api.model.Cluster());
        action.getCluster().setId(GUIDS[1].toString());
        setUpGetEntityExpectations(StorageDomainType.ImportExport, GUIDS[2], true);
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.doImport(action)));
    }

    @Test
    public void testRegisterTemplate() {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, false);
    }

    @Test
    public void testRegisterTemplateAsNewEntity() {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestRegister(cluster, true);
    }

    public void doTestRegister(org.ovirt.engine.api.model.Cluster cluster, boolean importAsNewEntity) {
        setUriInfo(setUpActionExpectations(ActionType.ImportVmTemplateFromConfiguration,
                ImportVmTemplateFromConfParameters.class,
                new String[] { "ContainerId", "StorageDomainId", "ClusterId", "ImportAsNewEntity",
                        "ImagesExistOnTargetStorageDomain" },
                new Object[] { TEMPLATE_ID, GUIDS[3], GUIDS[1], importAsNewEntity, true }));

        Action action = new Action();
        action.setCluster(cluster);
        action.setClone(importAsNewEntity);
        verifyActionResponse(resource.register(action));
    }

    @Test
    public void testImport() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, false);
    }

    @Test
    public void testImportWithStorageDomainName() {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainByName,
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
    public void testImportWithClusterName() {
        setUpEntityQueryExpectations(QueryType.GetClusterByName,
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
    public void testImportAsNewEntity() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster, true);
    }

    @Test
    public void testRemove() {
        setUpQueryExpectations("", null, StorageDomainType.ImportExport);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUpGetUnregisteredVmTemplateExpectations(true);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVmTemplateFromImportExport,
                VmTemplateImportExportParameters.class,
                new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                new Object[] { GUIDS[1], GUIDS[3], GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveUnregisteredTemplate() {
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUpGetUnregisteredVmTemplateExpectations(false);
        setUriInfo(setUpActionExpectations(ActionType.RemoveUnregisteredVmTemplate,
                RemoveUnregisteredEntityParameters.class,
                new String[] { "EntityId", "StorageDomainId", "StoragePoolId" },
                new Object[] { GUIDS[1], GUIDS[3], GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetUnregisteredVmTemplateExpectations(true);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVmTemplateFromImportExport,
                VmTemplateImportExportParameters.class,
                new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                new Object[] { GUIDS[1], GUIDS[3], GUIDS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id) {
        setUpGetDataCenterByStorageDomainExpectations(id, 1);
    }

    public void doTestImport(StorageDomain storageDomain, org.ovirt.engine.api.model.Cluster cluster, boolean importAsNewEntity) {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);
        setUriInfo(setUpActionExpectations(ActionType.ImportVmTemplate,
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
    public void testImportAsyncPending() {
        doTestImportAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testImportAsyncInProgress() {
        doTestImportAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testImportAsyncFinished() {
        doTestImportAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestImportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3]);

        setUriInfo(setUpActionExpectations(
                ActionType.ImportVmTemplate,
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
    public void testIncompleteImport() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.doImport(new Action())),
                "Action", "doImport", "storageDomain.id|name");
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType) {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));
    }

    protected void setUpGetEntityExpectations(int times, StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) {
        while (times-- > 0) {
            setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId);
        }
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound) {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(QueryType.GetVmTemplate,
                                         GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { TEMPLATE_ID },
                                         notFound ? null : getEntity(1));
            break;
        case ImportExport:
            setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool());
            setUpEntityQueryExpectations(QueryType.GetTemplatesFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpTemplates(notFound));
            break;
        default:
            break;
        }
    }

    private void setUpGetUnregisteredVmTemplateExpectations(boolean notFound) {
        setUpEntityQueryExpectations(
                QueryType.GetUnregisteredVmTemplate,
                GetUnregisteredEntityQueryParameters.class,
                new String[] { "StorageDomainId", "EntityId" },
                new Object[] { STORAGE_DOMAIN_ID, TEMPLATE_ID },
                notFound ? null : getEntity(1));
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
        String uri = URL_BASE + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, URL_BASE, false);
    }

    @Override
    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    protected Map<VmTemplate, List<DiskImage>> setUpTemplates(boolean notFound) {
        if (notFound) {
            return Collections.emptyMap();
        }
        return IntStream.range(0, NAMES.length).boxed().collect(Collectors.toMap(this::getEntity, ArrayList::new));
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

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { STORAGE_DOMAIN_ID },
                setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(QueryType.GetVmTemplatesFromStorageDomain,
                    GetVmTemplatesFromStorageDomainParameters.class,
                    new String[] { "Id" },
                    new Object[] { STORAGE_DOMAIN_ID },
                    setUpTemplates(),
                    failure);
            break;
        case ImportExport:
            setUpEntityQueryExpectations(QueryType.GetTemplatesFromExportDomain,
                    GetAllFromExportDomainQueryParameters.class,
                    new String[] { "StoragePoolId", "StorageDomainId" },
                    new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                    setUpExportTemplates(),
                    failure);
            break;
        default:
            break;
        }
    }

    protected List<VmTemplate> setUpTemplates() {
        return IntStream.range(0, NAMES.length).mapToObj(this::getEntity).collect(Collectors.toList());
    }

    protected Map<VmTemplate, List<DiskImage>> setUpExportTemplates() {
        return IntStream.range(0, NAMES.length)
                .boxed()
                .collect(Collectors.toMap(this::getEntity, ArrayList::new, (u, v) -> null, LinkedHashMap::new));
    }
}
