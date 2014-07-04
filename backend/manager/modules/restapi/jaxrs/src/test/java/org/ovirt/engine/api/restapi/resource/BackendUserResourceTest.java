package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendUsersResourceTest.GROUPS;
import static org.ovirt.engine.api.restapi.resource.BackendUsersResourceTest.PARSED_GROUPS;

import java.util.Arrays;
import java.util.HashSet;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserNameAndDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendUserResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
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
        setUpGetEntityExpectations();
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetUserByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserNameAndDomain,
                GetDbUserByUserNameAndDomainQueryParameters.class,
                new String[] { "UserName", "DomainName" },
                new Object[] { "admin", "internal" },
                getEntity(0));
        control.replay();
        verifyModel(resource.getUserByNameAndDomain("admin", "internal"), 0);
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
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
        entity.setGroupNames(new HashSet<String>(Arrays.asList(GROUPS.split(","))));
        entity.setDomain(DOMAIN);
        return entity;
    }

    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        HashSet<String> groupNames = new HashSet<>();
        for (Group group : model.getGroups().getGroups()) {
            groupNames.add(group.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList(PARSED_GROUPS)), groupNames);
        verifyLinks(model);
    }
}
