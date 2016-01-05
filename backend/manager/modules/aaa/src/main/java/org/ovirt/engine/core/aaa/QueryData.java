package org.ovirt.engine.core.aaa;

import java.io.Serializable;

public class QueryData implements Serializable {
    private static final long serialVersionUID = 995908611144010190L;

    private String query;
    private long date;
    private String authz;
    private String namespace;

    public QueryData() {
        // needed for json serialization
    }

    public QueryData(String query, long date, String authz, String namespace) {
        this.query = query;
        this.date = date;
        this.authz = authz;
        this.namespace = namespace;
    }

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
