package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetVdsMessagesQuery<P extends GetVdsMessagesParameters> extends QueriesCommandBase<P> {
    public GetVdsMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue =
        // ResourceManager.Instance.GetVdsMessages((Parameters as
        // GetVdsMessagesParameters).getVdsId());
    }
}
