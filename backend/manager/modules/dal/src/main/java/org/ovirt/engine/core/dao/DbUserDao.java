package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code DbUserDao} defines a type for performing CRUD operations on instances of {@link DbUser}.
 */
public interface DbUserDao extends Dao, SearchDao<DbUser> {

    /**
     * Retrieves the suser with the specified id.
     *
     * @param id
     *            the id
     * @return the user, or {@code null} if the id was invalid
     */
    DbUser get(Guid id);

    /**
     * Retrieves the user with the specified id.
     *
     * @param id the id of user
     * @param isFiltered user level / admin level
     * @return the user, or {@code null} if the id was invalid
     */
    DbUser get(Guid id, boolean isFiltered);

    /**
     * Retrieves a user by username.
     *
     * @param username
     *            the username
     * @return the user
     */
    DbUser getByUsernameAndDomain(String username, String domainName);

    /**
     * Retrieves a user by domain name and external identifier.
     *
     * @param domain the name of the domain
     * @param externalId the external identifier
     * @return a reference to the user or {@code null} if no such user
     *   can be found in the database
     */
    DbUser getByExternalId(String domain, String externalId);

    /**
     * Retrieves a user that matches either the internal or external identifiers given.
     *
     * @param id the internal identifier
     * @param domain the name of the domain
     * @param externalId the external identifier
     * @return a reference to the user or {@code null} if no such user
     *   can be found in the database
     */
    DbUser getByIdOrExternalId(Guid id, String domain, String externalId);

    /**
     * Retrieves all users associated with the specified virtual machine.
     *
     * @param id
     *            the VM id
     * @return the list of users
     */
    List<DbUser> getAllForVm(Guid id);

    /**
     * Retrieves all users associated with the specified template.
     *
     * @param id
     *            the template id
     * @return the list of users
     */
    List<DbUser> getAllForTemplate(Guid id);

    /**
     * Retrieves all defined used.
     *
     * @return the collection of all users
     */
    List<DbUser> getAll();

    /**
     * Retrieves all users
     *
     * @return the list of entries
     */
    List<DbUser> getAll(Guid userID, boolean isFiltered);

    /**
     * Saves the user.
     *
     * @param user
     *            the user
     */
    void save(DbUser user);

    /**
     * Updates the specified user in the database.
     *
     * @param user
     *            the user
     */
    void update(DbUser user);

    /**
     * Removes the user with the specified id.
     *
     * @param user
     *            the user id
     */
    void remove(Guid user);

    /**
     * Saves or updates the user.
     *
     * @param user
     *            the user
     */
    void saveOrUpdate(DbUser user);

    /**
     * User presentation in GUI have a distinction between ADMIN/USER user. The distinction is determined by their
     * permissions or their group's permissions. when Permission with the role type Admin is found, set the DbUser
     * isAdmin flag to ADMIN Type or to USER otherwise. Make the change only if the value is different to what it is
     * saved to db
     */
    void updateLastAdminCheckStatus(Guid... userIds);
}
