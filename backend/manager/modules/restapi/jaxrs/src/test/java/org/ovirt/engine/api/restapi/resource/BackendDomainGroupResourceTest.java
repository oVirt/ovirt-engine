package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDomainGroupResourceTest
    extends AbstractBackendGroupResourceTest<BackendDomainGroupResource> {

    static final Guid GROUP_ID = GUIDS[1];
    static final Guid DOMAIN_ID = GUIDS[2];

    public BackendDomainGroupResourceTest() {
      super(new BackendDomainGroupResource(GROUP_ID.toString(),
                                           new BackendDomainGroupsResource(DOMAIN_ID.toString(),null)));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendDomainGroupResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        setUpGetEntityExpectations(2);
        initParetResource(resource.parent, uriInfo);

        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    private void initParetResource(AbstractBackendGroupsResource resource, UriInfo uriInfo) {
        initResource(resource);
        resource.setUriInfo(uriInfo);
    }

    protected void setUpGetEntityExpectations(int index) throws Exception {
        LdapGroup user = BackendGroupsResourceTest.setUpEntityExpectations(control.createMock(LdapGroup.class), index);
        setUpGetEntityExpectations("ADGROUP@"+DOMAIN+": name=*",
                SearchType.AdGroup,
                user);
    }
}

