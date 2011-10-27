package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetVmsMessagesQuery<P extends GetVmsMessagesParameters> extends QueriesCommandBase<P> {
    public GetVmsMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue =
        // ResourceManager.Instance.GetVmsMessages((Parameters as
        // GetVmsMessagesParameters).Id);
    }
}
