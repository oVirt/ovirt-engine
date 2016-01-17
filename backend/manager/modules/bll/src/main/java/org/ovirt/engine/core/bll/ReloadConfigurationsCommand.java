package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;

@NonTransactiveCommandAttribute
public class ReloadConfigurationsCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    public ReloadConfigurationsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        DBConfigUtils.refreshReloadableConfigsInVdcOptionCache();
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
