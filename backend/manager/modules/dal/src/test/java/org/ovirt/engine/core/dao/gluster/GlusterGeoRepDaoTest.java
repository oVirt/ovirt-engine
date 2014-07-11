package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepCrawlStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class GlusterGeoRepDaoTest extends BaseDAOTestCase {

    private static final Guid SESSION_ID = new Guid("4f4f751e-549b-4e7a-aff6-32d36856c125");
    private static final Guid NONEXIST_SESSION_ID = new Guid("5e5e751e-549b-4e7a-aff6-32d36856c125");

    private GlusterGeoRepDao dao;

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
        sessionDetails.setFilesPending(100L);
        sessionDetails.setDeletesPending(200L);
        sessionDetails.setBytesPending(129832972545904L);
        sessionDetails.setFilesSynced(2343958349L);
        sessionDetails.setFilesSkipped(0L);
        return sessionDetails;
    }

    private GlusterGeoRepSessionConfiguration getGlusterGeoRepSessionConfig() {
        GlusterGeoRepSessionConfiguration sessionConfig = new GlusterGeoRepSessionConfiguration();
        sessionConfig.setId(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        sessionConfig.setKey("georep-crawl");
        sessionConfig.setValue("changelog");
        return sessionConfig;
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterGeoRepDao();
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
        List<GlusterGeoRepSessionConfiguration> fetchedSessionConfig = dao.getGeoRepSessionConfig(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals(sessionConfig, fetchedSessionConfig.get(0));
    }

    @Test
    public void testGetById() {
        GlusterGeoRepSession session = dao.getById(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertNotNull(session);
        assertEquals(FixturesTool.GLUSTER_GEOREP_SESSION_ID, session.getId());
    }

    @Test
    public void testUpdateDetails() {
        GlusterGeoRepSessionDetails sessionDetails = getGlusterGeoRepSessionDetails();
        dao.saveDetails(sessionDetails);
        Long updatedBytesPending = 567888L;
        Long updatedFilesPending = 50L;
        sessionDetails.setFilesPending(updatedFilesPending);
        sessionDetails.setCheckPointStatus("NEW");
        sessionDetails.setBytesPending(updatedBytesPending);
        dao.updateDetails(sessionDetails);
        List<GlusterGeoRepSessionDetails> fetchedSessionDetails = dao.getGeoRepSessionDetails(FixturesTool.GLUSTER_GEOREP_SESSION_ID);
        assertEquals(updatedBytesPending, fetchedSessionDetails.get(0).getBytesPending());
        assertEquals(updatedFilesPending, fetchedSessionDetails.get(0).getFilesPending());
        assertEquals("NEW", fetchedSessionDetails.get(0).getCheckPointStatus());
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

}
