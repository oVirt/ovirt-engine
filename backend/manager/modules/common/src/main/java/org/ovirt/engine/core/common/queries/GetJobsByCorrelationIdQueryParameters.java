package org.ovirt.engine.core.common.queries;


public class GetJobsByCorrelationIdQueryParameters  extends QueryParametersBase {

    private static final long serialVersionUID = 758029173419093849L;

    private String correlationId;

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

}
