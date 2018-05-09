package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIconResourceTest extends AbstractBackendSubResourceTest<Icon, VmIcon, BackendIconResource> {

    public BackendIconResourceTest() {
        super(new BackendIconResource(GUIDS[0].toString()));
    }

    @Override
    protected VmIcon getEntity(int index) {
        return BackendIconsResourceTest.setUpVmIcons().get(index);
    }

    @Override
    protected void verifyModel(Icon model, int index) {
        BackendIconsResourceTest.verifyIconModel(model, index);
        verifyLinks(model);
    }

    @Test
    public void testGet() {
        setUpGetEntityExpectations(0, false);
        setUriInfo(setUpBasicUriExpectations());


        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, true);

        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
     public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> new BackendIconResource("foo")));
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetVmIcon,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] { GUIDS[index] },
                notFound ? null : getEntity(index));
    }
}
