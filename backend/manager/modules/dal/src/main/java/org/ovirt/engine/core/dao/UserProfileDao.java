package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserSshKey;
import org.ovirt.engine.core.compat.Guid;

public interface UserProfileDao extends Dao {

    UserProfileProperty get(Guid propertyId);

    Guid save(UserProfileProperty property);

    UserProfileProperty update(UserProfileProperty property);

    void remove(Guid keyId);

    List<UserProfileProperty> getAll(Guid userId);

    UserProfile getProfile(Guid userId);

    List<UserSshKey> getAllPublicSshKeys();

    UserProfileProperty getByName(String name, Guid userId);
}
