package org.ovirt.engine.core.bll;

public class QueryData {
    public QueryData(String query, long date, String domain) {
        this.query = query;
        this.date = date;
        this.domain = domain;
    }

    private final String query;
    private final long date;
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

    public long getDate() {
        return date;
    }
}
