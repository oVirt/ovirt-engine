package org.ovirt.engine.core.dao.qos;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.compat.Guid;

public interface StorageQosDao extends QosDao<StorageQos> {

    /**
     * fetches QoS object attached to disk profile
     *
     * @param diskProfileId
     *            the disk profile id
     * @return qos
     */
    StorageQos getQosByDiskProfileId(Guid diskProfileId);
}
