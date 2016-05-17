package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicLabelsResourceTest
    extends AbstractBackendCollectionResourceTest<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel, BackendHostNicLabelsResource> {

    public static Guid nicId = Guid.newGuid();
    public static String hostId = Guid.newGuid().toString();
    public static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };

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
        assertTrue(response.getEntity() instanceof NetworkLabel);
        verifyModel((NetworkLabel) response.getEntity(), 0);
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
        NetworkLabel model = new NetworkLabel();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "NetworkLabel", "add", "id");
        }
    }

    @Override
    protected List<NetworkLabel> getCollection() {
        return collection.list().getNetworkLabels();
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
    protected void verifyModel(NetworkLabel model, int index) {
        assertEquals(LABELS[index], model.getId());
        verifyLinks(model);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel networkLabel = control.createMock(org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        expect(networkLabel.getId()).andReturn(LABELS[index]).anyTimes();
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
