package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicLabelsResourceTest
    extends AbstractBackendCollectionResourceTest<Label, NetworkLabel, BackendHostNicLabelsResource> {

    public static Guid nicId = Guid.newGuid();
    public static String hostId = Guid.newGuid().toString();
    public static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };
    private static final String NON_EXISTANT_LABEL = "xxx";

    public BackendHostNicLabelsResourceTest() {
        super(new BackendHostNicLabelsResource(nicId, hostId), null, "");
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.LabelNic,
                LabelNicParameters.class,
                new String[] { "NicId", "Label" },
                new Object[] { nicId, LABELS[0] },
                true,
                true,
                LABELS[0],
                VdcQueryType.GetNetworkLabelsByHostNicId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { nicId },
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

    private void doTestBadAdd(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.LabelNic,
                LabelNicParameters.class,
                new String[] { "NicId", "Label" },
                new Object[] { nicId, LABELS[0] },
                valid,
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

        setUpEntityQueryExpectations(VdcQueryType.GetNetworkLabelsByHostNicId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { nicId },
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
