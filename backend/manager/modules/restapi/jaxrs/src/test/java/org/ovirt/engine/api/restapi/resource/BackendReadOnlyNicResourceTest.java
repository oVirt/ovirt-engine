package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendReadOnlyNicResourceTest
        extends AbstractBackendSubResourceTest<NIC, VmNetworkInterface, BackendReadOnlyDeviceResource<NIC, Nics, VmNetworkInterface>> {

    public BackendReadOnlyNicResourceTest() {
        super(new BackendReadOnlyDeviceResource<NIC, Nics, VmNetworkInterface>(NIC.class,
                                                                               VmNetworkInterface.class,
                                                                               GUIDS[1],
                                                                               getCollection()));
    }

    protected static BackendReadOnlyNicsResource getCollection() {
        return new BackendReadOnlyNicsResource(PARENT_ID,
                 VdcQueryType.GetTemplateInterfacesByTemplateId,
                 new IdQueryParameters(PARENT_ID));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetTemplateInterfacesByTemplateId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<VmNetworkInterface>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    @Ignore
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        NIC nic = resource.get();
        verifyModelSpecific(nic, 1);
        verifyLinks(nic);
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                                       control.createMock(VmNetworkStatistics.class),
                                       index);
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<VmNetworkInterface>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetTemplateInterfacesByTemplateId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getEntityList());
        }
    }

}
