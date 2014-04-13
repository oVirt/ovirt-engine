package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.core.common.action.LabelNetworkParameters;
import org.ovirt.engine.core.common.action.UnlabelNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworkLabelsResourceTest extends AbstractBackendCollectionResourceTest<Label, NetworkLabel, BackendNetworkLabelsResource> {

    public static Guid networkId = Guid.newGuid();
    public static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };
    private static final String NON_EXISTANT_LABEL = "xxx";

    public BackendNetworkLabelsResourceTest() {
        super(new BackendNetworkLabelsResource(networkId), null, "");
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.LabelNetwork,
                LabelNetworkParameters.class,
                new String[] { "NetworkId", "Label" },
                new Object[] { networkId, LABELS[0] },
                true,
                true,
                LABELS[0],
                VdcQueryType.GetNetworkLabelsByNetworkId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { networkId },
                asList(getEntity(0)));
        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Label);
        verifyModel((Label) response.getEntity(), 0);
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.LabelNetwork,
                LabelNetworkParameters.class,
                new String[] { "NetworkId", "Label" },
                new Object[] { networkId, LABELS[0] },
                canDo,
                success));
        try {
            collection.add(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Label model = new Label();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Label", "add", "id");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.UnlabelNetwork,
                UnlabelNetworkParameters.class,
                new String[] { "NetworkId" },
                new Object[] { networkId },
                true,
                true));
        verifyRemove(collection.remove(LABELS[0]));
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpGetEntityExpectations();
        control.replay();
        try {
            collection.remove(NON_EXISTANT_LABEL);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(true, false, FAILURE);
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetNetworkLabelsByNetworkId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { networkId },
                getEntityList());
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.UnlabelNetwork,
                UnlabelNetworkParameters.class,
                new String[] { "NetworkId" },
                new Object[] { networkId },
                canDo,
                success));
        try {
            collection.remove(LABELS[0]);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Override
    protected List<Label> getCollection() {
        return collection.list().getLabels();
    }

    // No searching support for network labels
    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetNetworkLabelsByNetworkId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { networkId },
                getEntityList(),
                failure);

        control.replay();
    }

    @Override
    protected void verifyModel(Label model, int index) {
        assertEquals(LABELS[index], model.getId());
        verifyLinks(model);
    }

    @Override
    protected NetworkLabel getEntity(int index) {
        NetworkLabel networkLabel = control.createMock(NetworkLabel.class);
        expect(networkLabel.getId()).andReturn(LABELS[index]).anyTimes();
        return networkLabel;
    }

    private List<NetworkLabel> getEntityList() {
        List<NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < LABELS.length; i++) {
            labels.add(getEntity(i));
        }

        return labels;
    }

    private Label getModel(int i) {
        Label model = new Label();
        model.setId(LABELS[i]);
        return model;
    }
}
