package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class GetUserMessageQuery<P extends GetUserMessageParameters> extends QueriesCommandBase<P> {
    public GetUserMessageQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                UserMessageController.getInstance().GetUserMessage(getParameters().getId()));
    }
}
