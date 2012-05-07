package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.AdUsersSearchParameters;

/**
 * A query to perform searches for AdUsers.
 * This class does not add any functionality on top of the {@link SearchQuery},
 * but rather restricts it, so that it can safely be exposed to non-admin users.
 *
 * Note that this query contains no implementation - it simply restricts the
 * parameter types so only user searches are possible.
 *
 * Note: This is query specifically searches AdUsers, you should not prepend the
 * search string with "AdUsers: ".
 */
public class AdUsersSearchQuery<P extends AdUsersSearchParameters> extends SearchQuery<P> {

    public AdUsersSearchQuery(P parameters) {
        super(parameters);
    }

}
