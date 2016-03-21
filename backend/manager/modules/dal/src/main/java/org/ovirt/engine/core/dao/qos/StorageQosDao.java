package org.ovirt.engine.core.dao.qos;

import java.util.Collection;
import java.util.Map;

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

    /**
     * fetches QoS objects attached to disk profiles
     *
     * @param diskProfileIds
     *            List of disk profile ids
     * @return Map profileId to Qos
     */
    Map<Guid, StorageQos> getQosByDiskProfileIds(Collection<Guid> diskProfileIds);
}
