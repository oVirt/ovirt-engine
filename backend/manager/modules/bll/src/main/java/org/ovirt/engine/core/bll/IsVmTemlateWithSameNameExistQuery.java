package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class IsVmTemlateWithSameNameExistQuery<P extends NameQueryParameters>
        extends QueriesCommandBase<P> {
    public IsVmTemlateWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                VmTemplateCommand.isVmTemlateWithSameNameExist(getParameters().getName()));
    }
}
