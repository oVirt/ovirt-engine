package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.StringHelper;

//
// JTODO - this needs testing -- Livnat
//

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//  Unimplemented for now. We leave the code for future implementations
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
/**
 * Determines whether [is computer with the same name exists].
 *
 * @param userName
 *            Name of the user.
 * @param password
 *            The password.
 * @param domain
 *            The domain.
 * @param computerName
 *            Name of the computer.
 * @return <c>true</c> if [is computer with the same name exists]; otherwise,
 *         <c>false</c>.
 */
public class LdapIsComputerWithTheSameNameExistsCommand extends LdapBrokerCommandBase {
    private String getComputerName() {
        return ((LdapIsComputerWithSameNameExistsParameters) getParameters()).getComputerName();
    }

    public LdapIsComputerWithTheSameNameExistsCommand(LdapIsComputerWithSameNameExistsParameters parameters) {
        super(parameters);
        if (StringHelper.isNullOrEmpty(getPassword())) {
            // setLoginName(mDefaultUserName);
            // setPassword(mDefaultUserPassword);
            String domain = LdapBrokerUtils.getDomainsList().get(0);
            setDomain(domain);
        }
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        Unimplemented for now. We leave the code for future implementations
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        check that there is no computer with the same name.
//        directorySearcher.Filter = String.format(AdBrokerLDAPQueries.GET_COMPUTER_BY_NAME, getComputerName());
//        directorySearcher.searchScope = SearchControls.SUBTREE_SCOPE;
//        Object objResult = directorySearcher.FindOne(new EmptyAttributeMapper());

//        setReturnValue(objResult != null);
        setReturnValue(false);
    }
}
