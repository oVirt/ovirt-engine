package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.AdGroupsSearchParameters;

/**
 * A query to perform searches for AdGroups.
 * This class does not add any functionality on top of the {@link SearchQuery},
 * but rather restricts it, so that it can safely be exposed to non-admin users.
 *
 * Note that this query contains no implementation - it simply restricts the
 * parameter types so only user searches are possible.
 *
 * Note: This is query specifically searches AdGroups, you should not prepend the
 * search string with "AdUsers: ".
 */
public class AdGroupsSearchQuery<P extends AdGroupsSearchParameters> extends SearchQuery<P> {

    public AdGroupsSearchQuery(P parameters) {
        super(parameters);
    }

}
