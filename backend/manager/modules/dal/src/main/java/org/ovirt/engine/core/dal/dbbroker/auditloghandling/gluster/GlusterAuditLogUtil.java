package org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

/**
 * Log Helper for gluster related audit logs. Provides convenience methods to create audit logs related to a gluster
 * cluster, volume, or server. While the AuditLogDirector is sufficient for logging from regular BLL commands, this
 * class can be used for creating audit logs from other places, e.g. GlusterManager
 */
public class GlusterAuditLogUtil {
    private static GlusterAuditLogUtil instance = new GlusterAuditLogUtil();

    private GlusterAuditLogUtil() {
    }

    public static GlusterAuditLogUtil getInstance() {
        return instance;
    }

    public void logVolumeMessage(final GlusterVolumeEntity volume, final AuditLogType logType) {
        logAuditMessage(volume.getClusterId(), volume, null, logType, Collections.<String, String> emptyMap());
    }

    public void logServerMessage(final VDS server, final AuditLogType logType) {
        logAuditMessage(null, null, server, logType, Collections.<String, String> emptyMap());
    }

    public void logClusterMessage(final Guid clusterId, final AuditLogType logType) {
        logAuditMessage(clusterId, null, null, logType, Collections.<String, String> emptyMap());
    }

    public void logAuditMessage(final Guid clusterId,
            final GlusterVolumeEntity volume,
            final VDS server,
            final AuditLogType logType,
            final Map<String, String> customValues) {

        AuditLogableBase logable = new AuditLogableBase();
        logable.setVds(server);
        logable.setGlusterVolume(volume);
        logable.setVdsGroupId(clusterId == null ? Guid.Empty : clusterId);

        if (customValues != null) {
            for (String key : customValues.keySet()) {
                logable.addCustomValue(key, customValues.get(key));
            }
        }

        AuditLogDirector.log(logable, logType);
    }
}
