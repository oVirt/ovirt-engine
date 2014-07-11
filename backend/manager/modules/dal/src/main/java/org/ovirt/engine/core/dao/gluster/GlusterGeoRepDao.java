package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;

/**
 * Interface for DB operations on Gluster Geo-Replication Entities
 */
public interface GlusterGeoRepDao extends DAO {

    public void save(GlusterGeoRepSession geoRepSession);

    public void saveDetails(GlusterGeoRepSessionDetails geoRepSessionDetails);

    public void saveConfig(GlusterGeoRepSessionConfiguration geoRepSessionConfig);

    /**
     * Retrieves the GlusterGeoRepSession.
     * @param id
     * @return
     */
    public GlusterGeoRepSession getById(Guid id);

    public GlusterGeoRepSession getGeoRepSession(String sessionKey);

    public List<GlusterGeoRepSession> getGeoRepSessions(Guid masterVolumeId);

    public void remove(Guid id);

    public void updateSession(GlusterGeoRepSession geoRepSession);

    public void updateDetails(GlusterGeoRepSessionDetails geoRepSessionDetails);

    public void updateConfig(GlusterGeoRepSessionConfiguration geoRepSessionConfig);

    public List<GlusterGeoRepSessionDetails> getGeoRepSessionDetails(Guid sessionId);

    public List<GlusterGeoRepSessionConfiguration> getGeoRepSessionConfig(Guid sessionId);

 }
