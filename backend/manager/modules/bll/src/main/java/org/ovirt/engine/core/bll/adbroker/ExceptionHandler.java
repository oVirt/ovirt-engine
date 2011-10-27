package org.ovirt.engine.core.bll.adbroker;

public interface ExceptionHandler<T> {

    public T handle(Exception e);

}

