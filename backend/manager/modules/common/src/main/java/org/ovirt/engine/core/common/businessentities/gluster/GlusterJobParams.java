package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

public class GlusterJobParams implements Queryable, BusinessEntity<Guid>, Comparable<GlusterJobParams> {

    private Guid id;

    private Guid jobId;

    private String paramsClassName;

    private String paramsClassValue;

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public String getParamsClassName() {
        return paramsClassName;
    }

    public String getParamsClassValue() {
        return paramsClassValue;
    }

    public void setParamsClassName(String paramsClassName) {
        this.paramsClassName = paramsClassName;
    }

    public void setParamsClassValue(String paramsClassValue) {
        this.paramsClassValue = paramsClassValue;
    }

    @Override
    public int compareTo(GlusterJobParams obj) {
        return BusinessEntityComparator.<GlusterJobParams, Guid> newInstance().compare(this, obj);
    }

    @Override
    public Guid getId() {
        if (id == null) {
            id = Guid.newGuid();
        }
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                jobId,
                paramsClassName,
                paramsClassValue,
                jobId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterJobParams)) {
            return false;
        }

        GlusterJobParams other = (GlusterJobParams) obj;
        return Objects.equals(jobId, other.jobId)
                && Objects.equals(paramsClassName, other.paramsClassName)
                && Objects.equals(paramsClassValue, other.paramsClassValue)
                && Objects.equals(jobId, other.jobId);
    }

}
