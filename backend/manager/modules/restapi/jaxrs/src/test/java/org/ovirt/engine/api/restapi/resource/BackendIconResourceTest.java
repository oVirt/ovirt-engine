package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    public void testGet() throws Exception {
        setUpGetEntityExpectations(0, false);
        setUriInfo(setUpBasicUriExpectations());

        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, true);

        control.replay();

        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
     public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendIconResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmIcon,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] { GUIDS[index] },
                notFound ? null : getEntity(index));
    }
}
