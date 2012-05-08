package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.interfaces.SearchType;

/**
 * Parameters for the AdGroupsSearchQuery.
 * Note that these are essentially search parameters, but the query string
 * does NOT need to be prepended with "AdGroups:"
 */
public class AdGroupsSearchParameters extends SearchParameters {
    private static final long serialVersionUID = -4695945267348177923L;

    public AdGroupsSearchParameters(String searchPattern) {
        super(searchPattern, SearchType.AdGroup);
    }

    public AdGroupsSearchParameters(String searchPattern, boolean caseSensitive) {
        super(searchPattern, SearchType.AdGroup, caseSensitive);
    }

    /** Prepends "AdGroups:" to the given search string */
    @Override
    public String getSearchPattern() {
        return getSearchTypeValue() + ": " + super.getSearchPattern();
    }

}
