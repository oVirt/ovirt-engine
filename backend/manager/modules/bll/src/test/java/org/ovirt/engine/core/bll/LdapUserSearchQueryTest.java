package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.AdUsersSearchParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Parameterized.class)
public class LdapUserSearchQueryTest extends LdapSearchQueryTestBase {

    public LdapUserSearchQueryTest(Class<? extends SearchQuery<? extends SearchParameters>> queryType,
            SearchParameters queryParamters) {
        super(queryType, queryParamters);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]
        { { SearchQuery.class, new SearchParameters("AdUser: allnames=" + NAME_TO_SEARCH, SearchType.AdUser) },
                { AdUsersSearchQuery.class, new AdUsersSearchParameters("allnames=" + NAME_TO_SEARCH) } });
    }

    @Override
    protected LdapUser getExpectedResult() {
        LdapUser user = new LdapUser();
        user.setUserName(NAME_TO_SEARCH);
        user.setPassword("melon!");
        user.setUserId(Guid.newGuid());
        user.setDomainControler(DOMAIN);
        return user;
    }

    @Override
    protected AdActionType getAdActionType() {
        return AdActionType.SearchUserByQuery;
    }

    @Override
    protected LdapQueryType getLdapActionType() {
        return LdapQueryType.searchUsers;
    }
}
