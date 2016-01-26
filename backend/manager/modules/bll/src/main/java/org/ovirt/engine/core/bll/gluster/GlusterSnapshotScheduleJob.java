package org.ovirt.engine.core.bll.gluster;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterSnapshotScheduleJob implements Serializable {
    private static final long serialVersionUID = 2355384696827317365L;

    private static final Logger log = LoggerFactory.getLogger(GlusterSnapshotScheduleJob.class);
    private GlusterAuditLogUtil logUtil = getLogUtil();

    public GlusterSnapshotScheduleJob() {
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer(String serverId, String volumeId, String snapshotNamePrefix, String description, boolean force) {
        final GlusterVolumeEntity volume = getGlusterVolumeDao().getById(new Guid(volumeId));
        if (volume == null) {
            log.error("Error while creating volume snapshot. Volume is null.");
            return;
        }

        final GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setClusterId(volume.getClusterId());
        snapshot.setVolumeId(volume.getId());
        snapshot.setSnapshotName(snapshotNamePrefix);
        snapshot.setDescription(description);

        VdcReturnValueBase returnValue = getBackend().runInternalAction(VdcActionType.CreateGlusterVolumeSnapshot,
                new CreateGlusterVolumeSnapshotParameters(snapshot, force));
        if (!returnValue.getSucceeded()) {
            log.error("Error while creating snapshot for volume '{}': {}",
                    volume.getName(),
                    returnValue.getExecuteFailedMessages().toString());
            logUtil.logAuditMessage(volume.getClusterId(),
                    volume,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED,
                    new HashMap<String, String>() {
                        {
                            put(GlusterConstants.VOLUME_SNAPSHOT_NAME, snapshot.getSnapshotName());
                            put(GlusterConstants.VOLUME_NAME, volume.getName());
                        }
                    });
        }

        // Check if next schedule available, and if not delete the scheduling details from DB
        GlusterVolumeSnapshotSchedule schedule =
                getGlusterVolumeSnapshotScheduleDao().getByVolumeId(volume.getId());
        Date endDate = GlusterUtil.getInstance().convertDate(schedule.getEndByDate(), schedule.getTimeZone());
        if (endDate != null && endDate.before(new Date())) {
            getGlusterVolumeSnapshotScheduleDao().removeByVolumeId(volume.getId());
            logUtil.logAuditMessage(volume.getClusterId(),
                    volume,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_DELETED,
                    new HashMap<String, String>() {
                        {
                            put(GlusterConstants.VOLUME_NAME, volume.getName());
                        }
                    });
        }
    }

    protected GlusterVolumeSnapshotScheduleDao getGlusterVolumeSnapshotScheduleDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotScheduleDao();
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance().getGlusterVolumeDao();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected GlusterAuditLogUtil getLogUtil() {
        return GlusterAuditLogUtil.getInstance();
    }
}
