package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDomainUserResourceTest
    extends AbstractBackendUserResourceTest<BackendDomainUserResource> {

    static final Guid USER_ID = GUIDS[1];
    static final Guid DOMAIN_ID = GUIDS[2];

    public BackendDomainUserResourceTest() {
        super(new BackendDomainUserResource(USER_ID.toString(),
                new BackendDomainUsersResource(DOMAIN_ID.toString(),null)));

    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendDomainUserResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
      UriInfo uriInfo = setUpBasicUriExpectations();
      setUriInfo(uriInfo);
      setUpEntityQueryExpectations(1);
      initParetResource(resource.parent, uriInfo);

      control.replay();

      verifyModel(resource.get(), 1);
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
    }

    @Test
    public void testGetNotFound() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(2);
        initParetResource(resource.parent, uriInfo);

        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    private void initParetResource(AbstractBackendUsersResource resource, UriInfo uriInfo) {
        initResource(resource);
        resource.setUriInfo(uriInfo);
    }

    protected void setUpEntityQueryExpectations(int index) throws Exception {
        LdapUser user = BackendUsersResourceTest.setUpEntityExpectations(control.createMock(LdapUser.class),index);
        setUpGetEntityExpectations("ADUSER@"+DOMAIN+": allnames=*",
                SearchType.AdUser,
                user);
    }
}

