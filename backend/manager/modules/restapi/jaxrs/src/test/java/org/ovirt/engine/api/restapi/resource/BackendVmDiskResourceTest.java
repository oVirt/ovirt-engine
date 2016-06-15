/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

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
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDiskResourceTest
    extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendVmDiskResource> {

    private static final Guid VM_ID = GUIDS[0];
    private static final Guid DISK_ID = GUIDS[1];

    public BackendVmDiskResourceTest() {
        super((BackendVmDiskResource)getCollection().getDiskResource(DISK_ID.toString()));
    }

    protected static BackendVmDisksResource getCollection() {
        return new BackendVmDisksResource(VM_ID);
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            null
        );
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
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
        setUpEntityQueryExpectations(
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            null
        );
        control.replay();
        try {
            resource.update(getUpdate());
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);

        setUpDiskVmElementExpectations();

        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVmDisk,
                VmDiskOperationParameterBase.class,
                new String[] { "VmId", "DiskInfo.WipeAfterDelete" },
                new Object[] { VM_ID, Boolean.FALSE },
                true,
                true
            )
        );
        Disk disk = resource.update(getUpdate());
        assertNotNull(disk);
    }

    @Test
    public void testUpdateReadOnly() throws Exception {
        setUpGetEntityExpectations(2);
        setUpDiskVmElementExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateVmDisk,
                VmDiskOperationParameterBase.class,
                new String[] { "VmId", "DiskInfo.ReadOnly" },
                new Object[] { VM_ID, Boolean.TRUE },
                true,
                true
            )
        );
        Disk disk = resource.update(getUpdate());
        assertNotNull(disk);
    }

    @Test
    public void testActivate() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.HotPlugDiskToVm,
                VmDiskOperationParameterBase.class,
                new String[] { "DiskVmElement" },
                new Object[] { new DiskVmElement(DISK_ID, VM_ID) },
                true,
                true
            )
        );
        Response response = resource.activate(new Action());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDeactivate() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.HotUnPlugDiskFromVm,
                VmDiskOperationParameterBase.class,
                new String[] { "DiskVmElement" },
                new Object[] { new DiskVmElement(DISK_ID, VM_ID) },
                true,
                true
            )
        );
        Response response = resource.deactivate(new Action());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        DiskImage entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<Disk, DiskImage> statisticsResource =
            (BackendStatisticsResource<Disk, DiskImage>) resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    @Test
    public void testExport() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.ExportRepoImage,
                ExportRepoImageParameters.class,
                new String[] { "ImageGroupID", "DestinationDomainId" },
                new Object[] { DISK_ID, GUIDS[3]},
                true,
                true,
                null,
                null,
                true
            )
        );
        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[3].toString());
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendStorageDomainVmResource(null, "foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.export(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.DetachDiskFromVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo" },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
            true,
            true,
            false
        );
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DetachDiskFromVm,
                AttachDetachVmDiskParameters.class,
                new String[] { "VmId", "EntityInfo" },
                new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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
        setUpGetEntityExpectations(1, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<Disk, DiskImage> query, DiskImage entity) throws Exception {
        assertEquals(Disk.class, query.getParentType());
        assertSame(entity, query.resolve(DISK_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(
            statistics,
            new String[] {
                "data.current.read",
                "data.current.write",
                "disk.read.latency",
                "disk.write.latency",
                "disk.flush.latency"
            },
            new BigDecimal[] {
                asDec(10),
                asDec(20),
                asDec(30.0),
                asDec(40.0),
                asDec(50.0)
            }
        );
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetDisk());
        assertEquals(DISK_ID.toString(), adopted.getDisk().getId());
        assertTrue(adopted.getDisk().isSetVm());
        assertEquals(VM_ID.toString(), adopted.getDisk().getVm().getId());
    }

    protected Disk getUpdate() {
        Disk update = new Disk();
        update.setWipeAfterDelete(false);
        update.setReadOnly(false);
        return update;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                getEntity(1)
            );
        }
    }

    private void setUpDiskVmElementExpectations() throws Exception {
        DiskVmElement dve = new DiskVmElement(DISK_ID, VM_ID);
        dve.setDiskInterface(DiskInterface.VirtIO);
        dve.setBoot(false);

        setUpGetEntityExpectations(
                VdcQueryType.GetDiskVmElementById,
                VmDeviceIdQueryParameters.class,
                new String[] { "Id"},
                new Object[] { new VmDeviceId(DISK_ID, VM_ID)},
                dve
        );
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, org.ovirt.engine.core.common.businessentities.storage.Disk entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                entity
            );
        }
    }

    @Test
    public void testMoveBySdId() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            getEntity(1)
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.MoveDisks,
                MoveDisksParameters.class,
                new String[] {},
                new Object[] {}
            )
        );
        verifyActionResponse(resource.move(setUpMoveParams(false)));
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
            setUpEntityQueryExpectations(
                VdcQueryType.GetAllStorageDomains,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                Collections.singletonList(getStorageDomain(2))
            );
        }
        else {
            setUpEntityQueryExpectations(
                VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2)
            );
        }

        setUpEntityQueryExpectations(
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            getEntity(1)
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.MoveDisks,
                MoveDisksParameters.class,
                new String[] {},
                new Object[] {}
            )
        );

        verifyActionResponse(resource.move(setUpMoveParams(true)));
    }

    protected void setUpFilteredQueryExpectations() {
        List<String> filterValue = new ArrayList<>();
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

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStorageDomainStatic(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic dom =
                new org.ovirt.engine.core.common.businessentities.StorageDomainStatic();
        dom.setId(GUIDS[idx]);
        dom.setStorageName(NAMES[idx]);
        return dom;
    }

    @Test
    public void testIncompleteMove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.move(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "move", "storageDomain.id|name");
        }
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID + "/disks/" + DISK_ID, false);
    }

    private Action setUpMoveParams(boolean byName) {
        Action action = new Action();
        StorageDomain sd = new StorageDomain();
        if (byName) {
            sd.setName(NAMES[2]);
        }
        else {
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

    private org.ovirt.engine.core.common.businessentities.storage.Disk setUpEntityExpectations(DiskImage entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVmSnapshotId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getVolumeFormat()).andReturn(VolumeFormat.RAW).anyTimes();
        expect(entity.getImageStatus()).andReturn(ImageStatus.OK).anyTimes();
        expect(entity.getVolumeType()).andReturn(VolumeType.Sparse).anyTimes();
        expect(entity.isShareable()).andReturn(false).anyTimes();
        expect(entity.getPropagateErrors()).andReturn(PropagateErrors.On).anyTimes();
        expect(entity.getDiskStorageType()).andReturn(DiskStorageType.IMAGE).anyTimes();
        expect(entity.getImageId()).andReturn(GUIDS[1]).anyTimes();
        expect(entity.getReadOnly()).andReturn(true).anyTimes();
        ArrayList<Guid> sdIds = new ArrayList<>();
        sdIds.add(Guid.Empty);
        expect(entity.getStorageIds()).andReturn(sdIds).anyTimes();
        return setUpStatisticalEntityExpectations(entity);
    }

    private org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        expect(entity.getReadRate()).andReturn(1).anyTimes();
        expect(entity.getWriteRate()).andReturn(2).anyTimes();
        expect(entity.getReadLatency()).andReturn(3.0).anyTimes();
        expect(entity.getWriteLatency()).andReturn(4.0).anyTimes();
        expect(entity.getFlushLatency()).andReturn(5.0).anyTimes();
        return entity;
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSparse());
        assertTrue(model.isPropagateErrors());
    }
}
