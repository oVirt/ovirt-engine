package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.verifyModelSpecific;

import java.math.BigDecimal;
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
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.Disk, BackendDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>> {

    protected static final Guid DISK_ID = GUIDS[1];

    protected static BackendVmDisksResource collection;

    public BackendVmDiskResourceTest() {
        super((BackendVmDiskResource)getCollection().getDeviceSubResource(DISK_ID.toString()));
    }

    protected static BackendVmDisksResource getCollection() {
        return new BackendVmDisksResource(PARENT_ID,
                                        VdcQueryType.GetAllDisksByVmId,
                                        new GetAllDisksByVmIdParameters(PARENT_ID));
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
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

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setUpEntityQueryExpectations(1);
            control.replay();

            Disk disk = resource.get();
            assertTrue(disk.isSetStatistics());
            verifyModelSpecific(disk, 1);
            verifyLinks(disk);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<DiskImage>());
        control.replay();
        try {
            resource.update(getUpdate());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmDisk,
                                           UpdateVmDiskParameters.class,
                                           new String[] { "VmId", "DiskId", "DiskInfo.WipeAfterDelete" },
                                           new Object[] { PARENT_ID, GUIDS[1], Boolean.FALSE },
                                           true,
                                           true));

        Disk disk = resource.update(getUpdate());
        assertNotNull(disk);
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HotPlugDiskToVm,
                                           HotPlugDiskToVmParameters.class,
                                           new String[] { "VmId", "DiskId" },
                                           new Object[] { PARENT_ID, DISK_ID },
                                           true,
                                           true));

        Response response = ((VmDiskResource) resource).activate(new Action());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HotUnPlugDiskFromVm,
                                           HotPlugDiskToVmParameters.class,
                                           new String[] { "VmId", "DiskId" },
                                           new Object[] { PARENT_ID, DISK_ID },
                                           true,
                                           true));

        Response response = ((VmDiskResource) resource).deactivate(new Action());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        DiskImage entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Disk, DiskImage> statisticsResource =
            (BackendStatisticsResource<Disk, DiskImage>)((VmDiskResource)resource).getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected DiskImage setUpStatisticalExpectations() throws Exception {
        DiskImage entity = control.createMock(DiskImage.class);
        expect(entity.getId()).andReturn(DISK_ID).anyTimes();
        expect(entity.getReadRate()).andReturn(10);
        expect(entity.getWriteRate()).andReturn(20);
        expect(entity.getReadLatency()).andReturn(30.0).times(2);
        expect(entity.getWriteLatency()).andReturn(40.0).times(2);
        expect(entity.getFlushLatency()).andReturn(50.0).times(2);
        expect(entity.getDiskStorageType()).andReturn(DiskStorageType.IMAGE).anyTimes();
        List<DiskImage> ifaces = new ArrayList<DiskImage>();
        ifaces.add(entity);
        setUpGetEntityExpectations(1, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Disk, DiskImage> query, DiskImage entity) throws Exception {
        assertEquals(Disk.class, query.getParentType());
        assertSame(entity, query.resolve(DISK_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"data.current.read", "data.current.write", "disk.read.latency", "disk.write.latency", "disk.flush.latency"},
                         new BigDecimal[] {asDec(10), asDec(20), asDec(30.0), asDec(40.0), asDec(50.0)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetDisk());
        assertEquals(DISK_ID.toString(), adopted.getDisk().getId());
        assertTrue(adopted.getDisk().isSetVm());
        assertEquals(PARENT_ID.toString(), adopted.getDisk().getVm().getId());
    }

    protected Disk getUpdate() {
        Disk update = new Disk();
        update.setWipeAfterDelete(false);
        return update;
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
            setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                         GetAllDisksByVmIdParameters.class,
                                         new String[] { "VmId" },
                                         new Object[] { PARENT_ID },
                                         getEntityList());
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, org.ovirt.engine.core.common.businessentities.Disk entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetAllDisksByVmId,
                                       GetAllDisksByVmIdParameters.class,
                                       new String[] { "VmId" },
                                       new Object[] { PARENT_ID },
                                       entity);
        }
    }

    @Test
    public void testMoveBySdId() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getEntity(1));
        setUriInfo(setUpActionExpectations(VdcActionType.MoveDisks,
                MoveDisksParameters.class,
                new String[] { "ParametersList" },
                new Object[] { Collections.singletonList(new MoveDiskParameters(GUIDS[1], Guid.Empty, GUIDS[3])) }));

        verifyActionResponse(((VmDiskResource) resource).move(setUpMoveParams(false)));
    }

    @Test
    public void testMoveBySdNameWithoutFilter() throws Exception {
        testMoveBySdName(false);
    }

    @Test
    public void testMoveBySdNameWithFilter() throws Exception {
        testMoveBySdName(true);
    }

    protected void testMoveBySdName(boolean isFiltered) throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        if (isFiltered) {
            setUpFilteredQueryExpectations();
            setUpEntityQueryExpectations(VdcQueryType.GetAllStorageDomains,
                    VdcQueryParametersBase.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(getStorageDomain(2)));
        }
        else {
            setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                    SearchType.StorageDomain,
                    getStorageDomain(2));
        }

        setUpEntityQueryExpectations(VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getEntity(1));
        setUriInfo(setUpActionExpectations(VdcActionType.MoveDisks,
                MoveDisksParameters.class,
                new String[] { "ParametersList" },
                new Object[] { Collections.singletonList(new MoveDiskParameters(GUIDS[1], Guid.Empty, GUIDS[3])) }));

        verifyActionResponse(((VmDiskResource) resource).move(setUpMoveParams(true)));
    }

    protected void setUpFilteredQueryExpectations() {
        List<String> filterValue = new ArrayList<String>();
        filterValue.add("true");
        EasyMock.reset(httpHeaders);
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomain dom = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        dom.setId(GUIDS[idx]);
        dom.setStorageName(NAMES[idx]);
        return dom;
    }

    @Test
    public void testIncompleteMove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            ((VmDiskResource) resource).move(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "move", "storageDomain.id|name");
        }
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + PARENT_ID + "/disks/" + PARENT_ID, false);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomainEntity(int index) {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = control.createMock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        return setUpStorageDomainEntityExpectations(entity, index, StorageType.NFS);
    }

    static org.ovirt.engine.core.common.businessentities.StorageDomain setUpStorageDomainEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomain entity,
            int index,
            StorageType storageType) {
        expect(entity.getId()).andReturn(GUIDS[3]).anyTimes();
        expect(entity.getStorageName()).andReturn(NAMES[2]).anyTimes();
        expect(entity.getStatus()).andReturn(StorageDomainStatus.Active).anyTimes();
        expect(entity.getStorageDomainType()).andReturn(StorageDomainType.Master).anyTimes();
        expect(entity.getStorageType()).andReturn(storageType).anyTimes();
        expect(entity.getStorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }

    private Action setUpMoveParams(boolean byName) {
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

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

}
