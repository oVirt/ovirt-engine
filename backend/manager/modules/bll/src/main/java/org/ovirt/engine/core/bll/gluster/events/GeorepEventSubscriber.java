package org.ovirt.engine.core.bll.gluster.events;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.gluster.GlusterGeoRepSyncJob;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeorepEventSubscriber implements GlusterEventSubscriber {
    private static final String SLAVE_VOLUME = "slave_volume";
    private static final String SLAVE_HOST = "slave_host";
    private static final String MASTER_VOLUME = "master_volume";
    private static final String EVENT_GEOREP_CHECKPOINT_COMPLETED = "EVENT_GEOREP_CHECKPOINT_COMPLETED";
    private static final Logger log = LoggerFactory.getLogger(GeorepEventSubscriber.class);
    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private GlusterServerDao glusterServerDao;

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    @Inject
    private GlusterGeoRepDao geoRepDao;

    @Inject
    private GlusterGeoRepSyncJob geoRepSyncJob;

    @Override
    public void processEvent(GlusterEvent event) {
        if (event == null) {
            log.debug("No event to process!");
            return;
        }
        GlusterServer glusterServer = glusterServerDao.getByGlusterServerUuid(Guid.createGuidFromString(event.getNodeId()));
        if (glusterServer == null) {
            log.debug("Could not determine gluster server from event '{}'", event);
            return;
        }
        VdsStatic host = vdsStaticDao.get(glusterServer.getId());
        if (host == null) {
            log.debug("No host corresponding to gluster server in '{}'", event);
            return;
        }
        if (event.getEvent().equalsIgnoreCase(EVENT_GEOREP_CHECKPOINT_COMPLETED)) {
            GlusterVolumeEntity masterVol =
                    glusterVolumeDao.getByName(host.getClusterId(), (String) event.getMessage().get(MASTER_VOLUME));
            if (masterVol == null) {
                log.debug("Could not determine master volume from event '{}'", event);
                return;
            }

            GlusterGeoRepSession session = geoRepDao.getGeoRepSession(masterVol.getId(),
                    (String) event.getMessage().get(SLAVE_HOST),
                    (String) event.getMessage().get(SLAVE_VOLUME));
            // update and save checkpoint details
            // Due to https://bugzilla.redhat.com/show_bug.cgi?id=1388755, node details are not available.
            // hence forcing sync for now.
            if (session != null) {
                log.debug("received event for session '{}'", session.getSessionKey());
            }
            geoRepSyncJob.refreshGeoRepDataForVolume(masterVol);
        }

    }

}
