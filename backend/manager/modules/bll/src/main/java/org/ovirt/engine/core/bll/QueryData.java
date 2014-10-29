package org.ovirt.engine.core.bll;

public class QueryData {
    public QueryData(String query, long date, String authz, String namespace) {
        this.query = query;
        this.date = date;
        this.authz = authz;
        this.namespace = namespace;
    }

    private final String query;
    private final long date;
    private final String authz;
    private final String namespace;

    public String getAuthz() {
        return authz;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getQuery() {
        return query;
    }

    public long getDate() {
        return date;
    }
}
