package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.interfaces.SearchType;

public class DirectorySearchParameters extends SearchParameters {

    /**
     *
     */
    private static final long serialVersionUID = 1831775270159639568L;

    private String namespace;

    public DirectorySearchParameters() {
        super();
    }

    public DirectorySearchParameters(String searchPattern, SearchType searchType, String namespace) {
        super(searchPattern, searchType);
        this.namespace = namespace;
    }

    public DirectorySearchParameters(String searchPattern, SearchType searchType) {
        this(searchPattern, searchType, null);
    }

    public DirectorySearchParameters(String searchPattern,
            SearchType searchType,
            boolean caseSensitive,
            String namespace) {
        super(searchPattern, searchType, caseSensitive);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }


}
