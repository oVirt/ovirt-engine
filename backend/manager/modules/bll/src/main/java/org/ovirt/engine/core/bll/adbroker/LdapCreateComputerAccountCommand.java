package org.ovirt.engine.core.bll.adbroker;

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//Unimplemented for now. We leave the code for future implementations
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
public class LdapCreateComputerAccountCommand extends LdapBrokerCommandBase {

    public LdapCreateComputerAccountCommand(LdapCreateComputerAccountParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        /*
         * JTODO: // add the new computer. DirectoryEntry computers = new
         * DirectoryEntry(getPath(), getLoginName(), getPassword());
         * DirectoryEntry newComputer = computers.Children.add("CN=" +
         * getComputerName(), "computer"); newComputer.CommitChanges();
         * computers.close(); setReturnValue(newComputer); setSucceeded(true);
         * JTODO END
         */
    }
}
