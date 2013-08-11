package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.common.interfaces.SearchType;

public class SearchParameters extends VdcQueryParametersBase implements Serializable {
    private static final long serialVersionUID = 2275481072329075722L;

    private String _searchPattern;
    private SearchType _searchType;
    private int _maxCount;
    private long searchFrom;
    private boolean caseSensitive;

    public SearchParameters() {
        this (null, SearchType.VM, true);
    }

    public SearchParameters(String searchPattern, SearchType searchType) {
        this (searchPattern, searchType, true);
    }

    public SearchParameters(String searchPattern, SearchType searchType, boolean caseSensitive) {
        _searchType = searchType;
        _searchPattern = searchPattern;
        this.caseSensitive = caseSensitive;
        _maxCount = -1;
    }

    public String getSearchPattern() {
        return _searchPattern;
    }

    public SearchType getSearchTypeValue() {
        return _searchType;
    }

    public int getMaxCount() {
        return _maxCount;
    }

    public void setMaxCount(int value) {
        _maxCount = value;
    }

    public void setSearchFrom(long value) {
        searchFrom = value;
    }

    public long getSearchFrom() {
        return searchFrom;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(50);
        builder.append("search type: ");
        builder.append(getSearchTypeValue());
        builder.append(", search pattern: [");
        builder.append(getSearchPattern());
        builder.append("], case sensitive: ");
        builder.append(getCaseSensitive());
        builder.append(" [from: ");
        builder.append(getSearchFrom());
        builder.append(", max: ");
        builder.append(getMaxCount());
        builder.append("] ");
        builder.append(super.toString());
        return builder.toString();
    }
}
