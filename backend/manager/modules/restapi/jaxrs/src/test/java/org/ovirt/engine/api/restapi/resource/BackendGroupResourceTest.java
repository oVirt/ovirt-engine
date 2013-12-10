package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ExternalId;

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
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendGroupResource("foo", null);
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

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    private void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(
             VdcQueryType.GetDbGroupById,
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
        entity.setName(NAMES[index]);
        entity.setDomain(DOMAIN);
        entity.setExternalId(new ExternalId(GUIDS[index].toByteArray()));
        return entity;
    }

    protected void verifyModel(Group model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        verifyLinks(model);
    }
}
