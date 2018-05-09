package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testGet() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(1, false);
        verifyModel(resource.get(), 1);
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(NAMES[index] + "@" + DOMAIN, model.getUserName());
    }

    @Test
    public void testGetNotFound() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        setUpEntityQueryExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    private void setUpParentExpectations() {
        BackendDomainUsersResource parent = mock(BackendDomainUsersResource.class);
        Domain domain = new Domain();
        domain.setName(DOMAIN);
        when(parent.getDirectory()).thenReturn(domain);
        resource.setParent(parent);
    }

    private void setUpEntityQueryExpectations(int index, boolean notFound) {
        setUpGetEntityExpectations(
            QueryType.GetDirectoryUserById,
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

