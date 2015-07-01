package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsCpuStatisticsDao</code> defines a type for performing CRUD operations on instances of {@link CpuStatistics}.
 *
 *
 */
public interface VdsCpuStatisticsDao extends Dao {

    /**
     * Get all cpu statistics of a vds by vds id
     * @param vdsId
     *            the id of vds
     * @return the list of cpu statistics
     */
    List<CpuStatistics> getAllCpuStatisticsByVdsId(Guid vdsId);

    /**
     * Save the given list of vds cpu statistics using a more efficient method to save all of them at
     * once, rather than each at a time.
     *
     * @param vdsCpuStatistics
     *            the vds cpu statistics to be saved
     * @param vdsId
     *            the vds id that the cpus belong to
     */
    void massSaveCpuStatistics(List<CpuStatistics> vdsCpuStatistics, Guid vdsId);

    /**
     * Update the cpu statistics data using a more efficient method
     * to update all of them at once, rather than each at a time.
     *
     * @param vdsCpuStatistics
     *            the vds cpu statistics to be updated
     * @param vdsId
     *            the vds id that the cpus belong to
     */
    void massUpdateCpuStatistics(List<CpuStatistics> vdsCpuStatistics, Guid vdsId);

    /**
     * Remove all the cpu statistics of a given vds
     *
     * @param vdsId
     *            the vds id
     */
    void removeAllCpuStatisticsByVdsId(Guid vdsId);

}
