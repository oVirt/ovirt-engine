package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainTemplatesResourceTest
    extends AbstractBackendCollectionResourceTest<Template, VmTemplate, BackendStorageDomainTemplatesResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendStorageDomainTemplatesResourceTest() {
        super(new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID), null, null);
    }

    @Override
    @Test
    @Disabled
    public void testQuery() {
    }

    @Test
    @Override
    @Disabled
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Disabled
    public void testListFailure() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrash() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrashClientLocale() {

    }

    @Test
    public void testListExport() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 1);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpQueryExpectations(query, failure, StorageDomainType.Data);
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
                                         new String[] { "StoragePoolId", "StorageDomainId"},
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID},
                                         setUpExportTemplates(),
                                         failure);
            break;
        default:
            break;
        }
    }

    @Override
    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    protected List<VmTemplate> setUpTemplates() {
        return IntStream.range(0, NAMES.length).mapToObj(this::getEntity).collect(Collectors.toList());
    }

    protected Map<VmTemplate, List<DiskImage>> setUpExportTemplates() {
        return IntStream.range(0, NAMES.length).boxed().collect(
                Collectors.toMap(this::getEntity,
                        x -> new ArrayList<>(),
                        (u, v) -> null, // Should never happen, we have distinct entities
                        LinkedHashMap::new));
    }

    public static org.ovirt.engine.core.common.businessentities.StorageDomain setUpStorageDomain(StorageDomainType domainType) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setId(STORAGE_DOMAIN_ID);
        entity.setStorageDomainType(domainType);
        return entity;
    }

    public static List<StoragePool> setUpStoragePool() {
        final StoragePool entity = new StoragePool();
        entity.setId(DATA_CENTER_ID);
        return Collections.singletonList(entity);
    }

    @Override
    protected List<Template> getCollection() {
        return collection.list().getTemplates();
    }

    @Override
    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
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

}
