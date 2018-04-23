package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public abstract class FenceAgentCommandBase extends CommandBase<FenceAgentCommandParameterBase> {

    private static final String PORT = "port";

    public FenceAgentCommandBase(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public FenceAgentCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR_TYPE_FENCE_AGENT);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }

    protected boolean validatePMAgentPort() {

        Integer port = getParameters().getAgent().getPort();
        // check if port was set directly
        if (port == null) {
            // check if port was set using the fence agent options
            Map<String, String> optionsMap = getParameters().getAgent().getOptionsMap();
            if (optionsMap != null && optionsMap.containsKey(PORT)) {
                String portStr = optionsMap.get(PORT);
                if (StringUtils.isNumeric(portStr)) {
                    port = Integer.valueOf(portStr);
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        return port == null || ValidationUtils.validatePort(port);
    }
}
