package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;

import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.PARENT_ID;

public class BackendTemplateDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.Disk, BackendTemplateDiskResource> {

    public BackendTemplateDiskResourceTest() {
        super(new BackendTemplateDiskResource(GUIDS[1],
                                              getcollection()));
    }

    protected static BackendTemplateDisksResource getcollection() {
        return new BackendTemplateDisksResource(PARENT_ID,
                                                VdcQueryType.GetVmTemplatesDisks,
                                                new GetVmTemplatesDisksParameters(PARENT_ID));
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesDisks,
                                     GetVmTemplatesDisksParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<DiskImage>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        Disk disk = resource.get();
        verifyModelSpecific(disk, 1);
        verifyLinks(disk);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Disk getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.Disk> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.Disk> entities = new ArrayList<org.ovirt.engine.core.common.businessentities.Disk>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesDisks,
                    GetVmTemplatesDisksParameters.class,
                    new String[] { "Id" },
                    new Object[] { PARENT_ID },
                    getEntityList());
        }
    }

    @Test
    public void testCopyBySdId() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetDiskByDiskId,
                GetDiskByDiskIdParameters.class,
                new String[] { "DiskId" },
                new Object[] { GUIDS[1] },
                getEntity(1));
        setUriInfo(setUpActionExpectations(VdcActionType.MoveOrCopyDisk,
                                           MoveOrCopyImageGroupParameters.class,
                                           new String[] { "ImageId", "SourceDomainId", "StorageDomainId", "Operation" },
                                           new Object[] { GUIDS[1], Guid.Empty, GUIDS[3], ImageOperation.Copy }));

        verifyActionResponse(resource.copy(setUpCopyParams(false)));
    }

    @Test
    public void testCopyBySdNameWithoutFilter() throws Exception {
        testCopyBySdName(false);
    }

    @Test
    public void testCopyBySdNameWithFilter() throws Exception {
        testCopyBySdName(true);
    }

    protected void testCopyBySdName(boolean isFiltered) throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        if (isFiltered) {
            setUpFilteredQueryExpectations();
            setUpEntityQueryExpectations(VdcQueryType.GetAllStorageDomains,
                    VdcQueryParametersBase.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(getStorageDomainEntity(0)));
        }
        else {
            setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                    SearchType.StorageDomain,
                    getStorageDomainEntity(0));
        }
        setUpEntityQueryExpectations(VdcQueryType.GetDiskByDiskId,
                GetDiskByDiskIdParameters.class,
                new String[] { "DiskId" },
                new Object[] { GUIDS[1] },
                getEntity(1));
        setUriInfo(setUpActionExpectations(VdcActionType.MoveOrCopyDisk,
                MoveOrCopyImageGroupParameters.class,
                new String[] { "ImageId", "SourceDomainId", "StorageDomainId", "Operation" },
                new Object[] { GUIDS[1], Guid.Empty, GUIDS[3], ImageOperation.Copy }));

        verifyActionResponse(resource.copy(setUpCopyParams(true)));
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomainEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        return setUpStorageDomainEntityExpectations(entity, index, StorageType.NFS);
    }

    static org.ovirt.engine.core.common.businessentities.StorageDomain setUpStorageDomainEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomain entity, int index, StorageType storageType) {
        expect(entity.getId()).andReturn(GUIDS[3]).anyTimes();
        expect(entity.getstorage_name()).andReturn(NAMES[2]).anyTimes();
        expect(entity.getstatus()).andReturn(StorageDomainStatus.Active).anyTimes();
        expect(entity.getstorage_domain_type()).andReturn(StorageDomainType.Master).anyTimes();
        expect(entity.getstorage_type()).andReturn(storageType).anyTimes();
        expect(entity.getstorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }
    private Action setUpCopyParams(boolean byName) {
        Action action = new Action();
        StorageDomain sd = new StorageDomain();
        if (byName) {
            sd.setName(NAMES[2]);
        } else {
            sd.setId(GUIDS[3].toString());
        }
        action.setStorageDomain(sd);
        return action;
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "templates/" + PARENT_ID + "/disks/" + PARENT_ID, false);
    }

    @Test
    public void testIncompleteCopy() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.copy(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "copy", "storageDomain.id|name");
        }
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected void setUpFilteredQueryExpectations() {
        List<String> filterValue = new ArrayList<String>();
        filterValue.add("true");
        EasyMock.reset(httpHeaders);
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue);
    }
}
