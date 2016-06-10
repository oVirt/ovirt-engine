package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;
import java.util.Date;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;
import org.ovirt.engine.core.utils.timer.DBSchedulerUtilQuartzImpl;

public abstract class ScheduleGlusterVolumeSnapshotCommandBase<T extends ScheduleGlusterVolumeSnapshotParameters> extends GlusterSnapshotCommandBase<T> {
    private GlusterVolumeSnapshotSchedule schedule;
    private boolean force;
    @Inject
    private DBSchedulerUtilQuartzImpl schedulerUtil;

    public ScheduleGlusterVolumeSnapshotCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
        this.schedule = getParameters().getSchedule();
        this.force = getParameters().getForce();

        if (this.schedule != null) {
            setClusterId(schedule.getClusterId());
            setGlusterVolumeId(schedule.getVolumeId());
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume != null && volume.getStatus() == GlusterStatus.DOWN) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
        }

        if (!GlusterUtil.getInstance().isVolumeThinlyProvisioned(volume)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_NOT_THINLY_PROVISIONED);
        }

        // Validate the scheduling dates (start and end by dates)
        Date convertedStartDate = getGlusterUtils().convertDate(schedule.getStartDate(), schedule.getTimeZone());
        Date convertedEndByDate = getGlusterUtils().convertDate(schedule.getEndByDate(), schedule.getTimeZone());

        if (schedule.getRecurrence() != null
                && schedule.getRecurrence() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN
                && schedule.getEndByDate() != null && convertedStartDate != null
                && convertedEndByDate.compareTo(convertedStartDate) <= 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_END_BY_DATE_BEFORE_START_DATE);
        }

        return true;
    }

    protected String scheduleJob() {
        // convert the execution time to engine time zone
        if (schedule.getExecutionTime() != null) {
            Time convertedTime =
                    GlusterUtil.getInstance()
                            .convertTime(schedule.getExecutionTime(), schedule.getTimeZone());
            schedule.setExecutionTime(convertedTime);
        }

        // convert the start date and end by date to the given timezone
        Date convertedStartDate = getGlusterUtils().convertDate(schedule.getStartDate(), schedule.getTimeZone());
        Date convertedEndByDate = getGlusterUtils().convertDate(schedule.getEndByDate(), schedule.getTimeZone());

        String cronExpression = GlusterUtil.getInstance().getCronExpression(schedule);
        if (cronExpression == null) {
            throw new RuntimeException("Unable to form cron expression for schedule. Invalid scheduling details.");
        }

        return getDbSchedulUtil().scheduleACronJob(new GlusterSnapshotScheduleJob(),
                "onTimer",
                new Class[] { String.class, String.class, String.class, String.class, Boolean.class },
                new Object[] { upServer.getId().toString(), getGlusterVolumeId().toString(),
                        schedule.getSnapshotNamePrefix(),
                        schedule.getSnapshotDescription(), force },
                cronExpression, convertedStartDate, convertedEndByDate);
    }

    protected GlusterVolumeSnapshotScheduleDao getGlusterVolumeSnapshotScheduleDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotScheduleDao();
    }

    protected GlusterVolumeSnapshotSchedule getSchedule() {
        return schedule;
    }

    protected boolean getForce() {
        return force;
    }

    @Override
    protected GlusterUtil getGlusterUtils() {
        return super.getGlusterUtils();
    }

    protected DBSchedulerUtilQuartzImpl getDbSchedulUtil() {
        return schedulerUtil;
    }
}
