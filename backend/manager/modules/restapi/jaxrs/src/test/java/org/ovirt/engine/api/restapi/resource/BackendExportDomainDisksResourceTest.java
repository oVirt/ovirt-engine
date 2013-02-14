package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
import javax.ws.rs.core.UriInfo;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExportDomainDisksResourceTest
    extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.Disk, BackendExportDomainDisksResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid DISK_ID = GUIDS[2];
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
        AbstractBackendResource parent = ((AbstractBackendStorageDomainContentResource)collection.getParent()).getParent();
        parent.setBackend(backend);
        parent.setMappingLocator(mapperLocator);
        parent.setValidatorLocator(validatorLocator);
        parent.setSessionHelper(sessionHelper);
        parent.setMessageBundle(messageBundle);
        parent.setHttpHeaders(httpHeaders);
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        collection.setUriInfo(uriInfo);
        ((AbstractBackendStorageDomainContentResource)collection.getParent()).getParent().setUriInfo(uriInfo);
    }

    @Override
    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Disk getEntity(int index) {
        DiskImage entity = new DiskImage();
        entity.setId(GUIDS[index]);
        entity.setvolume_format(VolumeFormat.RAW);
        entity.setDiskInterface(DiskInterface.VirtIO);
        entity.setImageStatus(ImageStatus.OK);
        entity.setvolume_type(VolumeType.Sparse);
        entity.setBoot(false);
        entity.setShareable(false);
        entity.setPropagateErrors(PropagateErrors.On);
        return setUpStatisticalEntityExpectations(entity);    }

    static org.ovirt.engine.core.common.businessentities.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        entity.setread_rate(1);
        entity.setwrite_rate(2);
        entity.setReadLatency(3.0);
        entity.setWriteLatency(4.0);
        entity.setFlushLatency(5.0);
        return entity;
    }

    private Object getStorageDomains() {
        List<storage_domains> sds = new LinkedList<storage_domains>();
        storage_domains sd = new storage_domains();
        sd.setstorage_name("Storage_Domain_1");
        sd.setId(GUIDS[2]);
        sds.add(sd);
        return sds;
    }

    static Disk getModel(int index) {
        Disk model = new Disk();
        model.setSize(1024 * 1024L);
        model.setFormat(DiskFormat.COW.value());
        model.setInterface(org.ovirt.engine.api.model.DiskInterface.IDE.value());
        model.setSparse(true);
        model.setBootable(false);
        model.setShareable(false);
        model.setPropagateErrors(true);
        model.setStorageDomains(new StorageDomains());
        model.getStorageDomains().getStorageDomains().add(new StorageDomain());
        model.getStorageDomains().getStorageDomains().get(0).setId(GUIDS[2].toString());
        return model;
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
        assertTrue(!model.isBootable());
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
                                     StorageDomainQueryParametersBase.class,
                                     new String[] { "StorageDomainId" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType),
                                     failure);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) throws Exception {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, null);
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
                                         StorageDomainQueryParametersBase.class,
                                         new String[] { "StorageDomainId" },
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
        HashMap<Guid, DiskImage> map = new HashMap<Guid, DiskImage>();
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
        if(queryClass == GetPermissionsForObjectParameters.class) {
            expect(backend.RunQuery(eq(query),
                eqQueryParams(queryClass,
                        addSession(queryNames),
                        addSession(queryValues)))).andReturn(queryResult).anyTimes();
        } else {
            expect(backend.RunQuery(eq(query),
                eqQueryParams(queryClass,
                        addSession(queryNames),
                        addSession(queryValues)))).andReturn(queryResult).anyTimes();
        }
    }

    protected HashMap<VmTemplate, DiskImageList> setUpTemplates(boolean notFound) {
        HashMap<VmTemplate, DiskImageList> ret = new HashMap<VmTemplate, DiskImageList>();
        if (notFound) {
            return ret;
        }
        for (int i = 0; i < NAMES.length; i++) {
            ret.put(getVmTemplateEntity(i), new DiskImageList());
        }
        return ret;
    }

    protected VmTemplate getVmTemplateEntity(int index) {
        VmTemplate vm = setUpEntityExpectations(control.createMock(VmTemplate.class), index);
        org.easymock.EasyMock.expect(vm.getDiskMap()).andReturn(getDiskMap()).anyTimes();
        return vm;
    }

    protected storage_domains getStorageDomain(int idx) {
        storage_domains dom = new storage_domains();
        dom.setId(GUIDS[idx]);
        return dom;
    }

    protected VDSGroup getCluster(int idx) {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(GUIDS[idx]);
        return cluster;
    }
}
