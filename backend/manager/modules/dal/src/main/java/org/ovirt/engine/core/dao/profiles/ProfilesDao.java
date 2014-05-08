package org.ovirt.engine.core.dao.profiles;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface ProfilesDao<T extends ProfileBase> extends GenericDao<T, Guid> {

}

