package org.ovirt.engine.core.bll.provider.storage;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveLibvirtSecretCommand extends LibvirtSecretCommandBase {

    public RemoveLibvirtSecretCommand(LibvirtSecretParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        LibvirtSecretValidator libvirtSecretValidator =
                new LibvirtSecretValidator(getParameters().getLibvirtSecret());
        return validate(libvirtSecretValidator.uuidExist());
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        libvirtSecretDao.remove(getParameters().getLibvirtSecret().getId());
        unregisterLibvirtSecret();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_LIBVIRT_SECRET : AuditLogType.USER_FAILED_TO_REMOVE_LIBVIRT_SECRET;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }
}
