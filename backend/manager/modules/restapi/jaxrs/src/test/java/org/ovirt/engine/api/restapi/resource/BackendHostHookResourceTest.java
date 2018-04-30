package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostHookResourceTest extends AbstractBackendSubResourceTest<Hook, Object, BackendHostHookResource> {

    private static final String MD5_2 = "md5_2";
    private static final String HOOK_2_NAME = "hook_2";
    private static final String EVENT_2_NAME = "event_2";
    private static final String MD5_1 = "md5_1";
    private static final String HOOK_1_NAME = "hook_1";
    private static final String EVENT_1_NAME = "event_1";
    private static final String EVENT2_HOOK2_MD52_HASH = "a9affe45-5cc4-148f-ce4d-f585c7999e05";
    private static final String SOME_ID = "b9af3e45-5ic4-128f-ce4d-a585c7888ecf";

    public BackendHostHookResourceTest() {
        super(new BackendHostHookResource(EVENT2_HOOK2_MD52_HASH, null));
    }

    private BackendHostHooksResource getCollectionResourceMock() {
        BackendHostHooksResource resource = mock(BackendHostHooksResource.class);
        when(resource.list()).thenReturn(getHooks());
        return resource;
    }

    /**
     * Resource was initialized with ID = "a9affe45-5cc4-148f-ce4d-f585c7999e05". The test verifies the Hook with this
     * ID is returned out of all hooks returned by parent.list();
     */
    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        resource.setParent(getCollectionResourceMock());
        Hook hook = resource.get();
        assertEquals(EVENT_2_NAME, hook.getEventName());
    }

    private Hooks getHooks() {
        Hooks hooks = new Hooks();
        Hook hook = new Hook();
        hook.setId(SOME_ID);
        hook.setEventName(EVENT_1_NAME);
        hook.setName(HOOK_1_NAME);
        hook.setMd5(MD5_1);
        hooks.getHooks().add(hook);
        hook = new Hook();
        hook.setId(EVENT2_HOOK2_MD52_HASH);
        hook.setEventName(EVENT_2_NAME);
        hook.setName(HOOK_2_NAME);
        hook.setMd5(MD5_2);
        hooks.getHooks().add(hook);
        return hooks;
    }

    @Override
    protected Object getEntity(int index) {
        // not needed
        return null;
    }

}
