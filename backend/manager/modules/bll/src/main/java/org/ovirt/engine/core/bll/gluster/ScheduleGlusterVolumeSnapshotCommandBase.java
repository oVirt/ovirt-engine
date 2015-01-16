package org.ovirt.engine.core.bll.gluster;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.gluster.ScheduleGlusterVolumeSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;
import org.ovirt.engine.core.utils.timer.DBSchedulerUtilQuartzImpl;

public abstract class ScheduleGlusterVolumeSnapshotCommandBase<T extends ScheduleGlusterVolumeSnapshotParameters> extends GlusterSnapshotCommandBase<T> {
    private GlusterVolumeSnapshotSchedule schedule;
    private boolean force;

    public ScheduleGlusterVolumeSnapshotCommandBase(T params) {
        super(params);
        this.schedule = getParameters().getSchedule();
        this.force = getParameters().getForce();

        if (this.schedule != null) {
            setVdsGroupId(schedule.getClusterId());
            setGlusterVolumeId(schedule.getVolumeId());
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume != null && volume.getStatus() == GlusterStatus.DOWN) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
        }

        if (!GlusterUtil.getInstance().isVolumeThinlyProvisioned(volume)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_NOT_THINLY_PROVISIONED);
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
        Date convertedStartDate = convertDate(schedule.getStartDate(), schedule.getTimeZone());
        Date convertedEndByDate = convertDate(schedule.getEndByDate(), schedule.getTimeZone());

        String cronExpression = GlusterUtil.getInstance().getCronExpression(schedule);
        if (cronExpression == null)
            return null;

        return DBSchedulerUtilQuartzImpl.getInstance().scheduleACronJob(new GlusterSnapshotScheduleJob(),
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

    private Date convertDate(Date inDate, String tZone) {
        if (inDate == null) {
            return null;
        }

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String formattedStartDate = format.format(inDate);

        format.setTimeZone(TimeZone.getTimeZone(tZone));
        try {
            return format.parse(formattedStartDate);
        } catch (Exception ex) {
            log.error("Error while converting the date to engine time zone");
            return null;
        }
    }
}
