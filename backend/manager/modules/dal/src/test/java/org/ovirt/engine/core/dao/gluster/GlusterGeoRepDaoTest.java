package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepCrawlStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class GlusterGeoRepDaoTest extends BaseDaoTestCase<GlusterGeoRepDao> {

    private static final String GEOREP_CONFIG_CRAWL = "georep-crawl";
    private static final Guid SESSION_ID = new Guid("4f4f751e-549b-4e7a-aff6-32d36856c125");
    private static final Guid NONEXIST_SESSION_ID = new Guid("5e5e751e-549b-4e7a-aff6-32d36856c125");

    private GlusterGeoRepSession getGlusterGeoRepSession() {
        GlusterGeoRepSession georepSession = new GlusterGeoRepSession();
        georepSession.setId(SESSION_ID);
        georepSession.setMasterVolumeId(FixturesTool.GLUSTER_VOLUME_UUID1);
        georepSession.setSlaveHostName("remoteHost");
        georepSession.setSlaveVolumeName("remoteVol");
        georepSession.setSlaveNodeUuid(FixturesTool.GLUSTER_SERVER_UUID1);
        georepSession.setSessionKey("remoteHost:remoteVol");
        georepSession.setStatus(GeoRepSessionStatus.ACTIVE);
        return georepSession;
    }

    private GlusterGeoRepSessionDetails getGlusterGeoRepSessionDetails() {
        GlusterGeoRepSessionDetails sessionDetails = new GlusterGeoRepSessionDetails();
        sessionDetails.setSessionId(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        sessionDetails.setMasterBrickId(FixturesTool.GLUSTER_BRICK_UUID1);
        sessionDetails.setSlaveHostName("remoteHost");
        sessionDetails.setSlaveNodeUuid(FixturesTool.GLUSTER_SERVER_UUID1);
        sessionDetails.setStatus(GeoRepSessionStatus.ACTIVE);
        sessionDetails.setCheckPointStatus("NA");
        sessionDetails.setCrawlStatus(GeoRepCrawlStatus.CHANGELOG_CRAWL);
        sessionDetails.setDataOpsPending(100L);
        sessionDetails.setMetaOpsPending(40L);
        sessionDetails.setEntryOpsPending(10L);
        sessionDetails.setFailures(0L);
        sessionDetails.setCheckpointCompleted(false);
        sessionDetails.setCheckPointTime(new Date());
        sessionDetails.setLastSyncedAt(new Date());
        return sessionDetails;
    }

    private GlusterGeoRepSessionConfiguration getGlusterGeoRepSessionConfig() {
        GlusterGeoRepSessionConfiguration sessionConfig = new GlusterGeoRepSessionConfiguration();
        sessionConfig.setId(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        sessionConfig.setKey(GEOREP_CONFIG_CRAWL);
        sessionConfig.setDescription("Geo-replication session  crawl");
        sessionConfig.setValue("changelog");
        return sessionConfig;
    }

    @Test
    public void testSave() {
        GlusterGeoRepSession newSession = getGlusterGeoRepSession();
        dao.save(newSession);
        GlusterGeoRepSession session = dao.getById(newSession.getId());
        assertEquals(newSession, session);
    }

    @Test
    public void testSaveDetails() {
        GlusterGeoRepSessionDetails sessionDetails = getGlusterGeoRepSessionDetails();
        dao.saveDetails(sessionDetails);
        List<GlusterGeoRepSessionDetails> fetchedSessionDetails = dao.getGeoRepSessionDetails(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals(sessionDetails, fetchedSessionDetails.get(0));
    }

    @Test
    public void testGetDetailsByInvalidId() {
        List<GlusterGeoRepSessionDetails> fetchedSessionDetails = dao.getGeoRepSessionDetails(NONEXIST_SESSION_ID);
        assertEquals(0, fetchedSessionDetails.size());
    }

    @Test
    public void testGetSessionByInvalidId() {
        GlusterGeoRepSession session = dao.getById(NONEXIST_SESSION_ID);
        assertNull(session);
    }

    @Test
    public void testSaveConfig() {
        GlusterGeoRepSessionConfiguration sessionConfig = getGlusterGeoRepSessionConfig();
        dao.saveConfig(sessionConfig);
        List<GlusterGeoRepSessionConfiguration> fetchedSessionConfigList =
                dao.getGeoRepSessionConfig(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals(sessionConfig, fetchedSessionConfigList.get(0));
        GlusterGeoRepSessionConfiguration fetchedSessionConfig =
                dao.getGeoRepSessionConfigByKey(FixturesTool.GLUSTER_GEOREP_SESSION_ID, GEOREP_CONFIG_CRAWL);
        assertEquals(sessionConfig, fetchedSessionConfig);
    }

    @Test
    public void testGetById() {
        GlusterGeoRepSession session = dao.getById(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertNotNull(session);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, session.getId());
    }

    @Test
    public void testGetGeoRepSessionBySlaveVolume() {
        GlusterGeoRepSession session = dao.getGeoRepSessionBySlaveVolume(FixturesTool.GLUSTER_GEOREP_SESSION_SLAVE_VOLUME_ID);
        assertNotNull(session);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, session.getId());
    }

    @Test
    public void testUpdateDetails() {
        GlusterGeoRepSessionDetails sessionDetails = getGlusterGeoRepSessionDetails();
        dao.saveDetails(sessionDetails);
        Long entryOpsPending = 567888L;
        Long dataOpsPending = 50L;
        sessionDetails.setEntryOpsPending(entryOpsPending);
        sessionDetails.setCheckPointStatus("NEW");
        sessionDetails.setDataOpsPending(dataOpsPending);
        dao.updateDetails(sessionDetails);
        List<GlusterGeoRepSessionDetails> fetchedSessionDetails = dao.getGeoRepSessionDetails(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals(entryOpsPending, fetchedSessionDetails.get(0).getEntryOpsPending());
        assertEquals(dataOpsPending, fetchedSessionDetails.get(0).getDataOpsPending());
        assertEquals("NEW", fetchedSessionDetails.get(0).getCheckPointStatus());
    }

    @Test
    public void testGetGlusterGeoRepSessionUnSetConfig() {
        GlusterGeoRepSessionConfiguration sessionConfig = getGlusterGeoRepSessionConfig();
        dao.saveConfig(sessionConfig);
        List<GlusterGeoRepSessionConfiguration> unsetSessionConfig = dao.getGlusterGeoRepSessionUnSetConfig(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals("use_meta_volume", unsetSessionConfig.get(0).getKey());
    }

    @Test
    public void testUpdateConfig() {
        GlusterGeoRepSessionConfiguration sessionConfig = getGlusterGeoRepSessionConfig();
        dao.saveConfig(sessionConfig);
        sessionConfig.setValue("NEW_VAL");
        dao.updateConfig(sessionConfig);
        List<GlusterGeoRepSessionConfiguration> fetchedSessionConfig = dao.getGeoRepSessionConfig(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals("NEW_VAL", fetchedSessionConfig.get(0).getValue());
    }

    @Test
    public void testGetAllInCluster() {
        List<GlusterGeoRepSession> fetchedSessions = dao.getGeoRepSessionsInCluster(FixturesTool.GLUSTER_CLUSTER_ID);
        assertEquals(1, fetchedSessions.size());
    }

    @Test
    public void testGetAllSessions() {
        List<GlusterGeoRepSession> sessions = dao.getAllSessions();
        assertNotNull(sessions);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, sessions.get(0).getId());
    }

    @Test
    public void testRemove() {
        dao.remove(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        List<GlusterGeoRepSession> fetchedSessions = dao.getGeoRepSessionsInCluster(FixturesTool.GLUSTER_CLUSTER_ID);
        assertEquals(0, fetchedSessions.size());
    }

    @Test
    public void testGetBySlaveHostAndVolume() {
        GlusterGeoRepSession session = dao.getGeoRepSession(FixturesTool.GLUSTER_VOLUME_UUID1,
                new Guid("44f645f6-3fe9-4b35-a30c-be0d1a835ea8"), "slave-replica");
        assertNotNull(session);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, session.getId());
    }

    @Test
    public void testGetBySlaveHostNameAndVolume() {
        GlusterGeoRepSession session = dao.getGeoRepSession(FixturesTool.GLUSTER_VOLUME_UUID1,
                "192.168.122.17", "slave-replica");
        assertNotNull(session);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, session.getId());
    }
}
