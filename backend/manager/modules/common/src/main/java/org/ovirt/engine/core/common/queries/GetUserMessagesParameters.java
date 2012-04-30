package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetUserMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = 3931907771424026679L;

    public GetUserMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetUserMessagesParameters() {
    }
}
