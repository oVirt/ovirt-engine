package org.ovirt.engine.core.bll;

public class QueryData2 {
    private String query;
    private String type;
    private java.util.Date date = new java.util.Date(0);
    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String value) {
        query = value;
    }

    public String getQueryForAdBroker() {
        return query;
    }

    public void setQueryForAdBroker(String value) {
        query = value;
    }

    public String getQType() {
        return type;
    }

    public void setQType(String value) {
        type = value;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date value) {
        date = value;
    }
}
