package org.ovirt.engine.core.bll;

import java.util.Date;

public class QueryData {
    public QueryData(String query, String type, Date date, String domain) {
        this.query = query;
        this.type = type;
        this.date = date;
        this.domain = domain;
    }

    private final String query;
    private final String type;
    private final Date date;
    private final String domain;

    public String getDomain() {
        return domain;
    }

    public String getQuery() {
        return query;
    }

    public String getQueryForAdBroker() {
        return query;
    }

    public String getQType() {
        return type;
    }

    public Date getDate() {
        return date;
    }
}
