package org.ovirt.engine.core.bll.adbroker;

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
