package org.ovirt.engine.api.restapi.resource.gluster;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        parentMock = mock(BackendClusterResource.class);
        Cluster cluster = new Cluster();
        cluster.setId(clusterId.toString());
        when(parentMock.get()).thenReturn(cluster);
        collection.setParent(parentMock);
        setupListExpectations();
    }

    @Override
    public void testList() throws Exception {
        setUpHooksQueryExpectations(null);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        verifyCollection(getCollection());
    }

    @Override
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        Throwable t = new RuntimeException(FAILURE);
        setUpHooksQueryExpectations(t);

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
                mock(GlusterHookEntity.class),
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
        when(entity.getId()).thenReturn(GUIDS[index]);
        //expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        when(entity.getHookKey()).thenReturn(NAMES[index]);
        when(entity.getClusterId()).thenReturn(GUIDS[2]);
        when(entity.getGlusterCommand()).thenReturn("create");
        return entity;
    }

    private void setupListExpectations() {
        Cluster cluster = new Cluster();
        cluster.setName(defaultClusterName);
        cluster.setId(clusterId.toString());

        parentMock = mock(ClusterResource.class);
        when(parentMock.get()).thenReturn(cluster);
    }

    private void setUpHooksQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = mock(VdcQueryReturnValue.class);
        when(queryResult.getSucceeded()).thenReturn(failure == null);
        List<GlusterHookEntity> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            when(queryResult.getReturnValue()).thenReturn(entities);
        } else {
            if (failure instanceof String) {
                when(queryResult.getExceptionString()).thenReturn((String) failure);
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(VdcQueryType.GetGlusterHooks), any(GlusterParameters.class))).thenThrow((Exception) failure);
                return;
            }
        }
        when(backend.runQuery(eq(VdcQueryType.GetGlusterHooks), any(GlusterParameters.class))).thenReturn(
                queryResult);

    }
}
