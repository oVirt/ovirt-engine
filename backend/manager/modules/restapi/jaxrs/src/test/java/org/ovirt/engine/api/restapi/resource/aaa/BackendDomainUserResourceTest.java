package org.ovirt.engine.api.restapi.resource.aaa;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendDomainUserResourceTest
    extends AbstractBackendSubResourceTest<User, DirectoryUser, BackendDomainUserResource> {

    public BackendDomainUserResourceTest() {
        super(new BackendDomainUserResource(EXTERNAL_IDS[1], null));
    }

    @Override
    protected void init () {
        super.init();
        setUpParentExpectations();
    }

    @Test
    public void testGet() throws Exception {
      UriInfo uriInfo = setUpBasicUriExpectations();
      setUriInfo(uriInfo);
      setUpEntityQueryExpectations(1, false);
      control.replay();
      verifyModel(resource.get(), 1);
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(NAMES[index] + "@" + DOMAIN, model.getUserName());
    }

    @Test
    public void testGetNotFound() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(1, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    private void setUpParentExpectations() {
        BackendDomainUsersResource parent = control.createMock(BackendDomainUsersResource.class);
        Domain domain = new Domain();
        domain.setName(DOMAIN);
        expect(parent.getDirectory()).andReturn(domain).anyTimes();
        resource.setParent(parent);
    }

    private void setUpEntityQueryExpectations(int index, boolean notFound) throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetDirectoryUserById,
            DirectoryIdQueryParameters.class,
            new String[] { "Domain", "Id" },
                new Object[] { DOMAIN, DirectoryEntryIdUtils.decode(EXTERNAL_IDS[index]) },
            notFound? null: getEntity(index)
        );
    }

    @Override
    protected DirectoryUser getEntity(int index) {
        return new DirectoryUser(DOMAIN, NAMESPACE,
                DirectoryEntryIdUtils.decode(EXTERNAL_IDS[index]),
                NAMES[index],
                NAMES[index], "");
    }
}

