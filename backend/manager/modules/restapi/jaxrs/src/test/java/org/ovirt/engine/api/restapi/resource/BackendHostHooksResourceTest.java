package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;

import org.junit.Test;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostHooksResourceTest extends AbstractBackendResourceTest<Hook, HashMap<String, HashMap<String, HashMap<String, String>>>> {

    BackendHostHooksResource resource = new BackendHostHooksResource(GUIDS[0].toString());

    static final String ON_VM_START_EVENT = "on_vm_start_event";
    static final String ON_VM_STOP_EVENT = "on_vm_stop_event";
    static final String NOTIFY_ADMIN_HOOK = "notify_admin_hook";
    static final String SPECIAL_LOG_HOOK = "special_log_hook";
    static final String RELEASE_RESOURCES_HOOK = "release_resources_hook";
    static final String MD5 = "md5";
    static final String MD5_1 = "aaa";
    static final String MD5_2 = "bbb";
    static final String MD5_3 = "ccc";

    @Override
    protected HashMap<String, HashMap<String, HashMap<String, String>>> getEntity(int index) {
        HashMap<String, HashMap<String, HashMap<String, String>>> events = new HashMap<>();

        events.put(ON_VM_START_EVENT, new HashMap<>());
        events.put(ON_VM_STOP_EVENT, new HashMap<>());
        events.get(ON_VM_START_EVENT).put(NOTIFY_ADMIN_HOOK, new HashMap<>());
        events.get(ON_VM_START_EVENT).put(SPECIAL_LOG_HOOK, new HashMap<>());
        events.get(ON_VM_STOP_EVENT).put(SPECIAL_LOG_HOOK, new HashMap<>());
        events.get(ON_VM_STOP_EVENT).put(RELEASE_RESOURCES_HOOK, new HashMap<>());
        events.get(ON_VM_START_EVENT).get(NOTIFY_ADMIN_HOOK).put(MD5, MD5_1);
        events.get(ON_VM_START_EVENT).get(SPECIAL_LOG_HOOK).put(MD5, MD5_2);
        events.get(ON_VM_STOP_EVENT).get(SPECIAL_LOG_HOOK).put(MD5, MD5_2);
        events.get(ON_VM_STOP_EVENT).get(RELEASE_RESOURCES_HOOK).put(MD5, MD5_3);
        return events;
    }

    @Override
    protected void init() {
        resource.setMappingLocator(mapperLocator);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testList() throws Exception {
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetVdsHooksById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] }, getEntity(0));
        control.replay();
        Hooks hooks = resource.list();
        assertNotNull(hooks.getHooks());
        assertEquals(4, hooks.getHooks().size());
    }
}
