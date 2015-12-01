package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.resource.BackendClusterResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterHooksResourceTest extends AbstractBackendCollectionResourceTest<GlusterHook, GlusterHookEntity, BackendGlusterHooksResource> {
    private static final Guid clusterId = GUIDS[0];
    private static final String defaultClusterName = "Default";
    private static ClusterResource parentMock;

    public BackendGlusterHooksResourceTest() {
        super(new BackendGlusterHooksResource(parentMock),
                null,
                null);
    }

    /**
     * Override init to perform additional mocking required
     * for the "list" method of the collection resource.
     */
    @Override
    protected void init() {
        super.init();
        parentMock = control.createMock(BackendClusterResource.class);
        Cluster cluster = new Cluster();
        cluster.setId(clusterId.toString());
        expect(parentMock.get()).andReturn(cluster).anyTimes();
        collection.setParent(parentMock);
        setupListExpectations();
    }

    @Override
    public void testList() throws Exception {
        setUpHooksQueryExpectations(null);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        control.replay();

        verifyCollection(getCollection());
    }

    @Override
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        Throwable t = new RuntimeException(FAILURE);
        setUpHooksQueryExpectations(t);
        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    /**
     * Overriding this as gluster hooks collection doesn't support search queries
     */
    @Override
    @Test
    public void testQuery() throws Exception {
        testList();
    }

    @Override
    public void testListCrashClientLocale() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpHooksQueryExpectations(t);
        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    @Override
    public void testListFailure() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpHooksQueryExpectations(FAILURE);
        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Override
    protected List<GlusterHook> getCollection() {
        return collection.list().getGlusterHooks();
    }

    @Override
    protected GlusterHookEntity getEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(GlusterHookEntity.class),
                index);
    }


    /**
     * Overridden as {@link GlusterHookEntity} does not have description field
     */
    @Override
    protected void verifyModel(GlusterHook model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals("create",
                model.getGlusterCommand());
        assertEquals(clusterId.toString(), model.getCluster().getId());
        verifyLinks(model);
    }


    static GlusterHookEntity setUpEntityExpectations(
            GlusterHookEntity entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        //expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getHookKey()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getGlusterCommand()).andReturn("create").anyTimes();
        return entity;
    }

    private void setupListExpectations() {
        Cluster cluster = new Cluster();
        cluster.setName(defaultClusterName);
        cluster.setId(clusterId.toString());

        parentMock = control.createMock(ClusterResource.class);
        expect(parentMock.get()).andReturn(cluster).anyTimes();
    }

    private void setUpHooksQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        List<GlusterHookEntity> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                expect(backend.runQuery(eq(VdcQueryType.GetGlusterHooks), anyObject(GlusterParameters.class))).andThrow((Exception) failure).anyTimes();
                return;
            }
        }
        expect(backend.runQuery(eq(VdcQueryType.GetGlusterHooks), anyObject(GlusterParameters.class))).andReturn(
                queryResult).anyTimes();

    }
}
