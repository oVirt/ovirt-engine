package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGroupResourceTest
    extends AbstractBackendSubResourceTest<Group, DbGroup, BackendGroupResource> {

    public BackendGroupResourceTest() {
        super(new BackendGroupResource(GUIDS[0].toString(), new BackendGroupsResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendGroupResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveGroup,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(GUIDS[0], true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    private void setUpGetEntityExpectations(Guid entityId, boolean returnNull) {
        setUpGetEntityExpectations(
                QueryType.GetDbGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entityId },
                returnNull ? null : getEntity(0));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveGroup,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(false);
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
             QueryType.GetDbGroupById,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { GUIDS[0] },
             notFound ? null : getEntity(0)
        );
    }

    @Override
    protected DbGroup getEntity(int index) {
        DbGroup entity = new DbGroup();
        entity.setId(GUIDS[index]);
        entity.setExternalId(EXTERNAL_IDS[index]);
        entity.setName(NAMES[index]);
        entity.setDomain(DOMAIN);
        return entity;
    }

    protected void verifyModel(Group model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        verifyLinks(model);
    }
}
