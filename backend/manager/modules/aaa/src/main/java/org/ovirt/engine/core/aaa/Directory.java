package org.ovirt.engine.core.aaa;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.api.extensions.Extension;

/**
 * A directory is an object that manages a collection of users and groups, usually stored in an external system like an
 * LDAP database.
 */
public abstract class Directory implements Extension {
    /**
     *
     */
    private static final long serialVersionUID = -8724317446083142917L;

    protected Map<Extension.ExtensionProperties, Object> context;

    public String getName() {
        return (String) context.get(ExtensionProperties.NAME);
    }

    public String getProfileName() {
        return ((Properties) context.get(ExtensionProperties.CONFIGURATION)).getProperty("ovirt.engine.aaa.authz.profile.name");
    }

    /**
     * Retrieves a user from the directory given its name. The name is expected to be unique.
     *
     * @param name the name of the user
     * @return the user corresponding to the given name or {@code null} if no such user can be found
     */
    public abstract DirectoryUser findUser(String name);

    /**
     * Retrieves a user from the directory given its identifier.
     *
     * @param id the identifier of the user
     * @return the user corresponding to the given identifier or {@code null} if no such user can be found
     */
    public abstract DirectoryUser findUser(ExternalId id);

    /**
     * Retrieves a list of users from the directory given their identifiers.
     *
     * @param ids the list of identifiers
     * @return a list containing at most on user for each identifier in the given set with no particular order, note
     *     that the returned list may contain less elements than the given list of identifiers
     */
    public abstract List<DirectoryUser> findUsers(List<ExternalId> ids);

    /**
     * Retrieves a group from the directory given its name.
     *
     * @param name the name of the group
     * @return the group corresponding to the given name or {@code null} if no such group can be found
     */
    public abstract DirectoryGroup findGroup(String name);

    /**
     * Retrieves a group from the directory given its identifier.
     *
     * @param id the identifier of the group
     * @return the group corresponding to the given identifier or {@code null} if no such group can be found
     */
    public abstract DirectoryGroup findGroup(ExternalId id);

    /**
     * Search the directory looking for users that match the given search query. Note that the query uses the LDAP query
     * format, regardless of the type of the directory, so the implementation is responsible for translating this LDAP
     * specific query into whatever is required by the underlying directory.
     *
     * @param query the LDAP query
     * @return a list containing the users that match the given query
     */
    public abstract List<DirectoryUser> queryUsers(String query);

    /**
     * Search the directory looking for groups that match the given search query. Note that the query uses the LDAP
     * query format, regardless of the type of the directory, so the implementation is responsible for translating this
     * LDAP specific query into whatever is required by the underlying directory.
     *
     * @param query the LDAP query
     * @return a list containing the groups that match the given query
     */
    public abstract List<DirectoryGroup> queryGroups(String query);

    @Override
    public void setContext(Map<ExtensionProperties, Object> context) {
        this.context = context;
    }

    @Override
    public Map<ExtensionProperties, Object> getContext() {
        return context;
    }

}
