package org.ovirt.engine.core.bll.gluster.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterEvent;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterBrickEventSubscriber implements GlusterEventSubscriber {
    private static final String PEER = "peer";

    private static final String BRICK = "brick";

    private static final String EVENT_BRICK_CONNECTED = "BRICK_CONNECTED";

    private static final String EVENT_BRICK_DISCONNECTED = "BRICK_DISCONNECTED";

    private static final Logger log = LoggerFactory.getLogger(GlusterBrickEventSubscriber.class);

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private GlusterServerDao glusterServerDao;

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    @Inject
    private GlusterBrickDao glusterBrickDao;

    @Inject
    private BackendInternal backend;

    @Inject
    private GlusterAuditLogUtil logUtil;

    @Override
    public void processEvent(GlusterEvent event) {
        if (event == null) {
            log.debug("No event to process!");
            return;
        }
        GlusterServer glusterServer =
                glusterServerDao.getByGlusterServerUuid(Guid.createGuidFromString(event.getNodeId()));
        if (glusterServer == null) {
            log.debug("Could not determine gluster server from event '{}'", event);
            return;
        }
        VdsStatic host = vdsStaticDao.get(glusterServer.getId());
        if (host == null) {
            log.debug("No host corresponding to gluster server in '{}'", event);
            return;
        }

        GlusterVolumeEntity vol =
                glusterVolumeDao.getByName(host.getClusterId(), (String) event.getMessage().get("volume"));
        if (vol == null) {
            return;
        }

        if (event.getEvent().equalsIgnoreCase(EVENT_BRICK_DISCONNECTED)
                || event.getEvent().equalsIgnoreCase(EVENT_BRICK_CONNECTED)) {
            // get brick
            GlusterStatus status =
                    event.getEvent().equalsIgnoreCase(EVENT_BRICK_DISCONNECTED) ? GlusterStatus.DOWN : GlusterStatus.UP;
            String path = (String) event.getMessage().get(BRICK);
            String peer = (String) event.getMessage().get(PEER);
            List<VdsStatic> vdsList = vdsStaticDao.getAllForCluster(host.getClusterId());
            VdsStatic vds = vdsList.stream()
                    .filter(v -> v.getName().equals(peer)
                            || interfaceDao.getAllInterfacesForVds(v.getId())
                                    .stream()
                                    .anyMatch(iface -> peer.equals(iface.getIpv4Address())
                                            || peer.equals(iface.getIpv6Address())))
                    .findFirst()
                    .orElse(null);
            GlusterBrickEntity brick =
                    vds != null ? glusterBrickDao.getBrickByServerIdAndDirectory(vds.getId(), path) : null;
            if (brick != null) {
                glusterBrickDao.updateBrickStatus(brick.getId(), status);
                logBrickStatusChange(vol, status, brick);
            } else {
                // call sync to force updation
                log.debug("Forcing sync as brick event '{}' received that could not be resolved to brick", event);
                backend.runInternalAction(ActionType.RefreshGlusterVolumeDetails,
                        new GlusterVolumeParameters(vol.getId()));
            }
        }

    }

    private void logBrickStatusChange(GlusterVolumeEntity vol, GlusterStatus status, GlusterBrickEntity brick) {
        Map<String, String> customValues = new HashMap<>();
        customValues.put(GlusterConstants.BRICK_PATH, brick.getQualifiedName());
        customValues.put(GlusterConstants.OPTION_OLD_VALUE, brick.getStatus().toString());
        customValues.put(GlusterConstants.OPTION_NEW_VALUE, status.toString());
        customValues.put(GlusterConstants.SOURCE, GlusterConstants.SOURCE_EVENT);
        logUtil.logAuditMessage(vol.getClusterId(),
                vol.getClusterName(),
                vol,
                null,
                AuditLogType.GLUSTER_BRICK_STATUS_CHANGED,
                customValues);
    }

}
