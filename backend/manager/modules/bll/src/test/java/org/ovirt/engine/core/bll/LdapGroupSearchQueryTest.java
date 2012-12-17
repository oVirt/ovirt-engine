package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.AdGroupsSearchParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Parameterized.class)
public class LdapGroupSearchQueryTest extends LdapSearchQueryTestBase {

    public LdapGroupSearchQueryTest(Class<? extends SearchQuery<? extends SearchParameters>> queryType,
            SearchParameters queryParamters) {
        super(queryType, queryParamters);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]
        { { SearchQuery.class, new SearchParameters("AdGroup: name=" + NAME_TO_SEARCH, SearchType.AdGroup) },
                { AdGroupsSearchQuery.class, new AdGroupsSearchParameters("name=" + NAME_TO_SEARCH) } });
    }

    @Override
    protected LdapGroup getExpectedResult() {
        return new LdapGroup(Guid.NewGuid(), NAME_TO_SEARCH, DOMAIN);
    }

    @Override
    protected AdActionType getAdActionType() {
        return AdActionType.SearchGroupsByQuery;
    }

    @Override
    protected LdapQueryType getLdapActionType() {
        return LdapQueryType.searchGroups;
    }
}
