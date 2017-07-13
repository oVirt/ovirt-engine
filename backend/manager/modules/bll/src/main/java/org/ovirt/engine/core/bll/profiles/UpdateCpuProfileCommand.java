package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.di.Injector;

public class UpdateCpuProfileCommand extends UpdateProfileCommandBase<CpuProfileParameters, CpuProfile, CpuProfileValidator> {

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;

    @Inject
    private CpuQosDao cpuQosDao;
    @Inject
    private CpuProfileDao cpuProfileDao;

    public UpdateCpuProfileCommand(CpuProfileParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected CpuProfileValidator getProfileValidator() {
        return Injector.injectMembers(new CpuProfileValidator(getProfile()));
    }

    @Override
    protected ProfilesDao<CpuProfile> getProfileDao() {
        return cpuProfileDao;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfileId(),
                VdcObjectType.CpuProfile, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__CPU_PROFILE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_CPU_PROFILE
                : AuditLogType.USER_FAILED_TO_UPDATE_CPU_PROFILE;
    }

    @Override
    protected void executeCommand() {
        // Chcek if qos has changed
        Guid oldQos = cpuProfileDao.get(getProfileId()).getQosId();
        Guid newQos = getProfile().getQosId();

        super.executeCommand();

        // QoS did not change
        if (Objects.equals(oldQos, newQos)) {
            return;
        }

        CpuQos qos;
        if (newQos == null || Guid.Empty.equals(newQos)) {
            qos = new CpuQos();
            qos.setCpuLimit(100);
        } else {
            qos = cpuQosDao.get(newQos);
        }

        // Update policies of all running vms
        if (getSucceeded()) {
            vmSlaPolicyUtils.refreshRunningVmsWithCpuProfile(getProfileId(), qos);
        }
    }
}
