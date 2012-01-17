package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.interfaces.SearchType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SearchParameters", namespace = "http://service.engine.ovirt.org")
public class SearchParameters extends VdcQueryParametersBase implements Serializable {
    private static final long serialVersionUID = 2275481072329075722L;

    @XmlElement(name = "SearchPattern", required = true)
    private String _searchPattern;
    @XmlElement(name = "SearchTypeValue", required = true)
    private SearchType _searchType = SearchType.forValue(0);
    @XmlElement(name = "MaxCount", defaultValue = "-1", required = true)
    private int _maxCount = -1;
    @XmlElement(name = "SearchFrom", defaultValue = "0", required = true)
    private long searchFrom = 0;
    @XmlElement(name = "CaseSensitive", defaultValue = "true", required = true)
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

}
