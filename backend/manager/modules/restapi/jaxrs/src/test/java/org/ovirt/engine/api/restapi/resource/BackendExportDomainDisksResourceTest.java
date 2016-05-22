package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.UriInfo;

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
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
        entity.setWriteRate(2);
        entity.setReadLatency(3.0);
        entity.setWriteLatency(4.0);
        entity.setFlushLatency(5.0);
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
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport, null);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, failure);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType, Object failure) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType),
                                     failure);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, Object failure) throws Exception {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false, failure);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound, Object failure) throws Exception {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplate,
                                         GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { TEMPLATE_ID },
                                         notFound ? null : getEntity(1),
                                         failure);
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool(),
                                         null);
            setUpEntityQueryExpectations(VdcQueryType.GetTemplatesFromExportDomain,
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

    private HashMap<Guid, DiskImage> getDiskMap() {
        HashMap<Guid, DiskImage> map = new HashMap<>();
        for (int i = 0; i < NAMES.length; i++) {
            DiskImage disk = (DiskImage) getEntity(i);
            map.put(disk.getId(), disk);
        }
        return map;
    }
    @Override
    protected void setUpEntityQueryExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            expect(queryResult.getReturnValue()).andReturn(queryReturn).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        expect(backend.runQuery(eq(query),
            eqQueryParams(queryClass,
                    addSession(queryNames),
                    addSession(queryValues)))).andReturn(queryResult).anyTimes();
    }

    protected HashMap<VmTemplate, List<DiskImage>> setUpTemplates(boolean notFound) {
        HashMap<VmTemplate, List<DiskImage>> ret = new HashMap<>();
        if (notFound) {
            return ret;
        }
        for (int i = 0; i < NAMES.length; i++) {
            ret.put(getVmTemplateEntity(i), new ArrayList<>());
        }
        return ret;
    }

    protected VmTemplate getVmTemplateEntity(int index) {
        VmTemplate vm = setUpEntityExpectations(control.createMock(VmTemplate.class), index);
        org.easymock.EasyMock.expect(vm.getDiskTemplateMap()).andReturn(getDiskMap()).anyTimes();
        return vm;
    }
}
