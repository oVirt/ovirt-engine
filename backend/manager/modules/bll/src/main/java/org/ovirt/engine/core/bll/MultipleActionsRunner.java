package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.slf4j.Logger;

public interface MultipleActionsRunner {
    ArrayList<VdcReturnValueBase> execute();

    void setIsRunOnlyIfAllValidatePass(boolean isRunOnlyIfAllValidationPass);

    void setIsWaitForResult(boolean waitForResult);

    default void logExecution(Logger log, SessionDataContainer sessionDataContainer, String sessionId, String details) {
        DbUser user = sessionDataContainer.getUser(sessionId, false);
        log.debug("Executing {}{}",
                details,
                user == null ? "." : String.format(" for user %s@%s.", user.getLoginName(), user.getDomain()));
    }
}
