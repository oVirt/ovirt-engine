package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AddStepParameters;

/**
 * BLL command to create a Step for Jobs that are internal
 */
@InternalCommandAttribute
public class AddInternalStepCommand <T extends AddStepParameters> extends AddStepCommand<T>{

    public AddInternalStepCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }

}
