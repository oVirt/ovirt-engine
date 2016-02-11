package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;

public class UpdateDiskProfileCommand extends UpdateProfileCommandBase<DiskProfileParameters, DiskProfile, DiskProfileValidator> {

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;

    @Inject
    private StorageQosDao storageQosDao;


    public UpdateDiskProfileCommand(DiskProfileParameters parameters) {
        super(parameters);
    }

    @Override
    protected DiskProfileValidator getProfileValidator() {
        return new DiskProfileValidator(getProfile());
    }

    @Override
    protected ProfilesDao<DiskProfile> getProfileDao() {
        return getDiskProfileDao();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfileId(),
                VdcObjectType.DiskProfile, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__TYPE__DISK_PROFILE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_DISK_PROFILE
                : AuditLogType.USER_FAILED_TO_UPDATE_DISK_PROFILE;
    }

    @Override
    protected void executeCommand() {
        // Chcek if qos has changed
        Guid oldQos = getDiskProfileDao().get(getProfileId()).getQosId();
        Guid newQos = getProfile().getQosId();

        super.executeCommand();

        // QoS did not change
        if (Objects.equals(oldQos, newQos)) {
            return;
        }

        StorageQos qos;
        if (newQos == null || Guid.Empty.equals(newQos)) {
            qos = new StorageQos();
        } else {
            qos = storageQosDao.get(newQos);
        }

        // Update policies of all running vms
        vmSlaPolicyUtils.refreshRunningVmsWithDiskProfile(getProfileId(), qos);
    }
}
