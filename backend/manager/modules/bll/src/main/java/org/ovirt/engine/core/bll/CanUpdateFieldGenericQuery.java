package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.CanUpdateFieldGenericParameters;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class CanUpdateFieldGenericQuery<P extends CanUpdateFieldGenericParameters> extends QueriesCommandBase<P> {
    public CanUpdateFieldGenericQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                ObjectIdentityChecker.CanUpdateField(getParameters().getFieldContainer(),
                                                     getParameters().getFieldName(), getParameters().getStatus()));
    }
}
