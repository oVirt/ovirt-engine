package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsMessagesParameters extends GetMessagesByIdParametersBase {
    private static final long serialVersionUID = -4224784218887876903L;

    public GetVmsMessagesParameters(Guid vmId) {
        super(vmId);
    }

    public GetVmsMessagesParameters() {
    }
}
