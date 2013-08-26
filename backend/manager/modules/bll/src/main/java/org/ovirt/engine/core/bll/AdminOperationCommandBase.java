package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class implements IsUserAuthorizedToRunAction() so only admin users can
 * execute it without explicit permissions given for users, Any command that can
 * be executed by administrators and there are no permission for should extend
 * this class for example - no permissions can be given on tags and only admin
 * users can manipulate tags
 *
 * 'admin user' logic is in MultiLevelAdministrationHandler.isAdminUser method
 *
 */
public abstract class AdminOperationCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {

    private static Log log = LogFactory.getLog(AdminOperationCommandBase.class);

    protected AdminOperationCommandBase(T parameters) {
        super(parameters);
    }

    protected AdminOperationCommandBase() {
    }

    /**
     * Check if current user is admin according to
     * MultiLevelAdministrationHandler.isAdminUser
     *
     */
    @Override
    protected boolean isUserAuthorizedToRunAction() {
        if (isInternalExecution() || !Config.<Boolean> GetValue(ConfigValues.IsMultilevelAdministrationOn)) {
            if (log.isDebugEnabled()) {
                log.debugFormat(
                        "IsUserAuthorizedToRunAction: Internal action or MLA is off - permission check skipped for action {0}",
                        getActionType());
            }
            return true;
        }

        if (getCurrentUser() != null) {
            if (MultiLevelAdministrationHandler.isAdminUser(getCurrentUser())) {
                return true;
            }
            addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
            return false;
        } // user not logged in
        else {
            addCanDoActionMessage(VdcBllMessages.USER_IS_NOT_LOGGED_IN);
            return false;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
