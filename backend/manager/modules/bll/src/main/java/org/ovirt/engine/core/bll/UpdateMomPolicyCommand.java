package org.ovirt.engine.core.bll;


import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
public class UpdateMomPolicyCommand extends VdsCommand<VdsActionParameters> {

    public UpdateMomPolicyCommand(VdsActionParameters vdsActionParameters) {
        super(vdsActionParameters);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        try {
            succeeded = runVdsCommand(VDSCommandType.SetMOMPolicyParameters,
                    new MomPolicyVDSParameters(getVds(), getVdsGroup().isEnableBallooning(),
                            getVdsGroup().isEnableKsm(), getVdsGroup().isKsmMergeAcrossNumaNodes())
                                              ).getSucceeded();
        } catch (EngineException e) {
            log.error("Could not update MoM policy on host '{}': {}",
                    getVdsName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
        getReturnValue().setSucceeded(succeeded);
    }

    @Override
    protected boolean canDoAction() {
        HostValidator hostValidator = new HostValidator(getVds());

        return validate(hostValidator.hostExists())
                && validate(hostValidator.isUp())
                && validate(validateMinimumVersionSupport());
    }

    private ValidationResult validateMinimumVersionSupport() {
        return FeatureSupported.momPolicyOnHost(getVdsGroup().getCompatibilityVersion())
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MOM_UPDATE_VDS_VERSION);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getVdsId(),
                VdcObjectType.VDS, getActionType().getActionGroup()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_MOM_POLICIES
                : AuditLogType.USER_FAILED_TO_UPDATE_MOM_POLICIES;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__TYPE__HOST);
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
