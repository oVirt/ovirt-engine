package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class GlusterHooksDaoTest extends BaseDaoTestCase {
    private static final String GLUSTER_COMMAND = "start volume";
    private static final GlusterHookStage STAGE = GlusterHookStage.POST;
    private static final String EXISTING_HOOK_NAME = "28cifs_config";
    private static final String HOOK_NAME = "georep";
    private static final Guid SERVER_ID = new Guid("2001751e-549b-4e7a-aff6-32d36856c125");
    private static final Guid SERVER_ID2 = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final String CHECKSUM = "0127f712fc008f857e77a2f3f179c710";
    private static final String CONTENT = "Sample text for hook content ";
    private static final String CHECKSUM_HOOK1_SERVER1 = "bf35fa420d3e0f669e27b337062bf19f510480d4";
    private GlusterHooksDao dao;

    private GlusterHookEntity getGlusterHook() {
        GlusterHookEntity hook = new GlusterHookEntity();
        hook.setId(FixturesTool.HOOK_ID);
        hook.setClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        hook.setGlusterCommand(GLUSTER_COMMAND);
        hook.setStage(GlusterHookStage.POST);
        hook.setName(HOOK_NAME);
        hook.setChecksum(CHECKSUM);
        hook.setStatus(GlusterHookStatus.DISABLED);
        hook.setContentType(GlusterHookContentType.TEXT);
        hook.setConflictValue(false, false, false);
        return hook;
    }

    private GlusterServerHook getGlusterServerHook(Guid serverId,
            GlusterHookStatus status,
            GlusterHookContentType contentType,
            String checksum) {
        GlusterServerHook serverHook = new GlusterServerHook();
        serverHook.setHookId(FixturesTool.HOOK_ID);
        serverHook.setServerId(serverId);
        serverHook.setStatus(status);
        serverHook.setContentType(contentType);
        serverHook.setChecksum(checksum);
        return serverHook;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterHooksDao();
    }

    @Test
    public void testSave() {
        GlusterHookEntity newHook = getGlusterHook();
        newHook.setId(FixturesTool.NEW_HOOK_ID);
        dao.save(newHook);
        GlusterHookEntity hook = dao.getById(newHook.getId());
        assertTrue(newHook.equals(hook));
    }

    @Test
    public void testGetById() {
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID);
        assertNotNull(hook);
        assertEquals(FixturesTool.HOOK_ID, hook.getId());
    }

    @Test
    public void testGetByIdAll() {
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID, true);
        assertNotNull(hook);
        assertTrue(hook.getServerHooks().size() == 2);
        assertEquals(FixturesTool.HOOK_ID, hook.getId());
    }

    @Test
    public void testGetHook() {
        GlusterHookEntity hook = dao.getGlusterHook(FixturesTool.GLUSTER_CLUSTER_ID, GLUSTER_COMMAND, STAGE, EXISTING_HOOK_NAME);
        assertNotNull(hook);
        assertEquals(EXISTING_HOOK_NAME, hook.getName());
    }

    @Test
    public void testGetServerHooks() {
        GlusterServerHook serverHook1 = getGlusterServerHook(SERVER_ID,
                GlusterHookStatus.ENABLED, GlusterHookContentType.TEXT, CHECKSUM_HOOK1_SERVER1);
        GlusterServerHook serverHook2 = getGlusterServerHook(SERVER_ID2,
                GlusterHookStatus.MISSING, null, null);
        List<GlusterServerHook> serverHooks  = dao.getGlusterServerHooks(FixturesTool.HOOK_ID);
        assertNotNull(serverHooks);
        assertEquals(2, serverHooks.size());
        assertTrue(serverHooks.contains(serverHook1));
        assertTrue(serverHooks.contains(serverHook2));
    }

    @Test
    public void testGetByClusterId() {
        List<GlusterHookEntity> hooks = dao.getByClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        assertNotNull(hooks);
        assertEquals(2, hooks.size());
    }

    @Test
    public void testGetByNullClusterId() {
        List<GlusterHookEntity> hooks = dao.getByClusterId(null);
        assertNotNull(hooks);
        assertTrue(hooks.isEmpty());
    }

    @Test
    public void testRemove() {
        dao.remove(FixturesTool.HOOK_ID);
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID);
        assertNull(hook);
        GlusterServerHook serverhook = dao.getGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        assertNull(serverhook);
    }

    @Test
    public void testRemoveAll() {
        List<Guid> hookIds = new ArrayList<>();
        hookIds.add(FixturesTool.HOOK_ID);
        hookIds.add(FixturesTool.HOOK_ID2);
        dao.removeAll(hookIds);
        List<GlusterHookEntity> hooks = dao.getByClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        assertNotNull(hooks);
        assertTrue(hooks.isEmpty());
    }

    @Test
    public void testRemoveAllInCluster() {
        List<GlusterHookEntity> originalHookList = dao.getByClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        assertFalse(originalHookList.isEmpty());
        dao.removeAllInCluster(FixturesTool.GLUSTER_CLUSTER_ID);
        List<GlusterHookEntity> hooks = dao.getByClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        assertNotNull(hooks);
        assertTrue(hooks.isEmpty());
    }

    public void testRemoveAllButOne() {
        GlusterHookEntity newHook = getGlusterHook();
        newHook.setId(FixturesTool.NEW_HOOK_ID);
        dao.save(newHook);

        List<Guid> hookIds = new ArrayList<>();
        hookIds.add(FixturesTool.HOOK_ID);
        hookIds.add(FixturesTool.HOOK_ID2);
        dao.removeAll(hookIds);
        List<GlusterHookEntity> hooks = dao.getByClusterId(FixturesTool.GLUSTER_CLUSTER_ID);
        assertNotNull(hooks);
        assertEquals(1, hooks.size());
    }

    @Test
    public void testRemoveAllServerHooks() {
        dao.removeGlusterServerHooks(FixturesTool.HOOK_ID);
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID, true);
        assertTrue(hook.getServerHooks().isEmpty());
    }

    @Test
    public void testRemoveServerHook() {
        dao.removeGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        GlusterServerHook serverhook = dao.getGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        assertNull(serverhook);
    }

    @Test
    public void testUpdateGlusterHookStatus() {
        dao.updateGlusterHookStatus(FixturesTool.HOOK_ID, GlusterHookStatus.ENABLED);
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID);
        assertNotNull(hook);
        assertEquals(GlusterHookStatus.ENABLED, hook.getStatus());
    }

    @Test
    public void testUpdateGlusterServerHookStatus() {
        GlusterServerHook serverhookExisting = dao.getGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        assertEquals(GlusterHookStatus.ENABLED, serverhookExisting.getStatus());
        dao.updateGlusterServerHookStatus(FixturesTool.HOOK_ID, SERVER_ID, GlusterHookStatus.DISABLED);
        GlusterServerHook serverhookUpdated = dao.getGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        assertNotNull(serverhookUpdated);
        assertEquals(GlusterHookStatus.DISABLED, serverhookUpdated.getStatus());
    }

    @Test
    public void updateGlusterHookConflictStatus() {
        dao.updateGlusterHookConflictStatus(FixturesTool.HOOK_ID, 0);
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID);
        assertNotNull(hook);
        assertEquals(Integer.valueOf(0), hook.getConflictStatus());
    }

    @Test
    public void testUpdateGlusterHookChecksum() {
        dao.updateGlusterServerHookChecksum(FixturesTool.HOOK_ID, SERVER_ID, CHECKSUM);
        GlusterServerHook serverhook = dao.getGlusterServerHook(FixturesTool.HOOK_ID, SERVER_ID);
        assertNotNull(serverhook);
        assertEquals(GlusterHookStatus.ENABLED, serverhook.getStatus());
        assertEquals(CHECKSUM, serverhook.getChecksum());
    }

    @Test
    public void updateGlusterHookContent() {
        String updateContent = "Updated script content to test";
        String updateChecksum = "ddffeef712fc008f857e77a2f3f179c710";
        dao.updateGlusterHookContent(FixturesTool.HOOK_ID, updateChecksum, updateContent);
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID, true);
        assertNotNull(hook);
        assertEquals(updateContent, hook.getContent());
        assertEquals(updateChecksum, hook.getChecksum());
    }

    @Test
    public void testUpdateGlusterHook() {
        GlusterHookEntity existingHook = getGlusterHook();
        existingHook.setName(EXISTING_HOOK_NAME);
        dao.updateGlusterHook(getGlusterHook());
        GlusterHookEntity hook = dao.getById(FixturesTool.HOOK_ID);
        assertNotNull(hook);
        assertEquals(existingHook, hook);
    }

    @Test
    public void getGlusterHookContent() {
        String content = dao.getGlusterHookContent(FixturesTool.HOOK_ID);
        assertNotNull(content);
        assertEquals(content, CONTENT);
    }

}
