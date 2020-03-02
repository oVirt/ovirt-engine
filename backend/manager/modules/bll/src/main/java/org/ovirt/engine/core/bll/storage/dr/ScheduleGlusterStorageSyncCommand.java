package org.ovirt.engine.core.bll.storage.dr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.OvirtGlusterSchedulingService;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageSyncScheduleParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDRDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@NonTransactiveCommandAttribute
public class ScheduleGlusterStorageSyncCommand<T extends StorageSyncScheduleParameters> extends CommandBase<T> {

    @Inject
    private OvirtGlusterSchedulingService schedulingService;

    @Inject
    private StorageDomainDRDao storageDomainDRDao;

    @Inject
    private GlusterGeoRepDao geoRepDao;

    public ScheduleGlusterStorageSyncCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    private GlusterGeoRepSession getGeoRepSession() {
        return geoRepDao.getById(getParameters().getGeoRepSessionId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SCHEDULE);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE_DOMAIN_DR);
        super.setActionMessageParameters();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getStorageDomainId().toString() + ";" +getGeoRepSession().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_STORAGE_DOMAIN_SYNC,
                        new LockMessage(EngineMessage.STORAGE_DOMAIN_SYNC_SCHEDULING_IN_PROGRESS)));
    }

    @Override
    protected boolean validate() {
        if (getParameters().getSchedule() == null
                || (getParameters().getSchedule().getFrequency() != StorageSyncSchedule.Frequency.NONE
                        && StringUtils.isEmpty(getParameters().getSchedule().toCronExpression()))) {
            return failValidation(EngineMessage.VALIDATION_INVALID_SCHEDULE);
        }
        if (getStorageDomain() == null) {
            return failValidation(EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST);
        }
        if (getGeoRepSession() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_INVALID);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        StorageDomainDR storageDomainDR =
                storageDomainDRDao.get(getParameters().getStorageDomainId(), getParameters().getGeoRepSessionId());
        if (storageDomainDR == null) {
            storageDomainDR = new StorageDomainDR();
            storageDomainDR.setStorageDomainId(getParameters().getStorageDomainId());
            storageDomainDR.setGeoRepSessionId(getParameters().getGeoRepSessionId());
        } else if (storageDomainDR.getJobId() != null) {
            // delete existing job
            schedulingService.deleteScheduledJob(storageDomainDR.getJobId());
        }
        if (getParameters().getSchedule().getFrequency() != StorageSyncSchedule.Frequency.NONE) {
            Guid jobId = schedulingService.schedule(GlusterStorageDomainDRSyncJob.class.getName(),
                    Guid.Empty,
                    "syncData",
                    Arrays.asList(new String[] { String.class.getName(), String.class.getName() }),
                    Arrays.asList(new String[] { getParameters().getStorageDomainId().toString(),
                            getParameters().getGeoRepSessionId().toString() }),
                    getParameters().getSchedule().toCronExpression(),
                    null,
                    null,
                    null);
            storageDomainDR.setScheduleCronExpression(getParameters().getSchedule().toCronExpression());
            storageDomainDR.setJobId(jobId);
            storageDomainDRDao.saveOrUpdate(storageDomainDR);
        } else {
            // FREQUENCY = NONE - no sync scheduled, so delete this
            storageDomainDRDao.remove(storageDomainDR.getStorageDomainId(), storageDomainDR.getGeoRepSessionId());
        }
        setSucceeded(true);
    }

    @Override
    public List getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
