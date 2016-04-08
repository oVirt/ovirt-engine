package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Label;

public class LabelActionParameters extends LabelActionParametersBase {
    @NotNull
    protected Label label;

    private LabelActionParameters() {
    }

    public LabelActionParameters(@Valid @NotNull Label label) {
        super(label.getId());
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }
}
