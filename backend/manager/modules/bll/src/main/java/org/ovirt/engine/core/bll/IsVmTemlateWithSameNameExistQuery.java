package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class IsVmTemlateWithSameNameExistQuery<P extends IsVmTemlateWithSameNameExistParameters>
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
