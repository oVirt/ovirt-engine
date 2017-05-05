package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;

/**
 * Log Helper for gluster related audit logs. Provides convenience methods to create audit logs related to a gluster
 * cluster, volume, or server. While the AuditLogDirector is sufficient for logging from regular BLL commands, this
 * class can be used for creating audit logs from other places, e.g. GlusterManager
 */
@Singleton
public class GlusterAuditLogUtil {
    @Inject
    private AuditLogDirector auditLogDirector;

    public void logVolumeMessage(final GlusterVolumeEntity volume, final AuditLogType logType) {
        logAuditMessage(volume.getClusterId(), volume.getClusterName(), volume, null, logType, Collections.emptyMap());
    }

    public void logServerMessage(final VDS server, final AuditLogType logType) {
        logAuditMessage(server.getClusterId(),
                server.getClusterName(),
                null,
                server,
                logType, Collections.emptyMap());
    }

    public void logAuditMessage(final Guid clusterId,
            String clusterName,
            final GlusterVolumeEntity volume,
            final VDS server,
            final AuditLogType logType,
            final Map<String, String> customValues) {

        AuditLogable logable = createEvent(volume, server, clusterId, clusterName);
        if (customValues != null) {
            customValues.entrySet().forEach(e -> logable.addCustomValue(e.getKey(), e.getValue()));
        }

        auditLogDirector.log(logable, logType);
    }

    public void logAuditMessage(final GlusterVolumeEntity volume,
            final AuditLogType logType,
            final Guid brickId,
            final String brickPath) {

        AuditLogable logable = createEvent(volume, null, volume.getClusterId(), volume.getClusterName());
        logable.setBrickId(brickId);
        logable.setBrickPath(brickPath);
        auditLogDirector.log(logable, logType);
    }

    private AuditLogable createEvent(GlusterVolumeEntity volume, VDS server, Guid clusterId, String clusterName) {
        AuditLogable logable = new AuditLogableImpl();
        if (server != null) {
            logable.setVdsId(server.getId());
            logable.setVdsName(server.getName());
        }

        if (volume != null) {
            logable.setGlusterVolumeId(volume.getId());
            logable.setGlusterVolumeName(volume.getName());
        }

        logable.setClusterId(clusterId);
        logable.setClusterName(clusterName);
        return logable;
    }
}
