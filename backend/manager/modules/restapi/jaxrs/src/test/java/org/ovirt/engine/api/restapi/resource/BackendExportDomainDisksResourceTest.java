package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.UriInfo;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExportDomainDisksResourceTest
    extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendExportDomainDisksResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendExportDomainDisksResourceTest() {
        super(new BackendExportDomainDisksResource(
                    new BackendStorageDomainTemplateResource(
                        new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID),
                        TEMPLATE_ID.toString())), SearchType.Disk, "Disks : ");
    }

    @Override
    protected void init() {
        super.init();
        initResource(collection);
        initParentResource();
    }

    private void initParentResource() {
        AbstractBackendResource parent = collection.getParent().getParent();
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        parent.setHttpHeaders(httpHeaders);
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        collection.setUriInfo(uriInfo);
        collection.getParent().getParent().setUriInfo(uriInfo);
    }

    @Override
    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        DiskImage entity = new DiskImage();
        entity.setId(GUIDS[index]);
        entity.setVolumeFormat(VolumeFormat.RAW);
        entity.setImageStatus(ImageStatus.OK);
        entity.setVolumeType(VolumeType.Sparse);
        entity.setShareable(false);
        entity.setPropagateErrors(PropagateErrors.On);
        return setUpStatisticalEntityExpectations(entity);    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        entity.setReadRate(1);
        entity.setReadOps(2L);
        entity.setWriteRate(3);
        entity.setWriteOps(4L);
        entity.setReadLatency(5.0);
        entity.setWriteLatency(6.0);
        entity.setFlushLatency(7.0);
        return entity;
    }

    @Override
    protected void verifyModel(Disk model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertFalse(model.isSetVm());
        assertTrue(model.isSparse());
        assertTrue(model.isPropagateErrors());
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport, null);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, failure);
        setUriInfo(setUpBasicUriExpectations());
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType),
                                     failure);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, Object failure) {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false, failure);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound, Object failure) {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(QueryType.GetVmTemplate,
                                         GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { TEMPLATE_ID },
                                         notFound ? null : getEntity(1),
                                         failure);
            break;
        case ImportExport:
            setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool(),
                                         null);
            setUpEntityQueryExpectations(QueryType.GetTemplatesFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpTemplates(notFound),
                                         failure);
            break;
        default:
            break;
        }
    }

    private Map<Guid, DiskImage> getDiskMap() {
        return IntStream.range(0, NAMES.length)
                .mapToObj(i -> (DiskImage) getEntity(i))
                .collect(Collectors.toMap(DiskImage::getId, Function.identity(), (u, v) -> null, LinkedHashMap::new));
    }

    @Override
    protected void setUpEntityQueryExpectations(QueryType query,
            Class<? extends QueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        if (failure == null) {
            queryResult.setReturnValue(queryReturn);
            when(backend.runQuery(eq(query),
                    eqParams(queryClass,
                            addSession(queryNames),
                            addSession(queryValues)))).thenReturn(queryResult);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String) failure);
                when(backend.runQuery(eq(query),
                        eqParams(queryClass,
                                addSession(queryNames),
                                addSession(queryValues)))).thenReturn(queryResult);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(query),
                        eqParams(queryClass,
                                addSession(queryNames),
                                addSession(queryValues)))).thenThrow((Exception) failure);
            }
        }
    }

    protected Map<VmTemplate, List<DiskImage>> setUpTemplates(boolean notFound) {
        if (notFound) {
            return Collections.emptyMap();
        }

        return IntStream.range(0, NAMES.length)
                .boxed().collect(Collectors.toMap(this::getVmTemplateEntity, ArrayList::new));
    }

    protected VmTemplate getVmTemplateEntity(int index) {
        VmTemplate vm = setUpEntityExpectations(mock(VmTemplate.class), index);
        when(vm.getDiskTemplateMap()).thenReturn(getDiskMap());
        return vm;
    }
}
