package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.compat.Guid;

public interface UserProfileDao extends Dao {
    /**
     * Retrieves the user profile with the specified id.
     *
     * @param id
     *            the id
     * @return the user profile, or <code>null</code> if the id was invalid
     */
    UserProfile get(Guid id);

    /**
     * Retrieves the user profile associated with the specified user id.
     *
     * @param userId
     *            the user id
     * @return the user profile, or <code>null</code> if the id was invalid
     */
    UserProfile getByUserId(Guid userId);

    /**
     * Retrieves all user profiles.
     *
     * @return the collection of all user profiles
     */
    List<UserProfile> getAll();

    /**
     * Saves the user profile.
     *
     * @param profile
     *            the user profile
     */
    void save(UserProfile profile);

    /**
     * Updates the specified user profile in the database.
     *
     * @param profile
     *            the user profile
     */
    void update(UserProfile profile);

    /**
     * Removes the user profile with the specified id.
     *
     * @param id
     *            the user profile id
     */
    void remove(Guid id);
}
