package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendNetworkLabelResourceTest
extends AbstractBackendSubResourceTest<Label, NetworkLabel, BackendNetworkLabelResource> {

    private static final String LABEL = "aaa";

    public BackendNetworkLabelResourceTest() {
        super(new BackendNetworkLabelResource(LABEL,
                new BackendNetworkLabelsResource(BackendNetworkLabelsResourceTest.networkId)));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetNetworkLabelsByNetworkId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { BackendNetworkLabelsResourceTest.networkId },
                                     Collections.emptyList());
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

        Label model = resource.get();
        assertEquals(LABEL, model.getId());
        verifyLinks(model);
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetNetworkLabelsByNetworkId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { BackendNetworkLabelsResourceTest.networkId },
                                         Arrays.asList(getEntity(0)));
        }
    }

    @Override
    protected NetworkLabel getEntity(int index) {
        NetworkLabel entity = control.createMock(NetworkLabel.class);
        expect(entity.getId()).andReturn(LABEL).anyTimes();
        return entity;
    }
}
