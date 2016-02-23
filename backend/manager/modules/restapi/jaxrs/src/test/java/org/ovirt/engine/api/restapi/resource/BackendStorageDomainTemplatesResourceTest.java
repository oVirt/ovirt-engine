package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplatesResourceTest
    extends AbstractBackendCollectionResourceTest<Template, VmTemplate, BackendStorageDomainTemplatesResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendStorageDomainTemplatesResourceTest() {
        super(new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID), null, null);
    }

    @Override
    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testListFailure() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrash() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrashClientLocale() throws Exception {

    }

    @Test
    public void testListExport() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 1);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, true);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpQueryExpectations(query, failure, StorageDomainType.Data, true);
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
                                         new String[] { "StoragePoolId", "StorageDomainId"},
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID},
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

    @Override
    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
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

    public static org.ovirt.engine.core.common.businessentities.StorageDomain setUpStorageDomain(StorageDomainType domainType) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setId(STORAGE_DOMAIN_ID);
        entity.setStorageDomainType(domainType);
        return entity;
    }

    public static List<StoragePool> setUpStoragePool() {
        final StoragePool entity = new StoragePool();
        entity.setId(DATA_CENTER_ID);
        return new ArrayList<StoragePool>(){
            private static final long serialVersionUID = 6544998068993726769L;
        {
            add(entity);}
        };
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
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }

}
