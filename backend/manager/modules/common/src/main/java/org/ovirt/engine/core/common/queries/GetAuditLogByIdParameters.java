package org.ovirt.engine.core.common.queries;

public class GetAuditLogByIdParameters extends QueryParametersBase {

    public GetAuditLogByIdParameters() {
    }

    public GetAuditLogByIdParameters(Long id) {
        super();
        this.id = id;
    }

    private static final long serialVersionUID = 1L;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
