package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class SearchParameters extends QueryParametersBase implements Serializable {
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
        _maxCount = Integer.MAX_VALUE;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("searchType", getSearchTypeValue())
                .append("searchPattern", getSearchPattern())
                .append("caseSensitive", getCaseSensitive())
                .append("from", getSearchFrom())
                .append("max", getMaxCount());
    }
}
