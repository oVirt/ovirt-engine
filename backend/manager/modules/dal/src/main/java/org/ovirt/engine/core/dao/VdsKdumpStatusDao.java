package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsKdumpStatus;
import org.ovirt.engine.core.compat.Guid;

public interface VdsKdumpStatusDao extends Dao {
    /**
     * Updates kdump status record for specified VDS
     *
     * @param vdsKdumpStatus
     *            updated kdump status
     */
    public void update(VdsKdumpStatus vdsKdumpStatus);

    /**
     * Updates kdump status record for specified VDS
     *
     * @param ip
     *            IP address of host to update status for
     * @param vdsKdumpStatus
     *            updated kdump status
     */
    public void updateForIp(String ip, VdsKdumpStatus vdsKdumpStatus);

    /**
     * Removes kdump status for specified VDS
     *
     * @param vdsId
     *            UUID of host
     */
    public void remove(Guid vdsId);

    /**
     * Returns kdump status record for specified VDS
     *
     * @param vdsId
     *            UUID of host
     */
    public VdsKdumpStatus get(Guid vdsId);

    /**
     * Returns all unfinished kdump status records
     */
    public List<VdsKdumpStatus> getAllUnfinishedVdsKdumpStatus();
}
