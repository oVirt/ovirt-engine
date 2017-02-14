package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The return type for gluster volume heal info.
 */
public final class GlusterVolumesHealInfoReturn extends StatusReturn {
    private static final String BRICK_STATUS_CONNECTED = "Connected";
    private static final String HEAL_INFO = "healInfo";
    private static final String NAME = "name";
    private static final String BRICKS = "bricks";
    private static final String NO_OF_ENTRIES = "numberOfEntries";
    private static final String STATUS = "status";
    private static final String HOST_UUID = "hostUuid";

    private static final Logger log = LoggerFactory.getLogger(GlusterVolumesHealInfoReturn.class);
    private static final GlusterDBUtils dbUtils = Injector.get(GlusterDBUtils.class);

    private Map<Guid, Integer> unSyncedEntries = new HashMap<>();

    @SuppressWarnings("unchecked")
    public GlusterVolumesHealInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        if (getStatus().code != 0) {
            return;
        }

        getUnSyncedEntries((Map<String, Object>) innerMap.get(HEAL_INFO));
    }

    @SuppressWarnings("unchecked")
    private void getUnSyncedEntries(Map<String, Object> map) {
        Object[] healInfos = (Object[]) map.get(BRICKS);
        for (Object healInfoObj : healInfos) {
            Map<String, String> healInfo = (Map<String, String>) healInfoObj;
            String status = (String) healInfo.get(STATUS);
            Integer entries = null;
            if (BRICK_STATUS_CONNECTED.equals(status)) {
                String hostUuid = (String) healInfo.get(HOST_UUID);
                GlusterServer glusterServer = dbUtils.getServerByUuid(Guid.createGuidFromString(hostUuid));
                String brickName = (String) healInfo.get(NAME);

                String[] brickParts = brickName.split(":", -1);
                if (brickParts.length != 2) {
                    log.warn("Invalid brick representation [{}] in volume volume {}", brickName);
                    continue;
                }

                if (glusterServer == null) {
                    log.warn("Could not fetch heal info for brick '{}' - server uuid '{}' not found",
                            brickName,
                            hostUuid);
                    continue;
                }
                GlusterBrickEntity brick =
                        dbUtils.getGlusterBrickByServerUuidAndBrickDir(glusterServer.getId(), brickParts[1]);
                entries = Integer.valueOf((String) healInfo.get(NO_OF_ENTRIES));
                unSyncedEntries.put(brick.getId(), entries);
            }
        }
    }

    public Map<Guid, Integer> getUnSyncedEntries() {
        return unSyncedEntries;
    }

}
