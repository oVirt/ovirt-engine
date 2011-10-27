package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendDisksResourceTest.PARENT_ID;

public class BackendReadOnlyDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, DiskImage, BackendReadOnlyDeviceResource<Disk, Disks, DiskImage>> {

    public BackendReadOnlyDiskResourceTest() {
        super(new BackendReadOnlyDeviceResource<Disk, Disks, DiskImage>(Disk.class,
                                                                        DiskImage.class,
                                                                        GUIDS[1],
                                                                        getcollection()));
    }

    protected static BackendReadOnlyDisksResource getcollection() {
        return new BackendReadOnlyDisksResource(PARENT_ID,
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
    protected DiskImage getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    protected List<DiskImage> getEntityList() {
        List<DiskImage> entities = new ArrayList<DiskImage>();
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

}
