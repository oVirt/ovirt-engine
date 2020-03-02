package org.ovirt.engine.core.bll.gluster;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.scheduling.OnTimerMethodAnnotation;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterSnapshotScheduleJob implements Serializable {
    private static final long serialVersionUID = 2355384696827317365L;

    private static final Logger log = LoggerFactory.getLogger(GlusterSnapshotScheduleJob.class);

    private final GlusterAuditLogUtil logUtil;

    private final GlusterVolumeSnapshotScheduleDao glusterVolumeSnapshotScheduleDao;

    private final GlusterVolumeDao glusterVolumeDao;

    private final BackendInternal backend;

    private final GlusterUtil glusterUtil;

    public GlusterSnapshotScheduleJob() {
        super();
        // The @Inject annotation does not work when the GlusterSnapshotScheduleJob
        // is instantiated as part of Quartz trigger - even when the class passed to
        // quartz is instantiated using Injector.injectMembers.
        // TBD - change when quartz classes use CDI too
        backend = Injector.get(BackendInternal.class);
        glusterVolumeDao = Injector.get(GlusterVolumeDao.class);
        glusterUtil = Injector.get(GlusterUtil.class);
        glusterVolumeSnapshotScheduleDao = Injector.get(GlusterVolumeSnapshotScheduleDao.class);
        logUtil = Injector.get(GlusterAuditLogUtil.class);
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer(String serverId,
            String volumeId,
            String snapshotNamePrefix,
            String description,
            String force) {
        final GlusterVolumeEntity volume = glusterVolumeDao.getById(new Guid(volumeId));
        if (volume == null) {
            log.error("Error while creating volume snapshot. Volume is null.");
            return;
        }

        final GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setClusterId(volume.getClusterId());
        snapshot.setVolumeId(volume.getId());
        snapshot.setSnapshotName(snapshotNamePrefix);
        snapshot.setDescription(description);

        ActionReturnValue returnValue = backend.runInternalAction(ActionType.CreateGlusterVolumeSnapshot,
                new CreateGlusterVolumeSnapshotParameters(snapshot, Boolean.parseBoolean(force)));
        if (!returnValue.getSucceeded()) {
            log.error("Error while creating snapshot for volume '{}': {}",
                    volume.getName(),
                    returnValue.getExecuteFailedMessages().toString());
            Map<String, String> customValues = new HashMap<>();
            customValues.put(GlusterConstants.VOLUME_SNAPSHOT_NAME, snapshot.getSnapshotName());
            customValues.put(GlusterConstants.VOLUME_NAME, volume.getName());
            logUtil.logAuditMessage(volume.getClusterId(),
                    volume.getClusterName(),
                    volume,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED, customValues);
        }

        // Check if next schedule available, and if not delete the scheduling details from DB
        GlusterVolumeSnapshotSchedule schedule = glusterVolumeSnapshotScheduleDao.getByVolumeId(volume.getId());
        Date endDate = glusterUtil.convertDate(schedule.getEndByDate(), schedule.getTimeZone());
        if (endDate != null && endDate.before(new Date())) {
            glusterVolumeSnapshotScheduleDao.removeByVolumeId(volume.getId());
            logUtil.logAuditMessage(volume.getClusterId(),
                    volume.getClusterName(),
                    volume,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_DELETED, Collections.singletonMap(GlusterConstants.VOLUME_NAME, volume.getName()));
        }
    }
}
