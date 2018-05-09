package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostNicLabelResourceTest
    extends AbstractBackendSubResourceTest<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel, BackendHostNicLabelResource> {

    private static final Guid NIC_ID = Guid.newGuid();
    private static final Guid HOST_ID = Guid.newGuid();
    private static final String[] LABELS = { "lbl1", "lbl2", "lbl3" };

    public BackendHostNicLabelResourceTest() {
        super(
            new BackendHostNicLabelResource(
                LABELS[0],
                new BackendHostNicLabelsResource(NIC_ID, HOST_ID.toString())
            )
        );
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(Collections.emptyList());
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(getEntityList());
        NetworkLabel model = resource.get();
        assertEquals(LABELS[0], model.getId());
        verifyLinks(model);
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(getEntityList());
        setUriInfo(
            setUpActionExpectations(
                ActionType.UnlabelNic,
                LabelNicParameters.class,
                new String[] { "NicId", "Label" },
                new Object[] { NIC_ID, LABELS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpEntityQueryExpectations(Collections.emptyList());
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        setUpEntityQueryExpectations(getEntityList());
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        setUpEntityQueryExpectations(getEntityList());
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUriInfo(
            setUpActionExpectations(
                ActionType.UnlabelNic,
                LabelNicParameters.class,
                new String[] { "NicId", "Label" },
                new Object[] { NIC_ID, LABELS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpEntityQueryExpectations(List<? super org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> result) {
        setUpEntityQueryExpectations(
            QueryType.GetNetworkLabelsByHostNicId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NIC_ID },
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
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel entity = mock(org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        when(entity.getId()).thenReturn(LABELS[index]);
        return entity;
    }
}
