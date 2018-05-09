package org.ovirt.engine.api.restapi.resource.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.resource.BackendClusterResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testListCrash() {
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        Throwable t = new RuntimeException(FAILURE);
        setUpHooksQueryExpectations(t);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
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
    public void testListCrashClientLocale() {
        collection.setUriInfo(setUpUriExpectations(null));
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpHooksQueryExpectations(t);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    @Override
    public void testListFailure() {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpHooksQueryExpectations(FAILURE);

        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
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
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        List<GlusterHookEntity> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            queryResult.setReturnValue(entities);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(QueryType.GetGlusterHooks), any())).thenThrow((Exception) failure);
                return;
            }
        }
        when(backend.runQuery(eq(QueryType.GetGlusterHooks), any())).thenReturn(queryResult);

    }
}
