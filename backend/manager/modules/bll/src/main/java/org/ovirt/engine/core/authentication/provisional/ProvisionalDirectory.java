package org.ovirt.engine.core.authentication.provisional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryGroup;
import org.ovirt.engine.core.authentication.DirectoryManager;
import org.ovirt.engine.core.authentication.DirectoryUser;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapQueryData;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserIdListParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserNameParameters;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This directory implementation is a bridge between the new directory interfaces and the existing LDAP infrastructure.
 * It will exist only while the engine is migrated to use the new directory interfaces, then it will be removed.
 */
public class ProvisionalDirectory implements Directory {
    private Log log = LogFactory.getLog(ProvisionalDirectory.class);

    /**
     * The name of the domain.
     */
    private String domain;

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;

    public ProvisionalDirectory(String domain, LdapBroker broker) {
        this.domain = domain;
        this.broker = broker;
    }

    @Override
    public String getName() {
        return domain;
    }

    @Override
    public DirectoryUser findUser(ExternalId id) {
        // Find the user with the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.GetAdUserByUserId,
            new LdapSearchByIdParameters(domain, id)
        );
        LdapUser ldapUser = (LdapUser) ldapResult.getReturnValue();

        // Map the user:
        return mapUser(ldapUser);
    }

    @Override
    public DirectoryUser findUser(String name) {
        // Find the user with the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.GetAdUserByUserName,
            new LdapSearchByUserNameParameters(null, domain, name)
        );
        LdapUser ldapUser = (LdapUser) ldapResult.getReturnValue();
        if (ldapUser == null) {
            return null;
        }

        // Map the user:
        return mapUser(ldapUser);
    }

    @Override
    public List<DirectoryUser> findUsers(List<ExternalId> ids) {
        // Find the users using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.GetAdUserByUserIdList,
            new LdapSearchByUserIdListParameters(domain, ids, true)
        );
        @SuppressWarnings("unchecked")
        List<LdapUser> ldapUsers = (List<LdapUser>) ldapResult.getReturnValue();

        // Map the users:
        return mapUsers(ldapUsers);
    }

    @Override
    public List<DirectoryUser> queryUsers(String query) {
        throw new UnsupportedOperationException();
    }

    public List<DirectoryUser> queryUsers(LdapQueryData data) {
        // Find the users using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.SearchUserByQuery,
            new LdapSearchByQueryParameters(null, domain, data)
        );
        List<LdapUser> ldapUsers = (List<LdapUser>) ldapResult.getReturnValue();

        // Map the users:
        return mapUsers(ldapUsers);
    }

    /**
     * Transforms a LDAP user into a directory user.
     */
    private DirectoryUser mapUser(LdapUser ldapUser) {
        // Make sure that the directory user doesn't contain the domain name:
        String name = ldapUser.getUserName();
        int index = name.indexOf('@');
        if (index >= 0) {
            name = name.substring(0, index);
        }

        // Create the directory user and populate the basic attributes:
        DirectoryUser directoryUser = new DirectoryUser(this, ldapUser.getUserId(), name);
        directoryUser.setFirstName(ldapUser.getName());
        directoryUser.setLastName(ldapUser.getSurName());
        directoryUser.setDepartment(ldapUser.getDepartment());
        directoryUser.setEmail(ldapUser.getEmail());
        directoryUser.setTitle(ldapUser.getTitle());

        // Populate the groups of the user (note that as we a calling a method of this directory to do so we should
        // first locate it using the manager, calling the method directory would bypass any decorator that may put on
        // top of the directory):
        Directory directory = DirectoryManager.getInstance().getDirectory(domain);
        if (directory == null) {
            log.warnFormat(
                "Can't find domain \"{0}\" to retrieve groups for user \"{1}\", the groups and related permissions " +
                "won't be available.",
                domain, ldapUser.getUserId()
            );
        }
        else {
            Collection<LdapGroup> ldapGroups = ldapUser.getGroups() != null ? ldapUser.getGroups().values() : null;
            if (ldapGroups != null) {
                List<DirectoryGroup> directoryGroups = new ArrayList<DirectoryGroup>(ldapGroups.size());
                for (LdapGroup ldapGroup : ldapGroups) {
                    DirectoryGroup directoryGroup = mapGroup(ldapGroup);
                    if (directoryGroup != null) {
                        directoryGroups.add(directoryGroup);
                    }
                }
                directoryUser.setGroups(directoryGroups);
            }
        }

        return directoryUser;
    }

    /**
     * Transforms a list of LDAP users into a list of directory users.
     */
    private List<DirectoryUser> mapUsers(List<LdapUser> ldapUsers) {
        List<DirectoryUser> directoryUsers = new ArrayList<DirectoryUser>(ldapUsers.size());
        for (LdapUser ldapUser : ldapUsers) {
            DirectoryUser directoryUser = mapUser(ldapUser);
            directoryUsers.add(directoryUser);
        }
        return directoryUsers;
    }

    @Override
    public DirectoryGroup findGroup(String name) {
        // Retrieving groups by name isn't currently supported or needed.
        return null;
    }

    @Override
    public DirectoryGroup findGroup(ExternalId id) {
        // Find the group using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.GetAdGroupByGroupId,
            new LdapSearchByIdParameters(domain, id)
        );
        LdapGroup ldapGroup = (LdapGroup) ldapResult.getReturnValue();

        // Map the group:
        return mapGroup(ldapGroup);
    }

    @Override
    public List<DirectoryGroup> queryGroups(String query) {
        throw new UnsupportedOperationException();
    }

    public List<DirectoryGroup> queryGroups(LdapQueryData data) {
        // Find the groups using the old mechanism:
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.SearchGroupsByQuery,
            new LdapSearchByQueryParameters(null, domain, data)
        );
        List<LdapGroup> ldapGroups = (List<LdapGroup>) ldapResult.getReturnValue();

        // Map the groups:
        return mapGroups(ldapGroups);
    }

    /**
     * Transforms a LDAP group into a directory group.
     */
    private DirectoryGroup mapGroup(LdapGroup ldapGroup) {
        return new DirectoryGroup(this, ldapGroup.getid(), ldapGroup.getname());
    }

    /**
     * Transforms a list of LDAP groups into a list of directory groups.
     */
    private List<DirectoryGroup> mapGroups(List<LdapGroup> ldapGroups) {
        List<DirectoryGroup> directoryGroups = new ArrayList<>(ldapGroups.size());
        for (LdapGroup ldapGroup : ldapGroups) {
            DirectoryGroup directoryGroup = mapGroup(ldapGroup);
            directoryGroups.add(directoryGroup);
        }
        return directoryGroups;
    }
}
