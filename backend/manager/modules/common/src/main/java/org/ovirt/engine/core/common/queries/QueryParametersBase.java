package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.HasCorrelationId;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.PreRun;


public class QueryParametersBase implements Serializable, HasCorrelationId {
    private static final long serialVersionUID = -6766170283465888549L;

    /**
     * The identifier of session which should be set by sender via Rest Api or by front end
     */
    private String sessionId;

    /**
     * The boolean flag which provides if the session should be refreshed
     */
    private boolean refresh;

    /**
     * The boolean flag which specifies if the query should be filtered
     * (e.g., according to user permissions as opposed to the default, which is running as admin)
     */
    private boolean isFiltered;

    /**
     * A cross system identifier of the executed action
     */
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS, message = "VALIDATION_INVALID_CORRELATION_ID",
            groups = PreRun.class)
    @Size(min = 1, max = BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, groups = PreRun.class)
    private String correlationId;

    public QueryParametersBase() {
        refresh = false;
    }

    public QueryParametersBase(String sessionId) {
        this();
        this.sessionId = sessionId;

    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public QueryParametersBase withoutRefresh() {
        setRefresh(false);
        return this;
    }

    public QueryParametersBase withRefresh() {
        setRefresh(true);
        return this;
    }

    @Override
    public void setCorrelationId(String value) {
        correlationId = value;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isFiltered() {
        return isFiltered;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("refresh", refresh)
                .append("filtered", isFiltered);
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
