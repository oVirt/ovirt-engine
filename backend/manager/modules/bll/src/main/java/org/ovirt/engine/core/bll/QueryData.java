package org.ovirt.engine.core.bll;

import java.util.Date;

public class QueryData {
    private String query;
    private String type;
    private Date date = new Date(0);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date value) {
        date = value;
    }
}
