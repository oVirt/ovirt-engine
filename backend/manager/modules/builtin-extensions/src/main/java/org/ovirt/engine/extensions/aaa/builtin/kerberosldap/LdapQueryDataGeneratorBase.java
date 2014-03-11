package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.HashSet;
import java.util.Set;

public abstract class LdapQueryDataGeneratorBase<T> {

    protected Set<T> ldapIdentifiers = new HashSet<T>();

    /**
     *
     */
    public LdapQueryDataGeneratorBase(Set<T> identifiers) {
        this.ldapIdentifiers = identifiers;
    }

    public LdapQueryDataGeneratorBase() {
    }

    public void add(T identifier) {
        ldapIdentifiers.add(identifier);
    }

    public boolean getHasValues() {
        return !ldapIdentifiers.isEmpty();
    }

    // public abstract List<LdapQueryData> getLdapQueriesData(String domain);
}
