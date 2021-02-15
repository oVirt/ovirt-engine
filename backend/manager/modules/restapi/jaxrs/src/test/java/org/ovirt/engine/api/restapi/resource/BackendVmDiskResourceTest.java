/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
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
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            null
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);

        Disk disk = resource.get();
        verifyModelSpecific(disk, 1);
        verifyLinks(disk);
    }

    @Test
    public void testGetIncludeStatistics() {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setUpEntityQueryExpectations(1);

            Disk disk = resource.get();
            assertTrue(disk.isSetStatistics());
            verifyModelSpecific(disk, 1);
            verifyLinks(disk);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            null
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getUpdate())));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);

        setUpDiskVmElementExpectations();

        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateDisk,
                UpdateDiskParameters.class,
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
    public void testUpdateReadOnly() {
        setUpGetEntityExpectations(2);
        setUpDiskVmElementExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateDisk,
                UpdateDiskParameters.class,
                new String[] { "VmId", "DiskVmElement.ReadOnly" },
                new Object[] { VM_ID, Boolean.TRUE },
                true,
                true
            )
        );
        Disk disk = resource.update(getUpdate());
        assertNotNull(disk);
    }

    @Test
    public void testActivate() {
        setUriInfo(
            setUpActionExpectations(
                ActionType.HotPlugDiskToVm,
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
    public void testDeactivate() {
        setUriInfo(
            setUpActionExpectations(
                ActionType.HotUnPlugDiskFromVm,
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
    public void testExport() {
        setUriInfo(
            setUpActionExpectations(
                ActionType.ExportRepoImage,
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
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendStorageDomainVmResource(null, "foo")));
    }

    @Test
    public void testIncompleteExport() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.export(new Action())),
                "Action", "export", "storageDomain.id|name");
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.DetachDiskFromVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo" },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
            true,
            true,
            false
        );
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.DetachDiskFromVm,
                AttachDetachVmDiskParameters.class,
                new String[] { "VmId", "EntityInfo" },
                new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
                valid,
                success
            )
        );
        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected DiskImage setUpStatisticalExpectations() {
        DiskImage entity = mock(DiskImage.class);
        when(entity.getId()).thenReturn(DISK_ID);
        when(entity.getReadRate()).thenReturn(10);
        when(entity.getReadOps()).thenReturn(15L);
        when(entity.getWriteRate()).thenReturn(20);
        when(entity.getWriteOps()).thenReturn(25L);
        when(entity.getReadLatency()).thenReturn(30.0);
        when(entity.getWriteLatency()).thenReturn(40.0);
        when(entity.getFlushLatency()).thenReturn(50.0);
        when(entity.getDiskStorageType()).thenReturn(DiskStorageType.IMAGE);
        setUpGetEntityExpectations(1, entity);
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
                "data.current.read_ops",
                "data.current.write",
                "data.current.write_ops",
                "disk.read.latency",
                "disk.write.latency",
                "disk.flush.latency"
            },
            new BigDecimal[] {
                asDec(10),
                asDec(15),
                asDec(20),
                asDec(25),
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
        update.setReadOnly(true);
        return update;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        return setUpEntityExpectations(mock(DiskImage.class), index);
    }

    protected void setUpEntityQueryExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                getEntity(1)
            );
        }
    }

    private void setUpDiskVmElementExpectations() {
        DiskVmElement dve = new DiskVmElement(DISK_ID, VM_ID);
        dve.setDiskInterface(DiskInterface.VirtIO);
        dve.setBoot(false);

        setUpGetEntityExpectations(
                QueryType.GetDiskVmElementById,
                VmDeviceIdQueryParameters.class,
                new String[] { "Id"},
                new Object[] { new VmDeviceId(DISK_ID, VM_ID)},
                dve
        );
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                entity
            );
        }
    }

    @Test
    public void testMoveBySdId() {
        setUpEntityQueryExpectations(
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            getEntity(1)
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.MoveDisk,
                MoveDisksParameters.class,
                new String[] {},
                new Object[] {}
            )
        );
        verifyActionResponse(resource.move(setUpMoveParams(false)));
    }

    @Test
    public void testMoveBySdNameWithoutFilter() {
        testMoveBySdName(false);
    }

    @Test
    public void testMoveBySdNameWithFilter() {
        testMoveBySdName(true);
    }

    protected void testMoveBySdName(boolean isFiltered) {
        setUriInfo(setUpBasicUriExpectations());

        if (isFiltered) {
            setUpFilteredQueryExpectations();
            setUpEntityQueryExpectations(
                QueryType.GetAllStorageDomains,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                Collections.singletonList(getStorageDomain(2))
            );
        } else {
            setUpEntityQueryExpectations(
                QueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2)
            );
        }

        setUpEntityQueryExpectations(
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            getEntity(1)
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.MoveDisk,
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
        reset(httpHeaders);
        when(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).thenReturn(filterValue);
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
    public void testIncompleteMove() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.move(new Action())),
                "Action", "move", "storageDomain.id|name");
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "vms/" + VM_ID + "/disks/" + DISK_ID, false);
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

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    private org.ovirt.engine.core.common.businessentities.storage.Disk setUpEntityExpectations(DiskImage entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getVmSnapshotId()).thenReturn(GUIDS[2]);
        when(entity.getVolumeFormat()).thenReturn(VolumeFormat.RAW);
        when(entity.getImageStatus()).thenReturn(ImageStatus.OK);
        when(entity.getVolumeType()).thenReturn(VolumeType.Sparse);
        when(entity.isShareable()).thenReturn(false);
        when(entity.getPropagateErrors()).thenReturn(PropagateErrors.On);
        when(entity.getDiskStorageType()).thenReturn(DiskStorageType.IMAGE);
        when(entity.getImageId()).thenReturn(GUIDS[1]);
        ArrayList<Guid> sdIds = new ArrayList<>();
        sdIds.add(Guid.Empty);
        when(entity.getStorageIds()).thenReturn(sdIds);
        return setUpStatisticalEntityExpectations(entity);
    }

    private org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        when(entity.getReadRate()).thenReturn(1);
        when(entity.getReadOps()).thenReturn(2L);
        when(entity.getWriteRate()).thenReturn(3);
        when(entity.getWriteOps()).thenReturn(4L);
        when(entity.getReadLatency()).thenReturn(5.0);
        when(entity.getWriteLatency()).thenReturn(6.0);
        when(entity.getFlushLatency()).thenReturn(7.0);
        return entity;
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSparse());
        assertTrue(model.isPropagateErrors());
    }
}
