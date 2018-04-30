package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;


public class GlusterServerDaoTest extends BaseDaoTestCase<GlusterServerDao> {
    @Test
    public void testSave() {
        GlusterServer newEntity = new GlusterServer();
        newEntity.setId(FixturesTool.VDS_GLUSTER_SERVER2);
        newEntity.setGlusterServerUuid(FixturesTool.GLUSTER_SERVER_UUID2);
        newEntity.setPeerStatus(PeerStatus.CONNECTED);

        dao.save(newEntity);
        GlusterServer entity = dao.getByServerId(newEntity.getId());
        assertEquals(newEntity, entity);
    }

    @Test
    public void testGetById() {
        GlusterServer entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(FixturesTool.GLUSTER_BRICK_SERVER1, entity.getId());
        assertEquals(FixturesTool.GLUSTER_SERVER_UUID1, entity.getGlusterServerUuid());
    }

    @Test
    public void testGetByGlusterServerUuid() {
        GlusterServer entity = dao.getByGlusterServerUuid(FixturesTool.GLUSTER_SERVER_UUID1);
        assertNotNull(entity);
        assertEquals(FixturesTool.GLUSTER_BRICK_SERVER1, entity.getId());
        assertEquals(FixturesTool.GLUSTER_SERVER_UUID1, entity.getGlusterServerUuid());
    }

    @Test
    public void testRemove() {
        dao.remove(FixturesTool.GLUSTER_BRICK_SERVER1);
        GlusterServer entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNull(entity);
    }

    @Test
    public void testRemoveByGlusterServerUuid() {
        dao.removeByGlusterServerUuid(FixturesTool.GLUSTER_SERVER_UUID1);
        GlusterServer entity = dao.getByGlusterServerUuid(FixturesTool.GLUSTER_SERVER_UUID1);
        assertNull(entity);
    }

    @Test
    public void testUpdateGlusterServerUuid() {
        GlusterServer entityToUpdate = new GlusterServer(FixturesTool.GLUSTER_BRICK_SERVER1, FixturesTool.GLUSTER_SERVER_UUID_NEW);
        dao.update(entityToUpdate);
        GlusterServer entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(FixturesTool.GLUSTER_SERVER_UUID_NEW, entity.getGlusterServerUuid());
    }

    @Test
    public void testAddKnownAddresses() {
        GlusterServer entityToUpdate = new GlusterServer(FixturesTool.GLUSTER_BRICK_SERVER1, FixturesTool.GLUSTER_SERVER_UUID_NEW);
        dao.update(entityToUpdate);
        dao.addKnownAddress(FixturesTool.GLUSTER_BRICK_SERVER1, "a.1");
        GlusterServer entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(1, entity.getKnownAddresses().size());
        assertEquals("a.1", entity.getKnownAddresses().get(0));
        dao.addKnownAddress(FixturesTool.GLUSTER_BRICK_SERVER1, "a.2");
        entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(2, entity.getKnownAddresses().size());
        assertEquals("a.2", entity.getKnownAddresses().get(1));
    }

    @Test
    public void testUpdateKnownAddresses() {
        GlusterServer entityToUpdate = new GlusterServer(FixturesTool.GLUSTER_BRICK_SERVER1, FixturesTool.GLUSTER_SERVER_UUID_NEW);
        dao.update(entityToUpdate);
        dao.addKnownAddress(FixturesTool.GLUSTER_BRICK_SERVER1, "a.1");
        GlusterServer entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(1, entity.getKnownAddresses().size());
        assertEquals("a.1", entity.getKnownAddresses().get(0));
        ArrayList<String> knownAddresses = new ArrayList<>();
        knownAddresses.add("a.2");
        dao.updateKnownAddresses(FixturesTool.GLUSTER_BRICK_SERVER1, knownAddresses);
        entity = dao.getByServerId(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertNotNull(entity);
        assertEquals(1, entity.getKnownAddresses().size());
        assertEquals("a.2", entity.getKnownAddresses().get(0));
    }
}
