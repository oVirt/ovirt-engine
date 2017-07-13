package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;
import org.ovirt.engine.core.di.Injector;

public class RemoveCpuProfileCommand extends RemoveProfileCommandBase<CpuProfileParameters, CpuProfile, CpuProfileValidator> {

    @Inject
    private CpuProfileDao cpuProfileDao;

    public RemoveCpuProfileCommand(CpuProfileParameters parameters, CommandContext cmdContext) {
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
        return getSucceeded() ? AuditLogType.USER_REMOVED_CPU_PROFILE
                : AuditLogType.USER_FAILED_TO_REMOVE_CPU_PROFILE;
    }

}
