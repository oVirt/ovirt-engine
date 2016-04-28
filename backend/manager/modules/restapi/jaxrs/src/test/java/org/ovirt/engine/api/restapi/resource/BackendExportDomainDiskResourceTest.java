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

import org.junit.Test;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExportDomainDiskResourceTest
   extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendExportDomainDiskResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid DISK_ID = GUIDS[2];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendExportDomainDiskResourceTest() {
        super(new BackendExportDomainDiskResource(DISK_ID.toString(),
                new BackendExportDomainDisksResource(
                    new BackendStorageDomainTemplateResource(
                        new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID),
                        TEMPLATE_ID.toString()))));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource);
        initParentResource();
    }

    private void initParentResource() {
        AbstractBackendResource parent = resource.getParent().getParent().getParent();
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        parent.setHttpHeaders(httpHeaders);
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.setUriInfo(uriInfo);
        resource.getParent().getParent().getParent().setUriInfo(uriInfo);
    }

    @Test
    public void testGet() throws Exception {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        Disk disk = resource.get();
        assertNotNull(disk);
        assertEquals(disk.getId(), DISK_ID.toString());
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));
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

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        DiskImage disk = new DiskImage();
        disk.setId(DISK_ID);
        return disk;
    }

    private HashMap<Guid, DiskImage> getDiskMap() {
        HashMap<Guid, DiskImage> map = new HashMap<>();
        DiskImage disk = new DiskImage();
        disk.setId(DISK_ID);
        map.put(DISK_ID, disk);
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
