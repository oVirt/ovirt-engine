package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public interface ExceptionHandler<T, P> {

    public T handle(Exception e, P params);

}

