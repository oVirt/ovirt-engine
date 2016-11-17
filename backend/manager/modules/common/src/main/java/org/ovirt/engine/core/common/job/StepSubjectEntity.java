package org.ovirt.engine.core.common.job;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StepSubjectEntity extends SubjectEntity {
    private Integer stepEntityWeight;
    private Guid stepId;

    public StepSubjectEntity(VdcObjectType vdcObjectType, Guid entityId, Integer stepEntityWeight) {
        this(null, vdcObjectType, entityId, stepEntityWeight);
    }

    public StepSubjectEntity(Guid stepId, VdcObjectType vdcObjectType, Guid entityId, Integer stepEntityWeight) {
        super(vdcObjectType, entityId);
        this.stepEntityWeight = stepEntityWeight;
        this.stepId = stepId;
    }

    public StepSubjectEntity(Guid stepId, VdcObjectType vdcObjectType, Guid entityId) {
        super(vdcObjectType, entityId);
        this.stepId = stepId;
    }

    public StepSubjectEntity() {
    }

    public Integer getStepEntityWeight() {
        return stepEntityWeight;
    }

    public void setStepEntityWeight(Integer stepEntityWeight) {
        this.stepEntityWeight = stepEntityWeight;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        StepSubjectEntity that = (StepSubjectEntity) o;
        return Objects.equals(stepId, that.stepId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stepId);
    }

    @Override
    protected ToStringBuilder appendProperties(ToStringBuilder tsb) {
        return super.appendProperties(tsb)
                .append("stepEntityWeight", stepEntityWeight)
                .append("stepId", stepId);
    }
}
