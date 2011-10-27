package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;


import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;

@Ignore
public class AbstractBackendDisksResourceTest<T extends AbstractBackendReadOnlyDevicesResource<Disk, Disks, DiskImage>>
        extends AbstractBackendCollectionResourceTest<Disk, DiskImage, T> {

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

    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class as searching
        // over DiskImages is unsupported by the backend
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

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

    protected List<DiskImage> getEntityList() {
        List<DiskImage> entities = new ArrayList<DiskImage>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected DiskImage getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    static DiskImage setUpEntityExpectations(DiskImage entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getvm_snapshot_id()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getvm_guid()).andReturn(PARENT_ID).anyTimes();
        expect(entity.getvolume_format()).andReturn(VolumeFormat.RAW).anyTimes();
        expect(entity.getdisk_interface()).andReturn(DiskInterface.VirtIO).anyTimes();
        expect(entity.getdisk_type()).andReturn(DiskType.Data).anyTimes();
        expect(entity.getimageStatus()).andReturn(ImageStatus.OK).anyTimes();
        expect(entity.getvolume_type()).andReturn(VolumeType.Sparse).anyTimes();
        expect(entity.getboot()).andReturn(false).anyTimes();
        expect(entity.getpropagate_errors()).andReturn(PropagateErrors.On).anyTimes();
        return setUpStatisticalEntityExpectations(entity);
    }

    static DiskImage setUpStatisticalEntityExpectations(DiskImage entity) {
        expect(entity.getread_rate()).andReturn(1).anyTimes();
        expect(entity.getwrite_rate()).andReturn(2).anyTimes();
        return entity;
    }

    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    static Disk getModel(int index) {
        Disk model = new Disk();
        model.setSparse(true);
        model.setBootable(false);
        model.setPropagateErrors(true);
        return model;
    }

    protected void verifyModel(Disk model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSetVm());
        assertEquals(PARENT_ID.toString(), model.getVm().getId());
        assertTrue(model.isSparse());
        assertTrue(!model.isBootable());
        assertTrue(model.isPropagateErrors());
    }
}
