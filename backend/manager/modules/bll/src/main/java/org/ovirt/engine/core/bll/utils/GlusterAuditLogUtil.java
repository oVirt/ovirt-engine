package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;

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
        logAuditMessage(volume.getClusterId(), volume, null, logType, Collections.emptyMap());
    }

    public void logServerMessage(final VDS server, final AuditLogType logType) {
        logAuditMessage(server == null ? Guid.Empty : server.getClusterId(),
                null,
                server,
                logType,
                Collections.emptyMap());
    }

    public void logAuditMessage(final Guid clusterId,
            final GlusterVolumeEntity volume,
            final VDS server,
            final AuditLogType logType,
            final Map<String, String> customValues) {

        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
        logable.setVds(server);
        logable.setGlusterVolume(volume);
        logable.setClusterId(clusterId);

        if (customValues != null) {
            for (Entry<String, String> entry : customValues.entrySet()) {
                logable.addCustomValue(entry.getKey(), entry.getValue());
            }
        }

        auditLogDirector.log(logable, logType);
    }

    public void logAuditMessage(final Guid clusterId,
            final GlusterVolumeEntity volume,
            final VDS server,
            final AuditLogType logType,
            final Guid brickId,
            final String brickPath) {

        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
        logable.setVds(server);
        logable.setGlusterVolume(volume);
        logable.setClusterId(clusterId);
        logable.setBrickId(brickId);
        logable.setBrickPath(brickPath);
        auditLogDirector.log(logable, logType);
    }
}
