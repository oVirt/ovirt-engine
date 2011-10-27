package org.ovirt.engine.core.bll.adbroker;


/**
 * Interface for managing a cache for data that relates to users, domains and
 * user data in context of domain
 *
 */
public interface UsersDomainsCacheManager {

    /**
     * Adds a domain to the cache manager
     *
     * @param domain
     */
    public void addDomain(Domain domain);

    /**
     * Associate a user with a domain
     *
     * @param user
     * @param domainName
     */
    public UserDomainInfo associateUserWithDomain(String userName, String domainName);

    /**
     * Gets a user domain info according to user name
     *
     * @param userName
     * @return
     */
    public UserDomainInfo getUserDomainInfo(String userName, String domainName);

    /**
     * Get domain according to its name
     *
     * @param domainName
     * @return
     */
    public Domain getDomain(String domainName);

    /**
     * Removes a user domain info based on ad user
     *
     * @param ddUser
     */
    public void removeUserDomainInfo(String userName, String domainName);

    /**
     * Remove domain info object based on domain name
     *
     * @param domainName
     */
    public void removeDomain(String domainName);

}
