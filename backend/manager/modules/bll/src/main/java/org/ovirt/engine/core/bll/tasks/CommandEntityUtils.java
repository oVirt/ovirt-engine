package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;

public class CommandEntityUtils {

    private static final String PARAMETERS_KEY = "params";

    public static void setParameters(CommandEntity commandEntity, VdcActionParametersBase params) {
        commandEntity.getData().put(PARAMETERS_KEY, params);
    }

    public static VdcActionParametersBase getParameters(CommandEntity commandEntity) {
        return (VdcActionParametersBase) commandEntity.getData().get(PARAMETERS_KEY);
    }
}
