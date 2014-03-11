package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public enum LdapQueryType {
    rootDSE,
    getUserByGuid,
    getGroupByGuid,
    getGroupByDN,
    getGroupByName,
    getUserByPrincipalName,
    getUserByName,
    getGroupsByGroupNames,
    getUsersByUserGuids,
    searchUsers,
    searchGroups
}
