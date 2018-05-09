package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResourceTest.GROUPS;
import static org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResourceTest.PARSED_GROUPS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendUserResourceTest
        extends AbstractBackendSubResourceTest<User, DbUser, BackendUserResource> {

    public BackendUserResourceTest() {
        super(new BackendUserResource(GUIDS[0].toString(), new BackendUsersResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendUserResource("foo", null)));
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
                ActionType.RemoveUser,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(
                QueryType.GetDbUserByUserId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
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
                ActionType.RemoveUser,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetDbUserByUserId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   notFound ? null : getEntity(0));
    }

    @Override
    protected DbUser getEntity(int index) {
        DbUser entity = new DbUser();
        entity.setId(GUIDS[index]);
        entity.setExternalId(EXTERNAL_IDS[index]);
        entity.setFirstName(NAMES[index]);
        entity.setGroupNames(new LinkedList<>(Arrays.asList(GROUPS.split(","))));
        entity.setDomain(DOMAIN);
        return entity;
    }

    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        Set<String> groupNames = model.getGroups().getGroups().stream().map(Group::getName).collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList(PARSED_GROUPS)), groupNames);
        verifyLinks(model);
    }
}
