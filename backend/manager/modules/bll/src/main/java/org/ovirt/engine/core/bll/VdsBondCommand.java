package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("BondName") })
public abstract class VdsBondCommand<T extends RemoveBondParameters> extends VdsCommand<T> {
    public VdsBondCommand(T parameters) {
        super(parameters);
    }

    public String getBondName() {
        return getParameters().getBondName();
    }
}
