package org.ovirt.engine.core.bll.quota;


import java.util.List;

/**
 * Implement the QuotaStorageDependent interface to identify your command as one that dependent on
 * Storage Quota calculation in order to run. If the command handles disks, images, snapshots and so on -
 * it should be QuotaStorageDependent.
 */
public interface QuotaStorageDependent {

    /**
     * Get a list of the storage consumption parameters.
     * Override this method in order to set the storage consumption parameters for the quota check.
     * This method is called by CommandBase during the validate check in order to make sure the
     * command has sufficient quota resources in order to run.
     *
     * return null if the command does not consume any storage resources.
     *
     * @return - list of storage consumption parameters. null if no consumption.
     */
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters();
}
