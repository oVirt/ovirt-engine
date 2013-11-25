package org.ovirt.engine.core.bll;

import java.util.Date;

public class QueryData {
    public QueryData(String query, Date date, String domain) {
        this.query = query;
        this.date = date;
        this.domain = domain;
    }

    private final String query;
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

    public Date getDate() {
        return date;
    }
}
