package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class Domain {

    private String name; // domain name
    private RootDSE rootDSE; // rootDSE for domain
    private List<URI> ldapServers = new LinkedList<URI>(); // LDAP servers that match
    private LdapProviderType ldapProviderType;
    private LDAPSecurityAuthentication ldapSecurityAuthentication;
    private String userName;
    private String password;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Domain(String domainName) {
        name = domainName;
    }

    public LdapProviderType getLdapProviderType() {
        return ldapProviderType;
    }

    public void setLdapProviderType(LdapProviderType ldapProviderType) {
        this.ldapProviderType = ldapProviderType;
    }

    public String getName() {
        return name;
    }

    public void setRootDSE(RootDSE rootDSE) {
        this.rootDSE = rootDSE;
    }

    public RootDSE getRootDSE() {
        return rootDSE;
    }

    public void setLdapServers(List<URI> ldapServersURIs) {
        lock.writeLock().lock();
        try {
            this.ldapServers = ldapServersURIs;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<URI> getLdapServers() {
        lock.readLock().lock();
        try {
            return new ArrayList<URI>(ldapServers);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addLDAPServer(URI uri) {
        try {
            lock.readLock().lock();
            ldapServers.add(uri);
        } finally {
            lock.readLock().unlock();
        }
    }


    public LDAPSecurityAuthentication getLdapSecurityAuthentication() {
        return ldapSecurityAuthentication;
    }

    public void setLdapSecurityAuthentication(LDAPSecurityAuthentication ldapSecurityAuthentication) {
        this.ldapSecurityAuthentication = ldapSecurityAuthentication;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

}
