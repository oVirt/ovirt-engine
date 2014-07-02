package org.ovirt.engine.core.dao.profiles;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface ProfilesDao<T extends ProfileBase> extends GenericDao<T, Guid> {

    /**
     * Retrieves all profiles associated with the given QoS id.
     *
     * @param qosId
     *            the QoS's ID
     * @return the list of profiles
     */
    List<T> getAllForQos(Guid qosId);
}

