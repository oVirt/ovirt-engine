package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.common.action.BondParametersBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("BondName") })
public abstract class VdsBondCommand<T extends BondParametersBase> extends VdsCommand<T> {
    public VdsBondCommand(T parameters) {
        super(parameters);
    }

    public String getBondName() {
        return getParameters().getBondName();
    }
}
