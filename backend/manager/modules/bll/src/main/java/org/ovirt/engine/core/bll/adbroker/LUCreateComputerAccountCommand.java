package org.ovirt.engine.core.bll.adbroker;

public class LUCreateComputerAccountCommand extends LUBrokerCommandBase {
    public LUCreateComputerAccountCommand(LdapCreateComputerAccountParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        setSucceeded(false);
    }
}
