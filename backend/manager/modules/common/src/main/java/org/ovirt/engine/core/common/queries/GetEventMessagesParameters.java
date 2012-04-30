package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetEventMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = 921754923722982628L;

    public GetEventMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetEventMessagesParameters() {
    }
}
