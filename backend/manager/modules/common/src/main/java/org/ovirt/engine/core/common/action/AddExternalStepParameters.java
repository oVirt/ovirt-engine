package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;

public class AddExternalStepParameters extends AddStepParameters {

    private static final long serialVersionUID = 1L;

    public AddExternalStepParameters() {
    }

    public AddExternalStepParameters(Guid id, String description, StepEnum stepType) {
        super(id, description, stepType);
    }
}
