package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetTemplateMessagesQuery<P extends GetTemplateMessagesParameters> extends QueriesCommandBase<P> {
    public GetTemplateMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue =
        // ResourceManager.Instance.GetTemplateMessages((Parameters as
        // GetTemplateMessagesParameters).Id);
    }
}
