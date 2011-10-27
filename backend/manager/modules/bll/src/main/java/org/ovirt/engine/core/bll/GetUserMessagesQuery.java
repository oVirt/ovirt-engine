package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetUserMessagesQuery<P extends GetUserMessagesParameters> extends QueriesCommandBase<P> {
    public GetUserMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue =
        // ResourceManager.Instance.GetUserMessages((Parameters as
        // GetUserMessagesParameters).Id);
    }
}
