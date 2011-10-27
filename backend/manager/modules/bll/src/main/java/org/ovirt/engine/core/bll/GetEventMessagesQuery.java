package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetEventMessagesQuery<P extends GetEventMessagesParameters> extends QueriesCommandBase<P> {
    public GetEventMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue =
        // ResourceManager.Instance.GetEventMessages((Parameters as
        // GetEventMessagesParameters).Id);
    }
}
