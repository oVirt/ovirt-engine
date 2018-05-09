package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelNetworkParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendNetworkLabelsResourceTest
    extends AbstractBackendCollectionResourceTest<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel, BackendNetworkLabelsResource> {

    private static final Guid NETWORK_ID = Guid.newGuid();
    private static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };

    public BackendNetworkLabelsResourceTest() {
        super(new BackendNetworkLabelsResource(NETWORK_ID), null, "");
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.LabelNetwork,
                LabelNetworkParameters.class,
                new String[] { "NetworkId", "Label" },
                new Object[] {NETWORK_ID, LABELS[0] },
                true,
                true,
                LABELS[0],
                QueryType.GetNetworkLabelsByNetworkId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {NETWORK_ID},
                asList(getEntity(0)));
        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof NetworkLabel);
        verifyModel((NetworkLabel) response.getEntity(), 0);
    }

    @Test
    public void testAddCantDo() {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.LabelNetwork,
                LabelNetworkParameters.class,
                new String[] { "NetworkId", "Label" },
                new Object[] {NETWORK_ID, LABELS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(getModel(0))), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        NetworkLabel model = new NetworkLabel();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "NetworkLabel", "add", "id");
    }


    @Override
    protected List<NetworkLabel> getCollection() {
        return collection.list().getNetworkLabels();
    }

    // No searching support for network labels
    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetNetworkLabelsByNetworkId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {NETWORK_ID},
                getEntityList(),
                failure);

    }

    @Override
    protected void verifyModel(NetworkLabel model, int index) {
        assertEquals(LABELS[index], model.getId());
        verifyLinks(model);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel networkLabel = mock(org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        when(networkLabel.getId()).thenReturn(LABELS[index]);
        return networkLabel;
    }

    private List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < LABELS.length; i++) {
            labels.add(getEntity(i));
        }

        return labels;
    }

    private NetworkLabel getModel(int i) {
        NetworkLabel model = new NetworkLabel();
        model.setId(LABELS[i]);
        return model;
    }
}
