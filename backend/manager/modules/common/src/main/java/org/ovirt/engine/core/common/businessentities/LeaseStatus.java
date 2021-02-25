package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LeaseJobStatus;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class LeaseStatus implements Serializable {

    private static final long serialVersionUID = -6933791970230415564L;

    private List<Integer> owners;
    private int generation;
    private LeaseJobStatus jobStatus;

    public LeaseStatus() {
    }

    public LeaseStatus(List<Integer> owners) {
        this.owners = owners;
    }

    public boolean isFree() {
        return owners != null ? owners.isEmpty() : true;
    }

    public void setOwners(List<Integer> owners) {
        this.owners = owners;
    }

    public List<Integer> getOwners() {
        return owners;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public LeaseJobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(LeaseJobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("owners", owners)
                .append("generation", generation)
                .append("jobStatus", jobStatus.getValue())
                .build();
    }
}
