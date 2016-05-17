package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.core.common.action.UnlabelNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworkLabelResourceTest
    extends AbstractBackendSubResourceTest<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel, BackendNetworkLabelResource> {

    private static final Guid NETWORK_ID = Guid.newGuid();
    private static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };

    public BackendNetworkLabelResourceTest() {
        super(new BackendNetworkLabelResource(LABELS[0], new BackendNetworkLabelsResource(NETWORK_ID)));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(Collections.emptyList());
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
        setUpEntityQueryExpectations(getEntityList());
        control.replay();
        NetworkLabel model = resource.get();
        assertEquals(LABELS[0], model.getId());
        verifyLinks(model);
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(getEntityList());
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UnlabelNetwork,
                UnlabelNetworkParameters.class,
                new String[] { "NetworkId" },
                new Object[] { NETWORK_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpEntityQueryExpectations(Collections.emptyList());
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpEntityQueryExpectations(getEntityList());
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpEntityQueryExpectations(getEntityList());
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UnlabelNetwork,
                UnlabelNetworkParameters.class,
                new String[] { "NetworkId" },
                new Object[] { NETWORK_ID },
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

    private void setUpEntityQueryExpectations(List<? super org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> result) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkLabelsByNetworkId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            result
        );
    }

    private List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < LABELS.length; i++) {
            labels.add(getEntity(i));
        }
        return labels;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel entity = control.createMock(org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        expect(entity.getId()).andReturn(LABELS[index]).anyTimes();
        return entity;
    }

}
