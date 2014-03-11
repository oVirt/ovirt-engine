package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.serverordering.OrderingAlgorithmType;

public class LdapSearchExceptionHandlingResponse {
    private boolean tryNextServer;
    private Exception translatedException;
    private OrderingAlgorithmType orderingAlgorithm;

    public boolean isTryNextServer() {
        return tryNextServer;
    }

    public Exception getTranslatedException() {
        return translatedException;
    }

    public LdapSearchExceptionHandlingResponse setTryNextServer(boolean tryNextServer) {
        this.tryNextServer = tryNextServer;
        return this;
    }


    public LdapSearchExceptionHandlingResponse setTranslatedException(Exception translatedException) {
        this.translatedException = translatedException;
        return this;
    }

    public OrderingAlgorithmType getOrderingAlgorithm() {
        return orderingAlgorithm;
    }

    public LdapSearchExceptionHandlingResponse setOrderingAlgorithm(OrderingAlgorithmType orderingAlgorithm) {
        this.orderingAlgorithm = orderingAlgorithm;
        return this;
    }
}
