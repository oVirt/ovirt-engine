package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.common.interfaces.SearchType;

public class SearchParameters extends VdcQueryParametersBase implements Serializable {
    private static final long serialVersionUID = 2275481072329075722L;

    private String _searchPattern;
    private SearchType _searchType = SearchType.forValue(0);
    private int _maxCount = -1;
    private long searchFrom = 0;
    private boolean caseSensitive = true;

    public SearchParameters() {
    }

    public SearchParameters(String searchPattern, SearchType searchType) {
        _searchType = searchType;
        _searchPattern = searchPattern;
    }

    public SearchParameters(String searchPattern, SearchType searchType, boolean caseSensitive) {
        this(searchPattern, searchType);
        this.caseSensitive = caseSensitive;
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
