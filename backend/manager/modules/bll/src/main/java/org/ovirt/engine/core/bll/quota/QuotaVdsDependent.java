package org.ovirt.engine.core.bll.quota;


import java.util.List;

/**
 * Implement the QuotaVdsDependent interface to identify your command as one that dependent on
 * Vds (vcpu and/or memory) Quota calculation in order to run. If a Command handles vcpus and memory -
 * it should be QuotaVdsDependent.
 */
public interface QuotaVdsDependent {

    /**
     * Get a list of the vds consumption parameters.
     * Override this method in order to set the vds consumption parameters for the quota check.
     * This method is called by CommandBase during the validate check in order to make sure the
     * command has sufficient quota resources in order to run.
     *
     * return null if the command does not consume any vds resources.
     *
     * @return - list of vds consumption parameters. null if no consumption.
     */
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters();
}
