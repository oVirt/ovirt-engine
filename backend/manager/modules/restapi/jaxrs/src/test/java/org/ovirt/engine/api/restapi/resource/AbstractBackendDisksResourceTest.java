package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;


import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expect;

@Ignore
public class AbstractBackendDisksResourceTest<T extends AbstractBackendReadOnlyDevicesResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>>
        extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.Disk, T> {

    protected final static Guid PARENT_ID = GUIDS[1];

    protected VdcQueryType queryType;
    protected VdcQueryParametersBase queryParams;
    protected String queryIdName;

    public AbstractBackendDisksResourceTest(T collection,
                                            VdcQueryType queryType,
                                            VdcQueryParametersBase queryParams,
                                            String queryIdName) {
        super(collection, null, "");
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.queryIdName = queryIdName;
    }

    @Override
    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class as searching
        // over DiskImages is unsupported by the backend
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(queryType,
                                         queryParams.getClass(),
                                         new String[] { queryIdName },
                                         new Object[] { PARENT_ID },
                                         getEntityList(),
                                         failure);
        }
    }

    protected List<org.ovirt.engine.core.common.businessentities.Disk> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.Disk> entities = new ArrayList<org.ovirt.engine.core.common.businessentities.Disk>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Disk getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    static org.ovirt.engine.core.common.businessentities.Disk setUpEntityExpectations(DiskImage entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVmSnapshotId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getVolumeFormat()).andReturn(VolumeFormat.RAW).anyTimes();
        expect(entity.getDiskInterface()).andReturn(DiskInterface.VirtIO).anyTimes();
        expect(entity.getImageStatus()).andReturn(ImageStatus.OK).anyTimes();
        expect(entity.getVolumeType()).andReturn(VolumeType.Sparse).anyTimes();
        expect(entity.isBoot()).andReturn(false).anyTimes();
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

    static org.ovirt.engine.core.common.businessentities.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        expect(entity.getReadRate()).andReturn(1).anyTimes();
        expect(entity.getWriteRate()).andReturn(2).anyTimes();
        expect(entity.getReadLatency()).andReturn(3.0).anyTimes();
        expect(entity.getWriteLatency()).andReturn(4.0).anyTimes();
        expect(entity.getFlushLatency()).andReturn(5.0).anyTimes();
        return entity;
    }

    @Override
    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    static Disk getModel(int index) {
        Disk model = new Disk();
        model.setFormat(DiskFormat.COW.toString());
        model.setInterface(DiskInterface.VirtIO.toString());
        model.setSparse(true);
        model.setBootable(false);
        model.setShareable(false);
        model.setPropagateErrors(true);
        model.setStorageDomains(new StorageDomains());
        model.getStorageDomains().getStorageDomains().add(new StorageDomain());
        model.getStorageDomains().getStorageDomains().get(0).setId(GUIDS[2].toString());
        model.setSize(1000000000L);
        return model;
    }

    @Override
    protected void verifyModel(Disk model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSparse());
        assertTrue(!model.isBootable());
        assertTrue(model.isPropagateErrors());
    }
}
