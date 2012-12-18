package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.LdapUser;

//
// JTODO - this needs testing -- Livnat
//

public class LdapGetAdUserByUserNameCommand extends LdapBrokerCommandBase {
    private String getUserName() {
        return ((LdapSearchByUserNameParameters) getParameters()).getUserName();
    }

    public LdapGetAdUserByUserNameCommand(LdapSearchByUserNameParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setDomain(getDomain());
        queryData.setFilterParameters(new Object[] { getUserName().split("[@]", -1)[0] });
        queryData.setLdapQueryType(LdapQueryType.getUserByName);
        Object searchResult = directorySearcher.FindOne(queryData);

        setReturnValue(populateUserData((LdapUser) searchResult, getDomain()));
        // if user is not null then action succeeded
        setSucceeded((getReturnValue() != null));
    }
}
