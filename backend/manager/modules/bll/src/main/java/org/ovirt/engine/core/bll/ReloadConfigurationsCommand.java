package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.config.Config;

@NonTransactiveCommandAttribute
public class ReloadConfigurationsCommand<T extends ActionParametersBase> extends CommandBase<T> {

    public ReloadConfigurationsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Config.refresh();
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
            return getSucceeded() ? AuditLogType.RELOAD_CONFIGURATIONS_SUCCESS : AuditLogType.RELOAD_CONFIGURATIONS_FAILURE;
    }
}
