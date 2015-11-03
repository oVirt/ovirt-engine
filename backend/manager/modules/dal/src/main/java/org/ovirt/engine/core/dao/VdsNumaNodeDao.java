package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.compat.Guid;

public interface VdsNumaNodeDao extends NumaNodeDao<VdsNumaNode> {

    /**
     * Get all numa nodes of a vds by vds id
     * @param vdsId
     *            the id of vds
     * @return the list of numa nodes
     */
    List<VdsNumaNode> getAllVdsNumaNodeByVdsId(Guid vdsId);
}
