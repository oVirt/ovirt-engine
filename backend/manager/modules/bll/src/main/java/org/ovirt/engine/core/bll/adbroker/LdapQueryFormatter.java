package org.ovirt.engine.core.bll.adbroker;

public interface LdapQueryFormatter<T> {

    T format( LdapQueryMetadata queryMetadata );

}
