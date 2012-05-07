package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.interfaces.SearchType;

/**
 * Parameters for the AdUsersSearchQuery.
 * Note that these are essentially search parameters, but the query string
 * does NOT need to be prepended with "AdUsers:"
 */
public class AdUsersSearchParameters extends SearchParameters {
    private static final long serialVersionUID = -2788510087018816667L;

    public AdUsersSearchParameters(String searchPattern) {
        super(searchPattern, SearchType.AdUser);
    }

    public AdUsersSearchParameters(String searchPattern, boolean caseSensitive) {
        super(searchPattern, SearchType.AdUser, caseSensitive);
    }

    /** Prepends "AdUsers:" to the given search string */
    @Override
    public String getSearchPattern() {
        return getSearchTypeValue() + ": " + super.getSearchPattern();
    }

}
