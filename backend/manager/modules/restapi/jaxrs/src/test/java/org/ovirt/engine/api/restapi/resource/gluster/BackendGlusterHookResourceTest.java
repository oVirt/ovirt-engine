package org.ovirt.engine.api.restapi.resource.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.clusterId;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookContentQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGlusterHookResourceTest extends AbstractBackendSubResourceTest<GlusterHook, GlusterHookEntity, BackendGlusterHookResource> {

    private BackendGlusterHooksResource hooksResourceMock;
    private static final Guid hookId = GUIDS[0];
    private static final String CHECKSUM = "CHECKSUM";
    private static final String CONTENT = "hook content";

    public BackendGlusterHookResourceTest() {
        super(new BackendGlusterHookResource(hookId.toString()));
    }

    @Override
    protected void init() {
        super.init();
        hooksResourceMock = mock(BackendGlusterHooksResource.class);
        resource.setParent(hooksResourceMock);
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testResolveCopy() {
        setUriInfo(setUpActionExpectations(ActionType.UpdateGlusterHook,
                GlusterHookManageParameters.class,
                new String[] { "HookId" },
                new Object[] { hookId}));

        Action action = new Action();
        action.setResolutionType("copy");
        verifyActionResponse(resource.resolve(action));
    }

    @Test
    public void testResolveAdd() {
        setUriInfo(setUpActionExpectations(ActionType.AddGlusterHook,
                GlusterHookManageParameters.class,
                new String[] { "HookId" },
                new Object[] { hookId}));

        Action action = new Action();
        action.setResolutionType("add");
        verifyActionResponse(resource.resolve(action));
    }

    @Test
    public void testEnable() {
        setUriInfo(setUpActionExpectations(ActionType.EnableGlusterHook,
                GlusterHookParameters.class,
                new String[] { "HookId" },
                new Object[] { hookId}));

        Action action = new Action();
        verifyActionResponse(resource.enable(action));
    }

    @Test
    public void testDisable() {
        setUriInfo(setUpActionExpectations(ActionType.DisableGlusterHook,
                GlusterHookParameters.class,
                new String[] { "HookId" },
                new Object[] { hookId}));

        Action action = new Action();
        verifyActionResponse(resource.disable(action));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveGlusterHook,
                GlusterHookManageParameters.class,
                new String[] { "HookId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "glusterHooks/" + hookId, false);
    }

    @Override
    protected GlusterHookEntity getEntity(int index) {
        GlusterHookEntity hookEntity = new GlusterHookEntity();
        hookEntity.setId(hookId);
        hookEntity.setClusterId(clusterId);
        hookEntity.setChecksum(CHECKSUM);
        return hookEntity;
    }

    /**
     * Overridden as {@link GlusterHookEntity} does not have description field
     */
    @Override
    protected void verifyModel(GlusterHook model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(clusterId.toString(), model.getCluster().getId());
        assertEquals(CHECKSUM, model.getChecksum());
        assertEquals(CONTENT, model.getContent());
        verifyLinks(model);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetGlusterHookById,
                    GlusterHookQueryParameters.class,
                    new String[] { "HookId", "IncludeServerHooks" },
                    new Object[] { hookId , true},
                    notFound ? null : getEntity(0));
            if(!notFound) {
                setUpEntityQueryExpectations(QueryType.GetGlusterHookContent,
                        GlusterHookContentQueryParameters.class,
                        new String[] { "GlusterHookId"},
                        new Object[] { hookId},
                        CONTENT);
            }
        }
    }
}
