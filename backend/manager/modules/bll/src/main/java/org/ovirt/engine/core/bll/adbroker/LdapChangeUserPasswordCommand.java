package org.ovirt.engine.core.bll.adbroker;

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//Unimplemented for now. We leave the code for future implementations
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
public class LdapChangeUserPasswordCommand extends LdapBrokerCommandBase {
    private String getDestinationNewPassword() {
        return ((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserNewPassword();
    }

    private String getDestinationUserName() {
        return ((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserName();
    }

    private String getDestinationUserPassword() {
        return ((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserPassword();
    }

    public LdapChangeUserPasswordCommand(LdapChangeUserPasswordParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//  Unimplemented for now. We leave the code for future implementations
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        directorySearcher.Filter = String.format(AdBrokerLDAPQueries.GET_USER_BY_ANR, getDestinationUserName());
//        directorySearcher.searchScope = SearchControls.SUBTREE_SCOPE;
//        directorySearcher.setFilteredAttributes(UserAttributeMapper.USERS_ATTRIBUTE_FILTER);
        /*
         * JTODO: SearchResult objResult = directorySearcher.FindOne();
         * DirectoryEntry objLoginEntry = (objResult != null) ?
         * objResult.GetDirectoryEntry() : null;
         *
         * if (objLoginEntry != null) { Object obj =
         * objLoginEntry.invoke("ChangePassword", new Object[] {
         * getDestinationUserPassword(), getDestinationNewPassword() });
         *
         * objLoginEntry.CommitChanges(); obj = null; setSucceeded(true); }
         * JTODO END
         */
    }
}
