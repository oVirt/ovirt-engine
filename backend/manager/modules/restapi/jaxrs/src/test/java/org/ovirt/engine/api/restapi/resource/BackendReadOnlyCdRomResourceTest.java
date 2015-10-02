package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendCdRomsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyCdRomResourceTest
        extends AbstractBackendSubResourceTest<Cdrom, VM, BackendReadOnlyDeviceResource<Cdrom, Cdroms, VM>> {

    public BackendReadOnlyCdRomResourceTest() {
        super(getResource(GUIDS[0]));
    }

    protected static BackendReadOnlyDeviceResource<Cdrom, Cdroms, VM> getResource(Guid id) {
        return new BackendReadOnlyDeviceResource<Cdrom, Cdroms, VM>(Cdrom.class,
                                                                    VM.class,
                                                                    id,
                                                                    getCollection());
    }

    protected static BackendReadOnlyCdRomsResource<VM> getCollection() {
        return new BackendReadOnlyCdRomsResource<VM>(VM.class,
                                                     PARENT_ID,
                                                     VdcQueryType.GetVmByVmId,
                                                     new IdQueryParameters(PARENT_ID));
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        BackendReadOnlyDeviceResource<Cdrom, Cdroms, VM> resource =
            getResource(new Guid("0d0264ef-40de-45a1-b746-83a0088b47a8"));
        initResource(resource);
        initResource(resource.getCollection());
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
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

        Cdrom cdrom = resource.get();
        verifyModelSpecific(cdrom, 1);
        verifyLinks(cdrom);
    }

    @Override
    protected VM getEntity(int index) {
        return setUpEntityExpectations();
    }

    protected List<VM> getEntityList() {
        List<VM> entities = new ArrayList<VM>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getEntityList());
        }
    }

}
