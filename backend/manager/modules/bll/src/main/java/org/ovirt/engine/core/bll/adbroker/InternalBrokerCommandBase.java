package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public abstract class InternalBrokerCommandBase extends BrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(InternalBrokerCommandBase.class);

    public InternalBrokerCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
    }
    @Override
    protected String getPROTOCOL() {
        return "Internal";
    }

    @Override
    public LdapReturnValueBase Execute() {
        try {
            ExecuteQuery();
        } catch (RuntimeException e) {
            log.errorFormat("Error in executing Internal broker command. Exception is {0} ", e.getMessage());
            _ldapReturnValue.setSucceeded(false);
            _ldapReturnValue.setReturnValue(null);
        }
        return _ldapReturnValue;
    }

    protected abstract void ExecuteQuery();

}

