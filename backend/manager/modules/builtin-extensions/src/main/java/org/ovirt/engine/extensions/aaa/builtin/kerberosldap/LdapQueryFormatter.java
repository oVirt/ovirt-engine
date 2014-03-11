package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public interface LdapQueryFormatter<T> {

    T format( LdapQueryMetadata queryMetadata );

}
