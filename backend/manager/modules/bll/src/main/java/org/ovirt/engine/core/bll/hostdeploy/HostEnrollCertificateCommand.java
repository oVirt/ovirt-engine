package org.ovirt.engine.core.bll.hostdeploy;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

@NonTransactiveCommandAttribute
public class HostEnrollCertificateCommand extends VdsCommand<VdsActionParameters> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public HostEnrollCertificateCommand(VdsActionParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public boolean validate() {
        HostValidator hostValidator = HostValidator.createInstance(getVds());
        return validate(hostValidator.hostExists())
                && validate(hostValidator.validateStatusForEnrollCertificate());
    }

    @Override
    protected void executeCommand() {
        commandCoordinatorUtil.executeAsyncCommand(ActionType.HostEnrollCertificateInternal,
                getParameters(),
                cloneContextAndDetachFromParent());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ENROLL_CERTIFICATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_CERTIFICATION_ENROLLMENT_STARTED
                : AuditLogType.HOST_CERTIFICATION_ENROLLMENT_FAILED;
    }
}
