package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkAttachmentParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNetworkAttachmentResourceTest<C extends AbstractBackendNetworkAttachmentsResource, R extends AbstractBackendNetworkAttachmentResource<C>>
        extends AbstractBackendSubResourceTest<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment, R> {

    protected static final Guid hostId = Guid.newGuid();

    public AbstractBackendNetworkAttachmentResourceTest(R resource) {
        super(resource);
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, this::createReourceWithBadGuid));
    }

    protected abstract void createReourceWithBadGuid();

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpEntityQueryExpectations(2, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.UpdateNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, 0, false);

        org.ovirt.engine.api.model.NetworkAttachment model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(1, 0, false);
        setUriInfo(setUpActionExpectations(ActionType.RemoveNetworkAttachment,
            RemoveNetworkAttachmentParameters.class,
            new String[] {},
            new Object[] {},
            true,
            true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(1, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.RemoveNetworkAttachment,
            RemoveNetworkAttachmentParameters.class,
            new String[] {},
            new Object[] {},
            valid,
            success));
        verifyFault(assertThrows(WebApplicationException.class, () -> resource.remove()), detail);
    }

    @Test
    public void testRemoveNotFound() {
        setUpEntityQueryExpectations(1, 0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveNonExistant() {
        QueryReturnValue queryReturnValue = new QueryReturnValue();
        queryReturnValue.setSucceeded(false);

        setUpEntityQueryExpectations(QueryType.GetNetworkAttachmentById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { new Guid(resource.id) },
                queryReturnValue);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Override
    protected NetworkAttachment getEntity(int index) {
        return setUpEntityExpectations(mock(NetworkAttachment.class), index);
    }

    @Override
    protected final void verifyModel(org.ovirt.engine.api.model.NetworkAttachment model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(GUIDS[index].toString(), model.getNetwork().getId());
        verifyModel(model);
        verifyLinks(model);
    }

    protected void verifyModel(org.ovirt.engine.api.model.NetworkAttachment model) {
    }

    protected void setUpEntityQueryExpectations(int times, int index, boolean notFound) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetNetworkAttachmentById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[index] },
                    notFound ? null : getEntity(index));
        }
    }

    protected final NetworkAttachment setUpEntityExpectations(NetworkAttachment entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getNetworkId()).thenReturn(GUIDS[index]);
        setUpEntityExpectations(entity);
        return entity;
    }

    protected void setUpEntityExpectations(NetworkAttachment entity) {
    }

    protected org.ovirt.engine.api.model.NetworkAttachment getModel(int index) {
        org.ovirt.engine.api.model.NetworkAttachment model = new org.ovirt.engine.api.model.NetworkAttachment();
        model.setId(GUIDS[index].toString());
        model.setNetwork(new Network());
        model.getNetwork().setId(GUIDS[index].toString());
        return model;
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(1, 0, false);

        setUriInfo(setUpActionExpectations(ActionType.UpdateNetworkAttachment,
                NetworkAttachmentParameters.class,
                new String[] {},
                new Object[] {},
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }
}
