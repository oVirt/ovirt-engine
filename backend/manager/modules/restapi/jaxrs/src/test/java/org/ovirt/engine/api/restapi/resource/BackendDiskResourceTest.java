package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.PARENT_ID;
import static org.easymock.EasyMock.expect;

public class BackendDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.Disk, BackendDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>> {

    protected static final Guid DISK_ID = GUIDS[1];

    protected static BackendDisksResource collection;

    public BackendDiskResourceTest() {
        super((BackendDiskResource)getCollection().getDeviceSubResource(DISK_ID.toString()));
    }

    protected static BackendDisksResource getCollection() {
        return new BackendDisksResource(PARENT_ID,
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

        Response response = ((DiskResource) resource).activate(new Action());
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

        Response response = ((DiskResource) resource).deactivate(new Action());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        DiskImage entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Disk, DiskImage> statisticsResource =
            (BackendStatisticsResource<Disk, DiskImage>)((DiskResource)resource).getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected DiskImage setUpStatisticalExpectations() throws Exception {
        DiskImage entity = control.createMock(DiskImage.class);
        expect(entity.getId()).andReturn(DISK_ID).anyTimes();
        expect(entity.getread_rate()).andReturn(10);
        expect(entity.getwrite_rate()).andReturn(20);
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
                         new String[] {"data.current.read", "data.current.write"},
                         new BigDecimal[] {asDec(10), asDec(20)});
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

}
