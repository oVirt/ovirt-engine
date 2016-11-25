package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
public class UpdateMomPolicyCommand extends VdsCommand<VdsActionParameters> {

    public UpdateMomPolicyCommand(VdsActionParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        try {
            succeeded = runVdsCommand(VDSCommandType.SetMOMPolicyParameters,
                    new MomPolicyVDSParameters(getVds(), getCluster().isEnableBallooning(),
                            getCluster().isEnableKsm(), getCluster().isKsmMergeAcrossNumaNodes())
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
    protected boolean validate() {
        HostValidator hostValidator = HostValidator.createInstance(getVds());

        return validate(hostValidator.hostExists()) && validate(hostValidator.isUp());
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
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
